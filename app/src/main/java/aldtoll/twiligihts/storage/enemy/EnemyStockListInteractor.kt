package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.IStocks
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStockListInteractor @Inject constructor() : IStocks {

    private val liveData = MutableLiveData<ArrayList<Stock>>()
    var startedValue = ArrayList<Stock>()

    override fun update(list: ArrayList<Stock>) {
        liveData.postValue(list)
    }

    fun get() = liveData

    override fun value() = liveData.value
    fun init() {
        val stocks = ArrayList(startedValue.map { stock -> stock.copy() })
        if (BattleSettings.GOD_MODE) {
            stocks.forEach {
                it.value = 50000
            }
        }
        update(stocks)
    }

}