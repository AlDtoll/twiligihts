package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Perk
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AfterPerkInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Perk>()

    fun update(item: Perk) {
        liveData.value = item
    }

    fun get() = liveData
}