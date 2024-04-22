package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.ExecutedPerk
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecutedPerkInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ExecutedPerk>()

    fun update(item: ExecutedPerk) {
        liveData.value = item
    }

    fun get() = liveData

    fun value() = liveData.value
}