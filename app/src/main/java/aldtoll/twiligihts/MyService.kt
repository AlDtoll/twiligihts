package aldtoll.twiligihts

import aldtoll.twiligihts.storage.common.RemoteMessageInteractor
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * сервис для обработки пушей
 */
@AndroidEntryPoint
class MyService : FirebaseMessagingService() {

    @Inject
    lateinit var remoteMessageInteractor: RemoteMessageInteractor

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

//    /**
//     * метод вызывается когда приходит пуш
//     */
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        if (remoteMessage.data.isNotEmpty()) {
//            Log.d(MYTAG, "Message data payload: ${remoteMessage.data}")
//
//            if (/* Check if data needs to be processed by long running job */ false) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
//            } else {
//                // Handle message within 10 seconds
//                handleNow(remoteMessage)
//            }
//        }
//    }
//
//    private fun handleNow(remoteMessage: RemoteMessage) {
//        val data = remoteMessage.data
//        /**
//         * если в в пуше пришел заголовок, то значит, что нужно показать его в трее
//         */
//        if (data.containsKey("title")) {
//            val title = data["title"]
//            val body = data["body"]
//            sendNotification(title, body)
//        }
//        CoroutineScope(Dispatchers.Main).launch {
//            remoteMessageInteractor.update(remoteMessage)
//        }
//    }
//
//    private fun scheduleJob() {
//    }
//
//    private fun sendNotification(title: String?, message: String?) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
//        )
//        val channelId = "default_channel"
//        val notificationBuilder: NotificationCompat.Builder =
//            NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.drawable.ic_delete)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//
//        // Создаем канал для Android O и выше
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Приглашения к игре",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//        notificationManager.notify(0, notificationBuilder.build())
//    }
}