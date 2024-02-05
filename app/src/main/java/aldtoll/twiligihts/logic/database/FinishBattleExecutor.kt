package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.storage.EnemyInteractor
import aldtoll.twiligihts.storage.HeroInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinishBattleExecutor @Inject constructor(
    private val heroInteractor: HeroInteractor,
    private val enemyInteractor: EnemyInteractor,
    private val turnNumberInteractor: TurnNumberInteractor,
) {

    private val database = Firebase.database

    fun execute() {
        val reference = database.getReference("Result")
        reference.setValue(
            BattleResult(
                true,
                heroInteractor.value()?.hp ?: 0,
                enemyInteractor.value()?.hp ?: 0,
                turnNumberInteractor.value() ?: 0
            )
        )
    }
}