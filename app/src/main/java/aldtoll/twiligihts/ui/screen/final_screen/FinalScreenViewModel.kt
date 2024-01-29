package aldtoll.twiligihts.ui.screen.final_screen

import aldtoll.twiligihts.logic.FillEnemyExecutor
import aldtoll.twiligihts.logic.FillHeroExecutor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FinalScreenViewModel @Inject constructor(
    private val fillHeroExecutor: FillHeroExecutor,
    private val fillEnemyExecutor: FillEnemyExecutor
) : ViewModel() {

    fun reinit() {
        fillHeroExecutor.execute()
        fillEnemyExecutor.execute()
    }
}