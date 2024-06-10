package aldtoll.twiligihts.storage.common

import aldtoll.twiligihts.SingleLiveEvent
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteMessageInteractor @Inject constructor() {

    private val liveData = SingleLiveEvent<RemoteMessage>()

    fun update(item: RemoteMessage) {
        liveData.value = item
    }

    fun get() = liveData
}