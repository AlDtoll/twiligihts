package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinishBattleExecutor @Inject constructor(
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val turnNumberInteractor: TurnNumberInteractor,
    private val attemptCounterInteractor: AttemptCounterInteractor,
    private val logListInteractor: BattleLogListInteractor,
    private val goToFinishScreenInteractor: GoToFinishScreenInteractor,
) {

    private val database = Firebase.database

    fun execute() {
        val resultReference = database.getReference("Result")
        resultReference.setValue(
            BattleResult(
                true,
                heroInteractor.value()?.hp ?: 0,
                enemyInteractor.value()?.hp ?: 0,
                turnNumberInteractor.value() ?: 0,
                attemptCounterInteractor.value() ?: 0
            )
        )
        val logReference = database.getReference("Log")
        logReference.setValue(
            logListInteractor.value()?.map {
                it.message
            }
        )
        goToFinishScreenInteractor.update(Pair(false, false))
    }
}