package aldtoll.twiligihts.ui.screen.final_screen

import aldtoll.twiligihts.logic.FillEnemyExecutor
import aldtoll.twiligihts.logic.FillHeroExecutor
import aldtoll.twiligihts.logic.database.FinishBattleExecutor
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleLogListInteractor
import aldtoll.twiligihts.storage.TurnNumberInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FinalScreenViewModel @Inject constructor(
    private val fillHeroExecutor: FillHeroExecutor,
    private val fillEnemyExecutor: FillEnemyExecutor,
    private val turnNumberInteractor: TurnNumberInteractor,
    private val attemptCounterInteractor: AttemptCounterInteractor,
    private val finishBattleExecutor: FinishBattleExecutor,
    private val logListInteractor: BattleLogListInteractor,
) : ViewModel() {

    fun reinit() {
        fillHeroExecutor.execute()
        fillEnemyExecutor.execute()
        turnNumberInteractor.init()
        attemptCounterInteractor.init()
    }

    fun finishBattle() {
        finishBattleExecutor.execute()
    }

    fun getEvents(): ArrayList<BattleEvent>? {
        return logListInteractor.value()
    }
}