package at.htlhl.chatnet.util.cloudfunctions

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.NavController
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
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
    sharedViewModel: SharedViewModel,
) {
    val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
    val client = OkHttpClient()
    val requestData = "{\"user\":\"${userID}\", \"newUser\":${requestState}}"
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = requestData.toRequestBody(mediaType)

    val request = Request.Builder().url(url).post(requestBody).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody == "{\"partner\":null}" && navController.currentDestination?.route == Screens.RandChatScreen.route) {
                   Log.println(Log.INFO, "RandChat", "No partner found, retrying in 5 seconds")
                    handler.postDelayed(
                        {
                            requestRandChatPairingPartner(
                                userID = userID, requestState = false, navController = navController,sharedViewModel = sharedViewModel
                            )
                        }, delayMillis
                    )
                } else {
                    val chatID= responseBody?.substringAfter("chatID\":\"")?.substringBefore("\"")
                    val partner = responseBody?.substringAfter("partner\":\"")?.substringBefore("\"")
                    Log.println(Log.INFO, "RandChat", "Partner found: $partner")
                    sharedViewModel.fetchRandChatPairedUser(
                        partnerID = partner!!,
                        chatID = chatID!!
                    )

                }
            }
        }
    })
}