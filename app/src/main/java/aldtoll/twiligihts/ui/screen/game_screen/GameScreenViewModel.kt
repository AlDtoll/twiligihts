package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.logic.FillPersonExecutor
import aldtoll.twiligihts.model.Gem
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    private val fillPersonExecutor: FillPersonExecutor
) : ViewModel() {

    fun crushGems(removedGems: MutableList<Gem>) {
        // Get the color of the gem being removed
        val removedGemsCount = mutableMapOf<Int, Int>()
        for (gem in removedGems) {
            val removedGemColor = gem.type
            // Increment the count for the removed gem color in the map
            removedGemsCount[removedGemColor] = (removedGemsCount[removedGemColor] ?: 0) + 1
        }

    }

    fun initPerson() {
        fillPersonExecutor.execute()
    }
}