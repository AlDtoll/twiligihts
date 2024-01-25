package aldtoll.twiligihts.ui.screen.start_screen

import aldtoll.twiligihts.storage.BattleResultInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject constructor(
    private val battleResultInteractor: BattleResultInteractor
) : ViewModel() {

    fun resultData() = battleResultInteractor.get()
}