package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.model.Person
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EndTurnExecutor @Inject constructor(
    private val perkExecutor: PerkExecutor,
    private val enemyInteractor: EnemyInteractor,
    private val heroInteractor: HeroInteractor,
) {

    fun execute() {
        clearEnemyShield()
        enemyActions()
        clearHeroShield()
    }

    private fun enemyActions() {
        val enemy = enemyInteractor.value()
        enemy?.run {
            this.perks.forEach { perk: Perk ->
                perkExecutor.execute(perk)
            }
        }
    }

    private fun clearHeroShield() {
        val person = heroInteractor.value()
        person?.run {
            clearPersonShield(this)
            heroInteractor.update(this)
        }
    }


    private fun clearEnemyShield() {
        val person = enemyInteractor.value()
        person?.run {
            clearPersonShield(this)
            enemyInteractor.update(this)
        }
    }

    private fun clearPersonShield(person: Person) {
        person.run {
            this.shield = 0
        }
    }
}