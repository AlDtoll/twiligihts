package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Perk
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.PersonInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EndTurnExecutor @Inject constructor(
    private val perkExecutor: PerkExecutor,
    private val enemyInteractor: EnemyInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val heroInteractor: HeroInteractor,
    private val battleLogListInteractor: BattleLogListInteractor,
    private val updateStockExecutor: UpdateStockExecutor,
) {

    fun execute() {
        battleLogListInteractor.add("")
        clearPersonShield(false)
        enemyActions()
        clearPersonShield(true)
        clearPersonStatus(true)
        updateStockExecutor.updateStocksAfterTurn()
        battleLogListInteractor.add("")
    }

    private fun enemyActions() {
        val enemyHands = enemyHandsListInteractor.value()
        enemyHands?.run {
            this.forEach { hand ->
                hand.perks.forEach { perk: Perk ->
                    perkExecutor.execute(perk)
                }
            }
        }
    }

    private fun clearPersonShield(isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            this.shield = 0
            personInteractor.update(this)
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

    private fun clearPersonStatus(isHeroTarget: Boolean) {
        val personInteractor = personInteractor(isHeroTarget)
        val person = personInteractor.value()
        person?.run {
            val newPerson = this.recreate()
            newPerson.statuses.forEach {
                it.value = 0
            }
            personInteractor.update(newPerson)
        }
    }
}