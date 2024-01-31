package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Effect
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
        executePerkEffect(perk)
    }

    private fun executePerkEffect(perk: Perk) {
        val hero = heroInteractor.value()
        val enemy = enemyInteractor.value()
        perk.effects.forEach { originalEffect ->
            val perkMessage = if (isHeroPerk) {
                "Герой применяет ${perk.name}:${perk.description}"
            } else {
                "Противник применяет ${perk.name}:${perk.description}"
            }
            battleLogListInteractor.add(perkMessage)
            val effect = changeEffectByStatuses(originalEffect)
            when (effect.type) {
                Effect.EffectType.ATTACK_HP,
                Effect.EffectType.ATTACK_SP,
                Effect.EffectType.ATTACK -> {
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

                Effect.EffectType.DEFEND -> {
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

                Effect.EffectType.ADD_STATUS -> {
                    when (originalEffect.target) {
                        Effect.EffectTarget.ENEMY -> {
                            addStatusForPerson(originalEffect, false)
                        }

                        Effect.EffectTarget.HERO -> {
                            addStatusForPerson(originalEffect, true)
                        }

                        Effect.EffectTarget.ALL -> {
                            addStatusForPerson(originalEffect, false)
                            addStatusForPerson(originalEffect, true)
                        }
                    }
                }
            }
        }
    }

    private fun attackPerson(effect: Effect, vararg persons: Person) {
        persons.forEach { person: Person ->
            val isHeroTarget = person is Hero
            val personInteractor = personInteractor(isHeroTarget)
            person.run {
                val dodgeStatus =
                    this.statuses.find { status: Status -> status.type == Status.EffectType.DODGE }
                if (dodgeStatus != null && dodgeStatus.isActive()) {
                    dodge(isHeroTarget, dodgeStatus)
                } else {
                    val damageForSp = damageForSp(effect)
                    val damageBlockedByShield = damageShield(damageForSp)
                    val damageForHp = damageForHp(effect, damageBlockedByShield)
                    damageHp(damageForHp, isHeroTarget)
                }
                personInteractor.update(person)
            }
        }
    }

    private fun changeEffectByStatuses(effect: Effect): Effect {
        val effectForChange = effect.copy()
        val effectChangeByHeroStatuses = effectChangeByPersonStatuses(effectForChange, true)
        val effectChangeByEnemyStatuses =
            effectChangeByPersonStatuses(effectChangeByHeroStatuses, false)
        return effectChangeByEnemyStatuses
    }

    private fun effectChangeByPersonStatuses(
        effectForChange: Effect,
        isHeroTarget: Boolean
    ): Effect {
        val effect = effectForChange.copy()
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
                            when (effect.type) {
                                Effect.EffectType.ATTACK,
                                Effect.EffectType.ATTACK_HP,
                                Effect.EffectType.ATTACK_SP -> effect.value =
                                    decreaseEffectValueByStatus(effect, status)

                                else -> {}
                            }

                        }
                        if (status.type == Status.EffectType.STRONG) {
                            when (effect.type) {
                                Effect.EffectType.ATTACK,
                                Effect.EffectType.ATTACK_HP,
                                Effect.EffectType.ATTACK_SP -> effect.value =
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
                            when (effect.type) {
                                Effect.EffectType.ATTACK -> effect.value =
                                    effect.value + status.value

                                Effect.EffectType.ATTACK_HP -> effect.value =
                                    effect.value + status.value

                                Effect.EffectType.ATTACK_SP -> effect.value =
                                    effect.value + status.value

                                else -> {}
                            }
                        }
                        if (status.type == Status.EffectType.ARMOR) {
                            when (effect.type) {
                                Effect.EffectType.ATTACK,
                                Effect.EffectType.ATTACK_HP,
                                Effect.EffectType.ATTACK_SP -> effect.value =
                                    decreaseEffectValueByStatus(effect, status)

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
        effect: Effect,
        status: Status
    ): Int {
        val i = effect.value - status.value
        return i.coerceAtLeast(0)
    }

    private fun damageForHp(
        effect: Effect,
        damageBlockedByShield: Int
    ): Int {
        return when (effect.type) {
            Effect.EffectType.ATTACK -> effect.value - damageBlockedByShield
            Effect.EffectType.ATTACK_HP -> effect.value
            Effect.EffectType.ATTACK_SP -> 0
            else -> effect.value - damageBlockedByShield
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

    private fun Person.damageHp(
        damage: Int,
        isHeroTarget: Boolean
    ) {
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

    private fun defendPerson(effect: Effect, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            this.shield = this.shield + effect.value
            personInteractor.update(person)
        }
    }

    private fun damageForSp(effect: Effect): Int {
        return when (effect.type) {
            Effect.EffectType.ATTACK -> effect.value
            Effect.EffectType.ATTACK_HP -> 0
            Effect.EffectType.ATTACK_SP -> effect.value
            else -> 0
        }
    }

    private fun addStatusForPerson(effect: Effect, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val newPerson = this.recreate()
            effect.status?.let { effectStatus ->
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