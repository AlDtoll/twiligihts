package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Hand
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * здесь хранятся действия местности (либо союзников)
 * на них не должны распространяться статусы персонажей
 */
@Singleton
class PlaceHandsListInteractor @Inject constructor() {

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
            it.init()
        }
        update(list)
    }
}