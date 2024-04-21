package aldtoll.twiligihts.storage.enemy

import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.storage.IResources
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyResourcesInteractor @Inject constructor() : IResources {

    private val liveData = MutableLiveData<ArrayList<Resource>>()
    var startData = ArrayList<Resource>()

    fun update(list: ArrayList<Resource>) {
        liveData.value = list
    }

    fun get() = liveData

    override fun value() = liveData.value
    fun init() {
        val list = ArrayList(startData.map { resource -> resource.copy() })
        update(list)
    }

    override fun refresh() {
        update(liveData.value ?: arrayListOf())
    }
}