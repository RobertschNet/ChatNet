package at.htlhl.chatnet.util.cloudfunctions

import at.htlhl.chatnet.data.FirebaseMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

fun sendPushNotificationToPartner(
    userID: String,
    friendID: String,
    message: FirebaseMessage) {
    val url = "https://sendnotification-ie4mphraqq-uc.a.run.app/sendNotifications"
    val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()

    val requestBody = FormBody.Builder().add("uid",userID)
        .add("other", friendID).add("content", message.text).build()

    val request = Request.Builder().url(url).post(requestBody).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
        }

        override fun onResponse(call: Call, response: Response) {
        }
    })
}