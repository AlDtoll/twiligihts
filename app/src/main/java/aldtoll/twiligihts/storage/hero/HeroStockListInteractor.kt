package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.BattleSettings.Companion.GOD_MODE
import aldtoll.twiligihts.model.Stock
import aldtoll.twiligihts.storage.IStocks
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStockListInteractor @Inject constructor() : IStocks {

    private val liveData = MutableLiveData<ArrayList<Stock>>()
    var startedValue = ArrayList<Stock>()

    /** Снимок очков на начало хода героя (для подсчёта очков за ход) */
    private var stocksAtTurnStart: ArrayList<Stock>? = null

    override fun update(list: ArrayList<Stock>) {
        liveData.postValue(list)
    }

    fun get() = liveData

    override fun value() = liveData.value

    /** Сохранить текущие очки как «на начало хода» (вызывать при старте хода героя) */
    fun saveTurnStartSnapshot() {
        stocksAtTurnStart =
            value()?.map { Stock(it.value, it.gemType, it.maxValue) }?.let { ArrayList(it) }
    }

    /** Сохранить переданный список как снимок на начало хода (для init, когда value() ещё не обновлён) */
    fun saveTurnStartSnapshot(stocks: ArrayList<Stock>) {
        stocksAtTurnStart = ArrayList(stocks.map { Stock(it.value, it.gemType, it.maxValue) })
    }

    /** Очки на начало текущего хода (копия), для расчёта прироста за ход */
    fun getStocksAtTurnStart(): List<Stock>? = stocksAtTurnStart

    fun init() {
        val stocks = ArrayList(startedValue.map { stock -> stock.copy() })
        if (GOD_MODE) {
            stocks.forEach {
                it.value = 50000
            }
        }
        update(stocks)
        saveTurnStartSnapshot(stocks)
    }

}