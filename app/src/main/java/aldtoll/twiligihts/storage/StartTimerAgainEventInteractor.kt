package aldtoll.twiligihts.storage

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartTimerAgainEventInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Unit>()

    fun update(item: Unit) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value
}