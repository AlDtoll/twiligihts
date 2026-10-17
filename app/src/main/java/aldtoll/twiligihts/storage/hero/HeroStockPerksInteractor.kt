package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.StockPerk
import aldtoll.twiligihts.storage.snap
import aldtoll.twiligihts.storage.snapList
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroStockPerksInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<StockPerk>>()
    var startData = ArrayList<StockPerk>()

    /** рабочий список: его меняет логика, наблюдатели получают снимок */
    private var working: ArrayList<StockPerk>? = null

    fun update(list: ArrayList<StockPerk>) {
        working = list
        liveData.value = list.snapList { it.snap() }
    }

    fun get() = liveData

    fun value() = working

    fun init() {
        val list = ArrayList(
            startData.map { stockPerk ->
                val perk = stockPerk.perk
                val normalizedPerk = if (perk.charges != null && perk.currentCharges == null) {
                    perk.copy(currentCharges = perk.charges)
                } else {
                    perk.copy()
                }
                normalizedPerk.initEffects()
                stockPerk.copy(perk = normalizedPerk)
            }
        )
        update(list)
    }
}

