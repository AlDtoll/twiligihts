package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.Stock
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStockListInteractor @Inject constructor() {

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