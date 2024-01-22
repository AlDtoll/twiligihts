package aldtoll.twiligihts.logic

import aldtoll.twiligihts.storage.EnemyHandsListInteractor
import aldtoll.twiligihts.storage.EnemyInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillEnemyExecutor @Inject constructor(
    private val enemyInteractor: EnemyInteractor,
    private val enemyHandsListInteractor: EnemyHandsListInteractor,
) {

    fun execute() {
        enemyInteractor.init()
        enemyHandsListInteractor.init()
    }
}