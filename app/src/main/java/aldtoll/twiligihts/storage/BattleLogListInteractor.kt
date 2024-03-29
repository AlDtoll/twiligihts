package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.BattleEvent
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleLogListInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<BattleEvent>>()

    fun update(list: ArrayList<BattleEvent>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value

    fun add(event: BattleEvent) {
        addNewEvent(event)
    }

    fun add(message: String, gemType: Int = 0) {
        val event = BattleEvent(message, gemType = gemType)
        addNewEvent(event)
    }

    private fun addNewEvent(event: BattleEvent) {
        val newList = arrayListOf<BattleEvent>()
        val value = liveData.value
        value?.run {
            newList.addAll(this)
        }
        newList.add(event)
        update(newList)
    }
}