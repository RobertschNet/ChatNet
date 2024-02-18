package at.htlhl.chatnet.util.cloudfunctions

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


fun requestChatMateResponse(
    data: String,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit = {}
) {
    val url = "https://getresponse-ie4mphraqq-uc.a.run.app/getResponse?text=$data"
    val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()
    val request = Request.Builder().url(url).get().build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onFailure()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonObject = JSONObject(responseBody ?: "")
                val resultText = jsonObject.optString("result", "")
                onSuccess(resultText)
            } else {
                onFailure()
            }
        }
    })
}