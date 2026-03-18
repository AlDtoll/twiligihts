package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.TimePerk
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroTimePerksInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<TimePerk>>()
    var startData = ArrayList<TimePerk>()

    fun update(list: ArrayList<TimePerk>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value

    fun init() {
        val list = ArrayList(startData.map { state -> state.copy() })
        update(list)
    }
}

