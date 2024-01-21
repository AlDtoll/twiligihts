package aldtoll.twiligihts.ui

import aldtoll.twiligihts.logic.database.DatabaseInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val databaseInteractor: DatabaseInteractor
) : ViewModel() {

    fun observeData() {
        databaseInteractor.observeRealtimeDatabase()
    }
}