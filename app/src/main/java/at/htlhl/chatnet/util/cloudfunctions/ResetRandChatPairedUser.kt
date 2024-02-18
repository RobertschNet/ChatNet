package at.htlhl.chatnet.util.cloudfunctions

import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

fun resetRandChatPairedUser(
    auth: FirebaseAuth,
    onSuccess: () -> Unit = {},
    onFailure: () -> Unit = {}
) {
    val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
    val client = OkHttpClient()
    val requestData =
        "{\"user\":\"${auth.currentUser?.uid.toString()}\",\"newUser\":${false},\"action\":\"disconnect\"}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = requestData.toRequestBody(mediaType)
    val request = Request.Builder().url(url).post(requestBody).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onFailure()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                onSuccess()
            } else {
                onFailure()
            }
        }
    })
}