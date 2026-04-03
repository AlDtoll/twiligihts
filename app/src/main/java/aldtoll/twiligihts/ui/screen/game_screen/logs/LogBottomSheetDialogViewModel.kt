package aldtoll.twiligihts.ui.screen.game_screen.logs

import aldtoll.twiligihts.domain.usecase.battlelog.GetBattleLogUseCase
import aldtoll.twiligihts.model.BattleEvent
import aldtoll.twiligihts.storage.BattleLogUiSettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LogBottomSheetDialogViewModel @Inject constructor(
    getBattleLogUseCase: GetBattleLogUseCase,
    battleLogUiSettings: BattleLogUiSettings,
) : ViewModel() {

    /**
     * StateFlow с логами боя (с учётом «скрыть технические»).
     */
    val battleLog: StateFlow<List<BattleEvent>> = combine(
        getBattleLogUseCase(),
        battleLogUiSettings.hideTechnicalLogs
    ) { logs, hideTechnical ->
        if (hideTechnical) logs.filterNot { it.isTechnical } else logs
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}