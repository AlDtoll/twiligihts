package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.BattleEvent
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BattleLogListInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<BattleEvent>>()

    fun update(list: ArrayList<BattleEvent>) {
        liveData.postValue(list)
    }

    fun get() = liveData

    fun value() = liveData.value
}