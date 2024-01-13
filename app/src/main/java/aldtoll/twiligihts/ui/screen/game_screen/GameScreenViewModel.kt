package aldtoll.twiligihts.ui.screen.game_screen

import aldtoll.twiligihts.logic.FillPersonExecutor
import aldtoll.twiligihts.logic.FillStockExecutor
import aldtoll.twiligihts.model.Gem
import aldtoll.twiligihts.storage.HandsListInteractor
import aldtoll.twiligihts.storage.StockListInteractor
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GameScreenViewModel @Inject constructor(
    private val fillPersonExecutor: FillPersonExecutor,
    private val stockListInteractor: StockListInteractor,
    private val handsListInteractor: HandsListInteractor,
    private val fillStockExecutor: FillStockExecutor,
) : ViewModel() {

    fun crushGems(removedGems: MutableList<Gem>) {
        fillStockExecutor.execute(removedGems)
    }

    fun initPerson() {
        fillPersonExecutor.execute()
    }

    fun stockData() = stockListInteractor.get()
    fun handsData() = handsListInteractor.get()
}