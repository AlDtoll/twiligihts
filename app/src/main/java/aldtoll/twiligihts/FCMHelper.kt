package aldtoll.twiligihts

import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


object FCMHelper {
    private const val FCM_ENDPOINT = "https://fcm.googleapis.com/fcm/send"
    private const val SERVER_KEY =
        "AAAAIn089yE:APA91bE6IQpTES-S0Uu2_lkvOpJsihDacHN0JdF8JGjWnN9cPrT8f2k3KesXl7DzKde5TE9GDZ4sKqCS5yj8ziJ8ohKFtjlByErnnA3dv_roFwciaaIFI0JqDry1sykUkSvn3gEEfvOE"

    fun sendPushNotification(deviceToken: String, title: String, body: String) {
        val client = OkHttpClient()

        // Construct the JSON payload
        val json =
            "{\"to\":\"$deviceToken\",\"notification\":{\"title\":\"$title\",\"body\":\"$body\"}}"
        val requestBody: RequestBody =
            json.toRequestBody("application/json".toMediaTypeOrNull())

        // Build the request
        val request: Request = Request.Builder()
            .url(FCM_ENDPOINT)
            .post(requestBody)
            .addHeader("Authorization", "key=$SERVER_KEY")
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
    }
}