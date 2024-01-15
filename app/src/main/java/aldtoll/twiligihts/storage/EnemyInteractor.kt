package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Enemy
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnemyInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Enemy>()

    fun update(item: Enemy) {
        liveData.postValue(item)
    }

    fun get() = liveData

    fun value() = liveData.value
}