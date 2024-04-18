package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.Resource
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroResourcesInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Resource>>()
    var startData = ArrayList<Resource>()

    fun update(list: ArrayList<Resource>) {
        liveData.value = list
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        val list = ArrayList(startData.map { resource -> resource.copy() })
        update(list)
    }

    fun refresh() {
        update(liveData.value ?: arrayListOf())
    }
}