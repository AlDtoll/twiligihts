package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Person
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerkExecutor @Inject constructor(
    private val updateStockExecutor: UpdateStockExecutor,
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
) {
    fun execute(perk: Hand.Perk) {
        payPerkPrice(perk)
        executePerkEffect(perk)
    }

    private fun executePerkEffect(perk: Hand.Perk) {
        perk.effects.forEach { effect ->
            when (effect.effectType) {
                Hand.Perk.Effect.EffectType.ATTACK -> {
                    when (effect.target) {
                        Hand.Perk.Effect.EffectTarget.ENEMY -> {
                            attackEnemy(effect)
                        }

                        Hand.Perk.Effect.EffectTarget.PERSON -> {
                            attackHero(effect)
                        }

                        Hand.Perk.Effect.EffectTarget.ALL -> {
                            attackEnemy(effect)
                            attackHero(effect)
                        }
                    }
                }

                Hand.Perk.Effect.EffectType.DEFEND -> {
                    when (effect.target) {
                        Hand.Perk.Effect.EffectTarget.ENEMY -> {
                            defendEnemy(effect)
                        }

                        Hand.Perk.Effect.EffectTarget.PERSON -> {
                            defendHero(effect)
                        }

                        Hand.Perk.Effect.EffectTarget.ALL -> {
                            defendEnemy(effect)
                            defendHero(effect)
                        }
                    }
                }

                Hand.Perk.Effect.EffectType.DODGE -> {

                }
            }
        }
    }

    private fun attackHero(effect: Hand.Perk.Effect) {
        val person = heroInteractor.value()
        person?.run {
            attackPerson(effect, this)
            heroInteractor.update(this)
        }
    }

    private fun attackEnemy(effect: Hand.Perk.Effect) {
        val person = enemyInteractor.value()
        person?.run {
            attackPerson(effect, this)
            enemyInteractor.update(this)
        }
    }

    private fun attackPerson(effect: Hand.Perk.Effect, person: Person) {
        person.run {
            val damageForHp: Int
            if (effect.value > this.shield) {
                damageForHp = effect.value - this.shield
                this.shield = 0
            } else {
                damageForHp = 0
                this.shield = this.shield - effect.value
            }
            this.hp = this.hp - damageForHp
        }
    }

    private fun defendHero(effect: Hand.Perk.Effect) {
        val person = heroInteractor.value()
        person?.run {
            defendPerson(effect, this)
            heroInteractor.update(this)
        }
    }


    private fun defendEnemy(effect: Hand.Perk.Effect) {
        val person = enemyInteractor.value()
        person?.run {
            defendPerson(effect, this)
            enemyInteractor.update(this)
        }
    }

    private fun defendPerson(effect: Hand.Perk.Effect, person: Person) {
        person.run {
            this.shield = this.shield + effect.value
        }
    }

    private fun payPerkPrice(perk: Hand.Perk) {
        updateStockExecutor.payPriceForPerk(perk)
    }
}