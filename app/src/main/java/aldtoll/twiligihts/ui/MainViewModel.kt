package aldtoll.twiligihts.ui

import aldtoll.twiligihts.App
import aldtoll.twiligihts.logic.database.DatabaseInteractor
import aldtoll.twiligihts.ui.screen.start_screen.StartScreen
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val databaseInteractor: DatabaseInteractor
) : ViewModel() {

    fun observeData() {
        val prefix = App.getPrefs().getString(StartScreen.NAME, "")
        databaseInteractor.observeRealtimeDatabase(prefix ?: "")
    }

    fun saveToken(token: String) {
        databaseInteractor.addToken(token)
    }

    fun changePrefixAndLoadNewData(enemyName: String) {
        databaseInteractor.observeRealtimeDatabase(enemyName)
    }
}