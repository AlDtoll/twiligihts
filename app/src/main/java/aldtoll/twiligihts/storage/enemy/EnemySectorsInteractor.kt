package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.Sector
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemySectorsInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Sector>>()
    var startData = ArrayList<Sector>()

    fun update(list: ArrayList<Sector>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        val list = ArrayList(startData.map { sector -> sector.copy() })
        update(list)
    }
}

