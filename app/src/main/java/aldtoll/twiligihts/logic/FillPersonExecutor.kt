package aldtoll.twiligihts.logic

import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.StockListInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FillPersonExecutor @Inject constructor(
    private val stockListInteractor: StockListInteractor
) {

    fun execute() {
        val list = arrayListOf<Stock>()
        list.add(Stock(0, 1))
        list.add(Stock(0, 2))
        list.add(Stock(0, 3))
        list.add(Stock(0, 4))
        stockListInteractor.update(list)
    }
}