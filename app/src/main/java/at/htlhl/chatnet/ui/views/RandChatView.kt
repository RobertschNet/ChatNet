package at.htlhl.chatnet.ui.views

import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class RandChatView {

    private val matchedUser = mutableStateOf(FirebaseUsers())

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun RandChatScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val robert = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (matchedUser.value.id != "") {
                Log.println(Log.INFO, "RandChat", sharedViewModel.matchedUser.value.id)
                robert.value = true
            } else {
                getRandChat(sharedViewModel, false)
            }
        }
        if (!robert.value) {
            LoadingAnimation()
        }
    }


    private val handler = Handler()
    private val delayMillis = 5000L

    private fun getRandChat(sharedViewModel: SharedViewModel, state: Boolean) {
        val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
        val client = OkHttpClient()
        val requestData =
            "{\"user\":\"${sharedViewModel.auth.currentUser?.uid.toString()}\", \"newUser\":${state}}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestData.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.println(Log.INFO, "Response", responseBody ?: "")
                    // Response is null, so schedule a retry after 5 seconds
                    if (responseBody == "{\"partner\":null}") {
                        handler.postDelayed({
                            getRandChat(sharedViewModel, false)
                        }, delayMillis)
                    } else {
                        val partner =
                            responseBody?.substringAfter("partner\":\"")?.substringBefore("\"")
                        if (partner != null) {
                            fetchUser(partner)
                        }
                    }
                } else {
                    Log.println(Log.INFO, "Response", response.toString())
                }
            }
        })
    }

    fun fetchUser(uID: String) {
        FirebaseFirestore.getInstance().collection("users").document(uID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(FirebaseUsers::class.java)
                    matchedUser.value = personList!!
                }
            }
    }

    @Composable
    fun LoadingAnimation(
        indicatorSize: Dp = 100.dp,
        circleColors: List<Color> = listOf(
            Color(0xFF5851D8),
            Color(0xFF833AB4),
            Color(0xFFC13584),
            Color(0xFFE1306C),
            Color(0xFFFD1D1D),
            Color(0xFFF56040),
            Color(0xFFF77737),
            Color(0xFFFCAF45),
            Color(0xFFFFDC80),
            Color(0xFF5851D8)
        ),
        animationDuration: Int = 360
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val rotateAnimation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = animationDuration,
                    easing = LinearEasing
                )
            ), label = ""
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(size = indicatorSize)
                    .rotate(degrees = rotateAnimation)
                    .border(
                        width = 4.dp,
                        brush = Brush.sweepGradient(circleColors),
                        shape = CircleShape
                    ),
                progress = 1f,
                strokeWidth = 1.dp,
                color = Color.Black
            )
        }
    }
}