package aldtoll.twiligihts.ui.screen.game_screen.logs

import aldtoll.twiligihts.domain.usecase.battlelog.GetBattleLogUseCase
import aldtoll.twiligihts.model.BattleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LogBottomSheetDialogViewModel @Inject constructor(
    getBattleLogUseCase: GetBattleLogUseCase,
) : ViewModel() {

    /**
     * StateFlow с логами боя
     */
    val battleLog: StateFlow<List<BattleEvent>> = getBattleLogUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}