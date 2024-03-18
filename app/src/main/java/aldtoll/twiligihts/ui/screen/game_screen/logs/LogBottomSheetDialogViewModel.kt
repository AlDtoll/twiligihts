package aldtoll.twiligihts.ui.screen.game_screen.logs

import aldtoll.twiligihts.storage.BattleLogListInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogBottomSheetDialogViewModel @Inject constructor(
    private val battleLogListInteractor: BattleLogListInteractor,
) : ViewModel() {

    fun logData() = battleLogListInteractor.get()
}