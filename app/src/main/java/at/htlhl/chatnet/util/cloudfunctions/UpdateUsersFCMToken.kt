package at.htlhl.chatnet.util.cloudfunctions

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

fun updateUsersFCMToken(
    auth: FirebaseAuth, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}
) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val url = "https://settoken-ie4mphraqq-uc.a.run.app/setToken"
            val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()

            val requestBody = FormBody.Builder().add("token", task.result)
                .add("uid", auth.currentUser?.uid.toString()).build()

            val request = Request.Builder().url(url).post(requestBody).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure()
                }

                override fun onResponse(call: Call, response: Response) {
                    onSuccess()

                }
            })
        } else {
            onFailure()
        }
    }
}