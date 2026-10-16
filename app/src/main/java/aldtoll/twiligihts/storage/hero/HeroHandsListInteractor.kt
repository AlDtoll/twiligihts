package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.storage.snap
import aldtoll.twiligihts.storage.snapList
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroHandsListInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Hand>>()
    var startData = ArrayList<Hand>()

    /** рабочий список: его меняет логика, наблюдатели получают снимок */
    private var working: ArrayList<Hand>? = null

    fun update(list: ArrayList<Hand>) {
        working = list
        liveData.value = list.snapList { it.snap() }
    }

    fun get() = liveData

    fun value() = working
    fun init() {
        val list = ArrayList(startData.map { hand -> hand.copy() })
        list.forEach {
            it.init(true)
        }
        update(list)
    }
}