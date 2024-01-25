package aldtoll.twiligihts.logic

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
        perk.effects.forEach { effect ->
            val perkMessage = if (isHeroPerk) {
                "Герой применяет ${perk.name}:${perk.description}"
            } else {
                "Противник применяет ${perk.name}:${perk.description}"
            }
            battleLogListInteractor.add(perkMessage)
            when (effect.type) {
                Perk.Effect.EffectType.ATTACK_HP,
                Perk.Effect.EffectType.ATTACK_SP,
                Perk.Effect.EffectType.ATTACK -> {
                    when (effect.target) {
                        Perk.Effect.EffectTarget.ENEMY -> {
                            attackPerson(effect, enemy!!)
                        }

                        Perk.Effect.EffectTarget.HERO -> {
                            attackPerson(effect, hero!!)
                        }

                        Perk.Effect.EffectTarget.ALL -> {
                            attackPerson(effect, hero!!, enemy!!)
                        }
                    }
                }

                Perk.Effect.EffectType.DEFEND -> {
                    when (effect.target) {
                        Perk.Effect.EffectTarget.ENEMY -> {
                            defendPerson(effect, false)
                        }

                        Perk.Effect.EffectTarget.HERO -> {
                            defendPerson(effect, true)
                        }

                        Perk.Effect.EffectTarget.ALL -> {
                            defendPerson(effect, false)
                            defendPerson(effect, true)
                        }
                    }
                }

                Perk.Effect.EffectType.ADD_STATUS -> {
                    when (effect.target) {
                        Perk.Effect.EffectTarget.ENEMY -> {
                            addStatusForPerson(effect, false)
                        }

                        Perk.Effect.EffectTarget.HERO -> {
                            addStatusForPerson(effect, true)
                        }

                        Perk.Effect.EffectTarget.ALL -> {
                            addStatusForPerson(effect, false)
                            addStatusForPerson(effect, true)
                        }
                    }
                }
            }
        }
    }

    private fun attackPerson(effect: Perk.Effect, vararg persons: Person) {
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

    private fun damageForHp(
        effect: Perk.Effect,
        damageBlockedByShield: Int
    ): Int {
        return when (effect.type) {
            Perk.Effect.EffectType.ATTACK -> effect.value - damageBlockedByShield
            Perk.Effect.EffectType.ATTACK_HP -> effect.value
            Perk.Effect.EffectType.ATTACK_SP -> 0
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
        val damageForHp: Int = if (damage >= this.shield) {
            damage - this.shield
        } else {
            0
        }
        message += if (isHeroTarget) {
            "Герой "
        } else {
            "Противник "
        }
        message += "получает $damageForHp урона. "
        if (damageForHp > this.hp) {
            this.hp = 0
        } else {
            this.hp = this.hp - damageForHp
        }
        updateStockExecutor.updateStockAfterDamage()
        battleLogListInteractor.add(message)
        if (damageForHp > 0) {
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

    private fun defendPerson(effect: Perk.Effect, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            this.shield = this.shield + effect.value
            personInteractor.update(person)
        }
    }

    private fun damageForSp(effect: Perk.Effect): Int {
        return when (effect.type) {
            Perk.Effect.EffectType.ATTACK -> effect.value
            Perk.Effect.EffectType.ATTACK_HP -> 0
            Perk.Effect.EffectType.ATTACK_SP -> effect.value
            else -> 0
        }
    }

    private fun addStatusForPerson(effect: Perk.Effect, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            effect.status?.let { effectStatus ->
                val statusForChange =
                    person.statuses.find { personStatus -> personStatus.type == effectStatus.type }
                if (statusForChange != null) {
                    statusForChange.value = statusForChange.value + effectStatus.value
                } else {
                    person.statuses.add(effectStatus.copy())
                }
            }
            personInteractor.update(person)
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