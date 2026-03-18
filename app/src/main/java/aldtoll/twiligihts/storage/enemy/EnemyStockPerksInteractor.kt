package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.StockPerk
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStockPerksInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<StockPerk>>()
    var startData = ArrayList<StockPerk>()

    fun update(list: ArrayList<StockPerk>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value

    fun init() {
        val list = ArrayList(
            startData.map { stockPerk ->
                val perk = stockPerk.perk
                val normalizedPerk = if (perk.charges != null && perk.currentCharges == null) {
                    perk.copy(currentCharges = perk.charges)
                } else {
                    perk.copy()
                }
                stockPerk.copy(perk = normalizedPerk)
            }
        )
        update(list)
    }
}

