package aldtoll.twiligihts.storage

import aldtoll.twiligihts.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostEnemyTurnLogEventInteractor @Inject constructor() {

    private val liveData = SingleLiveEvent<Unit>()

    fun update(item: Unit) {
        liveData.value = item
    }

    fun get() = liveData
}
