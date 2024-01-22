package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Hand
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyHandsListInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ArrayList<Hand>>()

    fun update(list: ArrayList<Hand>) {
        liveData.postValue(list)
    }

    fun get() = liveData

    fun value() = liveData.value
}