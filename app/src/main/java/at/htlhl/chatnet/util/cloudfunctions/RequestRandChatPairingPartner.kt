package at.htlhl.chatnet.util.cloudfunctions

import android.os.Handler
import android.os.Looper
import androidx.navigation.NavController
import at.htlhl.chatnet.navigation.Screens
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


private val handler = Handler(Looper.getMainLooper())
private const val delayMillis = 5000L
fun requestRandChatPairingPartner(
    userID: String,
    requestState: Boolean,
    navController: NavController,
    onSuccess: (String) -> Unit = {},
    onFailure: () -> Unit = {}
) {
    val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
    val client = OkHttpClient()
    val requestData = "{\"user\":\"${userID}\", \"newUser\":${requestState}}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = requestData.toRequestBody(mediaType)

    val request = Request.Builder().url(url).post(requestBody).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onFailure()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody == "{\"partner\":null}" && navController.currentDestination?.route == Screens.RandChatScreen.route) {
                    handler.postDelayed(
                        {
                            requestRandChatPairingPartner(
                                userID = userID, requestState = false, navController = navController
                            )
                        }, delayMillis
                    )
                } else {
                    val partner =
                        responseBody?.substringAfter("partner\":\"")?.substringBefore("\"")
                    if (partner != null) {
                        onSuccess(partner)
                    }
                }
            } else {
                onFailure()
            }
        }
    })
}