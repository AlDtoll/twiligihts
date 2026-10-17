package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.TimePerk
import aldtoll.twiligihts.storage.snap
import aldtoll.twiligihts.storage.snapList
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyTimePerksInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<TimePerk>>()
    var startData = ArrayList<TimePerk>()

    /** рабочий список: его меняет логика, наблюдатели получают снимок */
    private var working: ArrayList<TimePerk>? = null

    fun update(list: ArrayList<TimePerk>) {
        working = list
        liveData.value = list.snapList { it.snap() }
    }

    fun get() = liveData

    fun value() = working

    fun init() {
        val list = ArrayList(
            startData.map { timePerk ->
                val perk = timePerk.perk
                val normalizedPerk = if (perk.charges != null && perk.currentCharges == null) {
                    perk.copy(currentCharges = perk.charges)
                } else {
                    perk.copy()
                }
                normalizedPerk.initEffects()
                timePerk.copy(perk = normalizedPerk)
            }
        )
        update(list)
    }
}

