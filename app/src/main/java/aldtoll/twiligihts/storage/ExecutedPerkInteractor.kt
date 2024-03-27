package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.Perk
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecutedPerkInteractor @Inject constructor() {

    private val liveData = MutableLiveData<Pair<Perk, Int>>()

    fun update(item: Pair<Perk, Int>) {
        liveData.value = item
    }

    fun get() = liveData
}