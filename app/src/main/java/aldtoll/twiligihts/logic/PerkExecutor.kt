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
            when (effect.effectType) {
                Perk.Effect.EffectType.ATTACK -> {
                    when (effect.target) {
                        Perk.Effect.EffectTarget.ENEMY -> {
                            attackEnemy(effect)
                        }

                        Perk.Effect.EffectTarget.HERO -> {
                            attackHero(effect)
                        }

                        Perk.Effect.EffectTarget.ALL -> {
                            attackEnemy(effect)
                            attackHero(effect)
                        }
                    }
                }

                Perk.Effect.EffectType.DEFEND -> {
                    when (effect.target) {
                        Perk.Effect.EffectTarget.ENEMY -> {
                            defendEnemy(effect)
                        }

                        Perk.Effect.EffectTarget.HERO -> {
                            defendHero(effect)
                        }

                        Perk.Effect.EffectTarget.ALL -> {
                            defendEnemy(effect)
                            defendHero(effect)
                        }
                    }
                }

                Perk.Effect.EffectType.DODGE -> {

                }
            }
        }
    }

    private fun attackHero(effect: Perk.Effect) {
        attackPerson(effect, true)
    }

    private fun attackEnemy(effect: Perk.Effect) {
        attackPerson(effect, false)
    }

    private fun attackPerson(effect: Perk.Effect, isHeroTarget: Boolean) {
        val personInteractor = if (isHeroTarget) {
            heroInteractor
        } else {
            enemyInteractor
        }
        val person = personInteractor.value()
        person?.run {
            val damageForHp: Int
            val effectMessage = if (isHeroPerk) {
                "Герой наносит ${effect.value} урона"
            } else {
                "Противник наносит ${effect.value} урона"
            }
            battleLogListInteractor.add(effectMessage)
            if (effect.value > this.shield) {
                damageForHp = effect.value - this.shield
                this.shield = 0
                inflictWound(damageForHp)
            } else {
                damageForHp = 0
                this.shield = this.shield - effect.value
            }
            val damageMessage = if (isHeroTarget) {
                "Герой получает $damageForHp урона"
            } else {
                "Противник получает $damageForHp урона"
            }
            battleLogListInteractor.add(damageMessage)
            if (damageForHp > this.hp) {
                this.hp = 0
            } else {
                this.hp = this.hp - damageForHp
            }
            personInteractor.update(person)
        }
    }

    private fun Person.inflictWound(damageForHp: Int) {
        val message = "Нанесена рана"
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

    private fun defendHero(effect: Perk.Effect) {
        val person = heroInteractor.value()
        person?.run {
            defendPerson(effect, this)
            heroInteractor.update(this)
        }
    }


    private fun defendEnemy(effect: Perk.Effect) {
        val person = enemyInteractor.value()
        person?.run {
            defendPerson(effect, this)
            enemyInteractor.update(this)
        }
    }

    private fun defendPerson(effect: Perk.Effect, person: Person) {
        person.run {
            this.shield = this.shield + effect.value
        }
    }

    private fun payPerkPrice(perk: Perk) {
        updateStockExecutor.payPriceForPerk(perk)
    }
}