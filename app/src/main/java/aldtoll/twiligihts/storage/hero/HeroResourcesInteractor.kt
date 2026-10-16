package aldtoll.twiligihts.storage.hero

import aldtoll.twiligihts.model.Resource
import aldtoll.twiligihts.storage.IResources
import aldtoll.twiligihts.storage.snap
import aldtoll.twiligihts.storage.snapList
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroResourcesInteractor @Inject constructor() : IResources {

    private val liveData = MutableLiveData<ArrayList<Resource>>()
    var startData = ArrayList<Resource>()

    /** рабочий список: его меняет логика, наблюдатели получают снимок */
    private var working: ArrayList<Resource>? = null

    fun update(list: ArrayList<Resource>) {
        working = list
        liveData.value = list.snapList { it.snap() }
    }

    fun get() = liveData

    override fun value() = working
    fun init() {
        val list = ArrayList(startData.map { resource -> resource.copy() })
        update(list)
    }

    override fun refresh() {
        update(working ?: arrayListOf())
    }
}