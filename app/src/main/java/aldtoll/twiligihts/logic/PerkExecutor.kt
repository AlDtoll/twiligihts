package aldtoll.twiligihts.logic

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
        perk.effects.forEach { effect ->
            val perkMessage = if (isHeroPerk) {
                "Герой применяет ${perk.name}:${perk.description}"
            } else {
                "Противник применяет ${perk.name}:${perk.description}"
            }
            battleLogListInteractor.add(perkMessage)
            when (effect.effectType) {
                Perk.Effect.EffectType.ATTACK -> {
                    when (effect.target) {
                        Perk.Effect.EffectTarget.ENEMY -> {
                            attackPerson(effect, false)
                        }

                        Perk.Effect.EffectTarget.HERO -> {
                            attackPerson(effect, true)
                        }

                        Perk.Effect.EffectTarget.ALL -> {
                            attackPerson(effect, false)
                            attackPerson(effect, true)
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

    private fun attackPerson(effect: Perk.Effect, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val dodgeStatus =
                this.statuses.find { status: Status -> status.effectType == Status.EffectType.DODGE }
            if (dodgeStatus != null && dodgeStatus.isActive()) {
                dodge(isHeroTarget, dodgeStatus)
            } else {
                damageShield(effect)
                damageHp(effect, isHeroTarget)
            }
            personInteractor.update(person)
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
        effect: Perk.Effect,
        isHeroTarget: Boolean
    ) {
        var message = ""
        val damageForHp: Int = if (effect.value >= this.shield) {
            effect.value - this.shield
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
        battleLogListInteractor.add(message)
        if (damageForHp > 0) {
            //inflictWound(damageForHp, isHeroTarget)
        }
    }

    private fun Person.damageShield(
        effect: Perk.Effect
    ) {
        var message = ""
        val damageForSp: Int = if (effect.value >= this.shield) {
            this.shield
        } else {
            this.shield - effect.value
        }
        if (this.shield > 0) {
            message += "Щиты блокируют $damageForSp урона. "
            if (damageForSp >= this.shield) {
                message += "Щиты уничтожены. "
                this.shield = 0
            } else {
                this.shield = this.shield - damageForSp
            }
        }
        if (message.isNotEmpty()) {
            battleLogListInteractor.add(message)
        }
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

    private fun addStatusForPerson(effect: Perk.Effect, isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            effect.status?.let { effectStatus ->
                val statusForChange =
                    person.statuses.find { personStatus -> personStatus.effectType == effectStatus.effectType }
                if (statusForChange != null) {
                    statusForChange.value = statusForChange.value + effectStatus.value
                } else {
                    person.statuses.add(effectStatus)
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