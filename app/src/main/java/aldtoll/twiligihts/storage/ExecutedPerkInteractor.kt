package aldtoll.twiligihts.storage

import aldtoll.twiligihts.model.ExecutedPerk
import aldtoll.twiligihts.model.Hand
import aldtoll.twiligihts.model.Perk.Companion.EMPTY_PERK
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecutedPerkInteractor @Inject constructor() {

    private val liveData = MutableLiveData<ExecutedPerk>()

    fun update(item: ExecutedPerk) {
        liveData.value = item
    }

    fun stopRunning() {
        update(ExecutedPerk(EMPTY_PERK, Hand()))
    }

    fun get() = liveData

    fun value() = liveData.value
}