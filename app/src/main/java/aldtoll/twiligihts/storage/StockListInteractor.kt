package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Stock
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockListInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Stock>>()
    var startedValue = ArrayList<Stock>()

    fun update(list: ArrayList<Stock>) {
        liveData.postValue(list)
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        update(ArrayList(startedValue.map { stock -> stock.copy() }))
    }

}