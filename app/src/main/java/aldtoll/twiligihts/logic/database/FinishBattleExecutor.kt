package aldtoll.twiligihts.logic.database

import aldtoll.twiligihts.FCMHelper
import aldtoll.twiligihts.logic.database.DatabaseInteractor.Companion.PREFIX
import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.GoToFinishScreenInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import aldtoll.twiligihts.storage.hero.HeroInteractor
import aldtoll.twiligihts.storage.hero.HeroStockListInteractor
import aldtoll.twiligihts.ui.screen.start_screen.StartScreen.Companion.STARTED
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
    private val heroStockListInteractor: HeroStockListInteractor
) {

    private val database = Firebase.database

    fun execute() {
        val resultReference = database.getReference("$PREFIX/Result")
        val heroHp = heroInteractor.value()?.hp ?: 0
        STARTED = false
        resultReference.setValue(
            BattleResult(
                true,
                heroHp,
                enemyInteractor.value()?.hp ?: 0,
                turnNumberInteractor.value() ?: 0,
                attemptCounterInteractor.value() ?: 0,
                heroStockListInteractor.value() ?: arrayListOf(),
                false
            )
        )
        val logReference = database.getReference("$PREFIX/Log")
        logReference.setValue(
            logListInteractor.value()?.map {
                it.message
            }
        )
        goToFinishScreenInteractor.update(Pair(false, false))
        FCMHelper.sendPushNotification("Бой с $PREFIX закончен", "Здоровье героя $heroHp")
    }
}