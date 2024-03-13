package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.enemy.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.enemy.EnemyStatesInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillEnemyExecutor @Inject constructor(
    private val enemyInteractor: EnemyInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
    private val enemyStatesInteractor: EnemyStatesInteractor,
) {

    fun execute() {
        enemyInteractor.init()
        enemyHandsListInteractor.init()
        enemyStatesInteractor.init()
    }
}