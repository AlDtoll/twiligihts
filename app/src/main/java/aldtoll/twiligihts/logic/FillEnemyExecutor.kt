package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyResourcesInteractor
import aldtoll.twiligihts.storage.enemy.EnemySectorsInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatusesInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStockPerksInteractor
import aldtoll.twiligihts.storage.enemy.EnemyTimePerksInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillEnemyExecutor @Inject constructor(
    private val enemyStockListInteractor: EnemyStockListInteractor,
    private val enemyStockPerksInteractor: EnemyStockPerksInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val enemyStatesInteractor: EnemyStatesInteractor,
    private val enemyStatusesInteractor: EnemyStatusesInteractor,
    private val enemySectorsInteractor: EnemySectorsInteractor,
    private val enemyResourcesInteractor: EnemyResourcesInteractor,
    private val enemyTimePerksInteractor: EnemyTimePerksInteractor,
) {

    fun execute() {
        enemyInteractor.init()
        enemyStockListInteractor.init()
        enemyStockPerksInteractor.init()
        enemyHandsListInteractor.init()
        enemyStatesInteractor.init()
        enemyStatusesInteractor.init()
        enemySectorsInteractor.init()
        enemyResourcesInteractor.init()
        enemyTimePerksInteractor.init()
        //todo пока здесь, чтобы не убирать Person.statuses
        enemyStatusesInteractor.value()?.run {
            enemyInteractor.value()?.statuses = this
        }
    }
}