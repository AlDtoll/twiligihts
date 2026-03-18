package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.TimePerk
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyTimePerksInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<TimePerk>>()
    var startData = ArrayList<TimePerk>()

    fun update(list: ArrayList<TimePerk>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value

    fun init() {
        val list = ArrayList(
            startData.map { timePerk ->
                val perk = timePerk.perk
                val normalizedPerk = if (perk.charges != null && perk.currentCharges == null) {
                    perk.copy(currentCharges = perk.charges)
                } else {
                    perk.copy()
                }
                timePerk.copy(perk = normalizedPerk)
            }
        )
        update(list)
    }
}

