package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.State
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStatesInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<State>>()
    var startData = ArrayList<State>()

    fun update(list: ArrayList<State>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        val list = ArrayList(startData.map { state -> state.copy() })
        update(list)
    }
}