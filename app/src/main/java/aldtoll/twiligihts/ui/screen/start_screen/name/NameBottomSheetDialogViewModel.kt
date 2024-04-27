package aldtoll.twiligihts.ui.screen.start_screen.name

import aldtoll.twiligihts.logic.database.DatabaseInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NameBottomSheetDialogViewModel @Inject constructor(
    private val databaseInteractor: DatabaseInteractor
) : ViewModel() {

    fun changePrefixAndLoadNewData(name: String) {
        databaseInteractor.observeRealtimeDatabase(name)
    }
}