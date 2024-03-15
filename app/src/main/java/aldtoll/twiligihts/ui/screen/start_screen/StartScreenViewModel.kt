package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.storage.AttemptCounterInteractor
import aldtoll.twiligihts.storage.BattleResultInteractor
import aldtoll.twiligihts.storage.BattleSettingsInteractor
import aldtoll.twiligihts.storage.enemy.EnemyInteractor
import androidx.lifecycle.ViewModel
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
}