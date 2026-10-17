package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.BattleSettings
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.IStocks
import aldtoll.twiligihts.storage.snap
import aldtoll.twiligihts.storage.snapList
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStockListInteractor @Inject constructor() : IStocks {

    private val liveData = MutableLiveData<ArrayList<Stock>>()
    var startedValue = ArrayList<Stock>()

    /** рабочий список: его меняет логика, наблюдатели получают снимок (доставка по-прежнему через postValue) */
    private var working: ArrayList<Stock>? = null

    override fun update(list: ArrayList<Stock>) {
        working = list
        liveData.postValue(list.snapList { it.snap() })
    }

    fun get() = liveData

    override fun value() = working

    /** Возвращает собранный список (value() после update() тоже актуален, это тот же список) */
    fun init(): ArrayList<Stock> {
        val stocks = ArrayList(startedValue.map { stock -> stock.copy() })
        if (BattleSettings.GOD_MODE) {
            stocks.forEach {
                it.value = 50000
            }
        }
        update(stocks)
        return stocks
    }

}