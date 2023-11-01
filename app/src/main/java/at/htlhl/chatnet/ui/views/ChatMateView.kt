package at.htlhl.chatnet.ui.views

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChats
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.data.InternalChatInstances
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.firebase.Timestamp

class ChatMateView {

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun ChatMateScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        Log.println(Log.INFO, "ChatMateView", "ChatMateScreen")
        val messageChatRoomDataState = sharedViewModel.chatData.collectAsState()
        val messageChatRoomData: List<FirebaseChats> = messageChatRoomDataState.value
        val chatmateData = messageChatRoomData.find { it.tab == "chatmate" }
        var text by remember {
            mutableStateOf("")
        }

        Button(onClick = {
            sharedViewModel.friend.value =
                chatmateData?.messages?.last()?.let {
                    InternalChatInstances(
                        FirebaseUsers(
                            "https://firebasestorage.googleapis.com/v0/b/testing-ee3e4.appspot.com/o/IQ6GL7wCpRdl5jgpkpjGE8Ri2cu1%2FmessagePictures%2F1000001160?alt=media&token=12e5d4b2-0202-4c8a-b81f-890d53cc6843",
                            mapOf("mixedcase" to "ChatMate", "lowercase" to "chatmate"),
                            "",
                            "chatmate",
                            "",
                            "",
                            "",
                            false,
                            ""
                        ),
                        Timestamp.now(), it, false, 0, false
                    )
                }!!
            navController.navigate(Screens.ChatViewScreen.route)
        }) {
            Text(text = "Click me")
        }

    }
}