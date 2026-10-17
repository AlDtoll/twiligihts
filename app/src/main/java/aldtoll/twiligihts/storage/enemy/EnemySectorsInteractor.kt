package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.Sector
import aldtoll.twiligihts.storage.snap
import aldtoll.twiligihts.storage.snapList
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemySectorsInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Sector>>()
    var startData = ArrayList<Sector>()

    /** рабочий список: его меняет логика, наблюдатели получают снимок */
    private var working: ArrayList<Sector>? = null

    fun update(list: ArrayList<Sector>) {
        working = list
        liveData.value = list.snapList { it.snap() }
    }

    fun get() = liveData

    fun value() = working
    fun init() {
        val list = ArrayList(startData.map { sector -> sector.copy() })
        update(list)
    }
}

