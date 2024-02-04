package aldtoll.twiligihts.storage

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TurnNumberInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Int>(1)

    fun update(item: Int) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value
    fun init() {
        update(1)
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