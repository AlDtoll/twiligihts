package aldtoll.twiligihts

import aldtoll.twiligihts.App.Companion.MYTAG
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(MYTAG, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage)
            }
        }
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(MYTAG, "Message Notification Body: ${it.body}")
            super.onMessageReceived(remoteMessage)
        }
    }

    private fun handleNow(remoteMessage: RemoteMessage) {

    }

    private fun scheduleJob() {
    }
}