package aldtoll.twiligihts.storage

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSecondsInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Int>(0)

    fun update(item: Int) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value
}