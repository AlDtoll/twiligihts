package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Condition
import aldtoll.twiligihts.model.Effect
import aldtoll.twiligihts.model.Enemy
import aldtoll.twiligihts.model.Hero
import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Person
import aldtoll.twiligihts.model.Status
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class PerkExecutor @Inject constructor(
    private val updateStockExecutor: UpdateStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val battleLogListInteractor: BattleLogListInteractor,
) {

    private var isHeroPerk = false
    fun execute(perk: Perk, isHero: Boolean = false) {
        this.isHeroPerk = isHero
        if (isHero) {
            payPerkPrice(perk)
        }
        executePerkEffects(perk)
    }

    private fun executePerkEffects(perk: Perk) {
        val hero = heroInteractor.value()
        val enemy = enemyInteractor.value()
        val perkMessage = if (isHeroPerk) {
            "Герой применяет ${perk.name}:${perk.description}"
        } else {
            "Противник применяет ${perk.name}:${perk.description}"
        }
        battleLogListInteractor.add(perkMessage)
        perk.effects.forEach { originalEffect ->
            if (originalEffect.condition != null) {
                if (checkCondition(originalEffect.condition!!, enemy!!, hero!!)) {
                    applyEffect(originalEffect, enemy, hero)
                }
            } else {
                applyEffect(originalEffect, enemy, hero)
            }
        }
    }

    private fun checkCondition(
        condition: Condition,
        enemy: Enemy,
        hero: Hero
    ): Boolean {
        return when (condition.target) {
            Effect.EffectTarget.ENEMY -> {
                return enemy.checkConditionForPerson(condition)
            }

            Effect.EffectTarget.HERO -> hero.checkConditionForPerson(condition)
            Effect.EffectTarget.ALL -> {
                return enemy.checkConditionForPerson(condition)
                        && hero.checkConditionForPerson(condition)
            }
        }
    }

    private fun Person.checkConditionForPerson(
        condition: Condition
    ): Boolean {
        val valueForCompare = when (condition.parameter) {
            Condition.Parameter.HP -> this.hp
            Condition.Parameter.SP -> this.shield
        }
        return when (condition.symbol) {
            Condition.Symbol.MORE -> valueForCompare > condition.value
            Condition.Symbol.LESS -> valueForCompare < condition.value
        }
    }

    private fun applyEffect(
        originalEffect: Effect,
        enemy: Enemy?,
        hero: Hero?
    ) {
        val effect = changeEffectByPersonsStatuses(originalEffect)
        when (effect) {
            is Effect.Attack -> {
                when (effect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        attackPerson(effect, enemy!!)
                    }

                    Effect.EffectTarget.HERO -> {
                        attackPerson(effect, hero!!)
                    }

                    Effect.EffectTarget.ALL -> {
                        attackPerson(effect, hero!!, enemy!!)
                    }
                }
            }

            is Effect.Defend -> {
                when (effect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        defendPerson(effect, false)
                    }

                    Effect.EffectTarget.HERO -> {
                        defendPerson(effect, true)
                    }

                    Effect.EffectTarget.ALL -> {
                        defendPerson(effect, false)
                        defendPerson(effect, true)
                    }
                }
            }

            is Effect.ChangeStatus -> {
                when (originalEffect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        addStatusForPerson(effect, false)
                    }

                    Effect.EffectTarget.HERO -> {
                        addStatusForPerson(effect, true)
                    }

                    Effect.EffectTarget.ALL -> {
                        addStatusForPerson(effect, false)
                        addStatusForPerson(effect, true)
                    }
                }
            }

            is Effect.ChangeStock -> {
                updateStockExecutor.updateStocks(Pair(effect.gemType, effect.value))
            }

            is Effect.Heal -> {
                val persons = arrayListOf<Person>()
                when (originalEffect.target) {
                    Effect.EffectTarget.ENEMY -> {
                        persons.add(enemy!!)
                    }

                    Effect.EffectTarget.HERO -> {
                        persons.add(hero!!)
                    }

                    Effect.EffectTarget.ALL -> {
                        persons.add(hero!!)
                        persons.add(enemy!!)
                    }
                }
                healPerson(effect, *persons.toTypedArray())
            }
        }
    }

    private fun healPerson(heal: Effect.Heal, vararg persons: Person) {
        persons.forEach { person: Person ->
            val isHeroTarget = person is Hero
            val personInteractor = personInteractor(isHeroTarget)
            person.run {
                healDamage(heal)
                personInteractor.update(person)
            }
        }
    }

    private fun attackPerson(attack: Effect.Attack, vararg persons: Person) {
        persons.forEach { person: Person ->
            val isHeroTarget = person is Hero
            val personInteractor = personInteractor(isHeroTarget)
            person.run {
                val dodgeStatus =
                    this.statuses.find { status: Status -> status.type == Status.EffectType.DODGE }
                if (dodgeStatus != null && dodgeStatus.isActive()) {
                    dodge(isHeroTarget, dodgeStatus)
                } else {
                    makeDamage(attack)
                }
                val counterAttackStatus =
                    this.statuses.find { status: Status -> status.type == Status.EffectType.COUNTERATTACK }
                if (counterAttackStatus != null && counterAttackStatus.isActive()) {
                    counterAttack(counterAttackStatus, this)
                }
                personInteractor.update(person)
            }
        }
    }

    private fun Person.makeDamage(
        attack: Effect.Attack
    ) {
        val damageForSp = damageForSp(attack)
        val damageBlockedByShield = damageShield(damageForSp)
        val damageForHp = damageForHp(attack, damageBlockedByShield)
        damageHp(damageForHp)
    }

    private fun Person.healDamage(
        heal: Effect.Heal
    ) {
        val isHeroTarget = this is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "восстанавливает ${heal.value} здоровья. "
        if (this.hp + heal.value > this.maxHp) {
            this.hp = this.maxHp
            message += "Здоровье полностью восстановлено"
        } else {
            this.hp = this.hp + this.maxHp
        }
        battleLogListInteractor.add(message)
    }

    private fun changeEffectByPersonsStatuses(effect: Effect): Effect {
        val effectForChange = effect.copyEffect()
        val effectChangeByHeroStatuses = effectChangeByPersonStatuses(effectForChange, true)
        val effectChangeByEnemyStatuses =
            effectChangeByPersonStatuses(effectChangeByHeroStatuses, false)
        return effectChangeByEnemyStatuses
    }

    private fun effectChangeByPersonStatuses(
        effectForChange: Effect,
        isHeroTarget: Boolean
    ): Effect {
        val effect = effectForChange.copyEffect()
        val person = personInteractor(isHeroTarget).value()
        person?.run {
            val statuses = this.statuses
            if (statuses.isNotEmpty()) {
                statuses.forEach { status ->
                    val isPersonPerk = if (isHeroTarget) {
                        isHeroPerk
                    } else {
                        !isHeroPerk
                    }
                    if (isPersonPerk) {
                        if (status.type == Status.EffectType.WEAK) {
                            when (effect) {
                                is Effect.Attack -> effect.value =
                                    decreaseEffectValueByStatus(effect, status)

                                else -> {}
                            }

                        }
                        if (status.type == Status.EffectType.STRONG) {
                            when (effect) {
                                is Effect.Attack -> effect.value =
                                    effect.value + status.value

                                else -> {}
                            }
                        }
                    }
                    val isPersonTarget = if (isHeroTarget) {
                        Effect.EffectTarget.HERO
                    } else {
                        Effect.EffectTarget.ENEMY
                    }
                    if (effect.target == isPersonTarget || effect.target == Effect.EffectTarget.ALL) {
                        if (status.type == Status.EffectType.VULNERABLE) {
                            when (effect) {
                                is Effect.Attack -> effect.value =
                                    effect.value + status.value

                                else -> {}
                            }
                        }
                        if (status.type == Status.EffectType.ARMOR) {
                            when (effect) {
                                is Effect.Attack ->
                                    effect.value = decreaseEffectValueByStatus(effect, status)

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
        return effect
    }

    private fun decreaseEffectValueByStatus(
        attack: Effect.Attack,
        status: Status
    ): Int {
        val i = attack.value - status.value
        return i.coerceAtLeast(0)
    }

    private fun damageForHp(
        attack: Effect.Attack,
        damageBlockedByShield: Int
    ): Int {
        return when (attack.type) {
            Effect.Attack.Type.BOTH -> attack.value - damageBlockedByShield
            Effect.Attack.Type.HP -> attack.value
            Effect.Attack.Type.SP -> 0
        }
    }

    private fun dodge(
        isHeroTarget: Boolean,
        dodgeStatus: Status
    ) {
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "уворачивается."
        dodgeStatus.value = dodgeStatus.value - 1
        battleLogListInteractor.add(message)
    }

    private fun counterAttack(
        counterAttackStatus: Status,
        person: Person
    ) {
        val isHeroTarget = person is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "в ответ наносит ${counterAttackStatus.value} урона."
        battleLogListInteractor.add(message)
        val attack = Effect.Attack(
            counterAttackStatus.value,
            Effect.Attack.Type.BOTH,
            target = if (isHeroTarget) Effect.EffectTarget.HERO else Effect.EffectTarget.ENEMY
        )

        val personInteractor = personInteractor(!isHeroTarget)
        personInteractor.value()?.run {
            //todo сейчас при контр атаке будет проигнорировано уклонение,
            // может надо на attackPerson заменить, но тогда будут проблемы,
            // что контратаки будут друг друга бить
            this.makeDamage(attack)
            personInteractor.update(this)
        }
    }

    private fun Person.damageHp(
        damage: Int
    ) {
        val isHeroTarget = this is Hero
        var message = ""
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "получает $damage урона. "
        if (damage > this.hp) {
            this.hp = 0
        } else {
            this.hp = this.hp - damage
        }
        updateStockExecutor.updateStockAfterDamage()
        battleLogListInteractor.add(message)
        if (damage > 0) {
            //inflictWound(damageForHp, isHeroTarget)
        }
    }

    private fun Person.damageShield(
        damage: Int
    ): Int {
        var message = ""
        val damageForSp: Int = if (damage >= this.shield) {
            this.shield
        } else {
            damage
        }
        if (this.shield > 0) {
            message += "Щиты блокируют $damageForSp урона. "
            if (damageForSp >= this.shield && damageForSp > 0) {
                message += "Щиты уничтожены. "
                this.shield = 0
            } else {
                this.shield = this.shield - damageForSp
            }
        }
        if (message.isNotEmpty()) {
            battleLogListInteractor.add(message)
        }
        return damageForSp
    }

    private fun Person.inflictWound(damageForHp: Int, isHeroTarget: Boolean) {
        val message = if (isHeroTarget) {
            "Герой получает рану"
        } else {
            "Противник получает рану"
        }
        if (damageForHp > this.hp) {
            battleLogListInteractor.add(message)
            this.wounds = this.wounds + 1
        } else {
            val percentOfDamage = 100 * damageForHp / this.hp
            val r = Random.nextInt(1, 100)
            if (r < percentOfDamage) {
                battleLogListInteractor.add(message)
                this.wounds = this.wounds + 1
            }
        }
    }

    private fun defendPerson(defend: Effect.Defend, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            this.shield = this.shield + defend.value
            personInteractor.update(person)
        }
    }

    private fun damageForSp(attack: Effect.Attack): Int {
        return when (attack.type) {
            Effect.Attack.Type.BOTH -> attack.value
            Effect.Attack.Type.HP -> 0
            Effect.Attack.Type.SP -> attack.value
            else -> 0
        }
    }

    private fun addStatusForPerson(effect: Effect.ChangeStatus, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val newPerson = this.recreate()
            effect.status.let { effectStatus ->
                val statusForChange =
                    newPerson.statuses.find { personStatus -> personStatus.type == effectStatus.type }
                if (statusForChange != null) {
                    statusForChange.value = statusForChange.value + effectStatus.value
                } else {
                    newPerson.statuses.add(effectStatus.copy())
                }
            }
            personInteractor.update(newPerson)
        }
    }

    private fun personInteractor(isHeroTarget: Boolean): PersonInteractor {
        val personInteractor = if (isHeroTarget) {
            heroInteractor
        } else {
            enemyInteractor
        }
        return personInteractor
    }

    private fun payPerkPrice(perk: Perk) {
        updateStockExecutor.payPriceForPerk(perk)
    }
}