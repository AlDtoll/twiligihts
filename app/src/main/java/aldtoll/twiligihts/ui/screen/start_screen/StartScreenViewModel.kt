package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.logic.database.DatabaseInteractor
import aldtoll.twiligihts.model.BattleResult
import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleResultInteractor
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val battleResultInteractor: BattleResultInteractor,
    private val settingsInteractor: BattleSettingsInteractor,
    private val attemptCounterInteractor: AttemptCounterInteractor,
    private val enemyInteractor: EnemyInteractor,
) : ViewModel() {

    fun resultData() = battleResultInteractor.get()

    fun battleName() = enemyInteractor.startedValue?.name

    fun settingsData() = settingsInteractor.get()
    fun newAttempt() {
        attemptCounterInteractor.increment()
    }

    fun activateGodMode() {
        BattleSettings.GOD_MODE = !BattleSettings.GOD_MODE
    }

    fun startBattleAgain() {
        val resultReference = Firebase.database.getReference("${DatabaseInteractor.PREFIX}/Result")
        resultReference.setValue(
            BattleResult(
                false
            )
        )
    }
}