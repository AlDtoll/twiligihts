package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Person
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroInteractor
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

                Perk.Effect.EffectType.DODGE -> {

                }
            }
        }
    }

    private fun attackPerson(effect: Perk.Effect, isHeroTarget: Boolean) {
        val personInteractor = if (isHeroTarget) {
            heroInteractor
        } else {
            enemyInteractor
        }
        val person = personInteractor.value()
        person?.run {
            var message = ""
            val damageForHp: Int
            val damageForSp: Int
            if (effect.value >= this.shield) {
                damageForSp = this.shield
                damageForHp = effect.value - this.shield
            } else {
                damageForSp = this.shield - effect.value
                damageForHp = 0
            }
            message += "Щиты блокируют $damageForSp урона. "
            if (damageForSp >= this.shield) {
                message += "Щиты уничтожены. "
                this.shield = 0
            } else {
                this.shield = this.shield - damageForSp
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
                inflictWound(damageForHp, isHeroTarget)
            }
            personInteractor.update(person)
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
        val personInteractor = if (isHeroTarget) {
            heroInteractor
        } else {
            enemyInteractor
        }
        val person = personInteractor.value()
        person?.run {
            this.shield = this.shield + effect.value
            personInteractor.update(person)
        }
    }

    private fun payPerkPrice(perk: Perk) {
        updateStockExecutor.payPriceForPerk(perk)
    }
}