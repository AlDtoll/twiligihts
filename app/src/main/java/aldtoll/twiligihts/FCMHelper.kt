package aldtoll.twiligihts

import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


object FCMHelper {
    private const val FCM_ENDPOINT =
        "https://fcm.googleapis.com/v1/projects/twilights-53442/messages:send"
    private const val SERVER_KEY = BuildConfig.FIREBASE_SERVER_KEY

    fun sendPushNotification(title: String, body: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = getAccessTokenFromRaw()
                Log.d("APP", "token = $token")
                val client = OkHttpClient()
                val jsonBody = JSONObject()
                val messageObject = JSONObject()
                val notificationObject = JSONObject()

                notificationObject.put("title", title)
                notificationObject.put("body", body)
                messageObject.put("token", App.MASTER_TOKEN)
                messageObject.put("notification", notificationObject)
                jsonBody.put("message", messageObject)

                val requestBody: RequestBody =
                    jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

                // Build the request
                val request: Request = Request.Builder()
                    .url(FCM_ENDPOINT)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                // Execute the request asynchronously
                client.newCall(request).enqueue(object : Callback {

                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        // Handle the response if needed
                        val responseData: String = response.body?.string() ?: ""
                        println("FCM Response: $responseData")
                    }
                })
            } catch (e: Exception) {
                Log.d("FCM", e.message.toString())
            }
        }

    }

    private suspend fun getAccessTokenFromRaw(): String = withContext(Dispatchers.IO) {
        val inputStream = App.instance.resources.openRawResource(R.raw.service_account)
        val googleCredentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(
                listOf(
                    "https://www.googleapis.com/auth/firebase.messaging"
                )
            )
        googleCredentials.refreshIfExpired()
        return@withContext googleCredentials.accessToken.tokenValue
    }
}