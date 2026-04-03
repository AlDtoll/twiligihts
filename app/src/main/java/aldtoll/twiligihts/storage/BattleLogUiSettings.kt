package aldtoll.twiligihts.storage

import aldtoll.twiligihts.App
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Настройки отображения боевого лога в UI (полный лог по-прежнему хранится в репозитории).
 */
@Singleton
class BattleLogUiSettings @Inject constructor() {

    private val prefs = App.getPrefs()

    private val _hideTechnicalLogs = MutableStateFlow(prefs.getBoolean(KEY_HIDE_TECHNICAL, false))
    val hideTechnicalLogs: StateFlow<Boolean> = _hideTechnicalLogs.asStateFlow()

    fun setHideTechnicalLogs(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_TECHNICAL, hide).apply()
        _hideTechnicalLogs.value = hide
    }

    companion object {
        private const val KEY_HIDE_TECHNICAL = "hide_technical_battle_logs"
    }
}
