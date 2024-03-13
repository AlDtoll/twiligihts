package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.Status
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyStatusesInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Status>>()
    var startData = ArrayList<Status>()

    fun update(list: ArrayList<Status>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        val list = ArrayList(startData.map { status -> status.copy() })
        update(list)
    }
}