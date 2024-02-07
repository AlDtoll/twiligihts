package aldtoll.twiligihts.storage

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttemptCounterInteractor @Inject constructor() {

    private val liveData = MutableLiveData(0)

    fun update(item: Int) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        update(0)
    }

    fun increment() {
        if (liveData.value == null) {
            update(1)
        }
        liveData.value?.run {
            val value = this + 1
            update(value)
        }
    }
}