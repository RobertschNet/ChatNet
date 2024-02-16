package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun saveMessage(
    chatRoomID: String,
    message: FirebaseMessage,
    onSuccess: () -> Unit,
    onFailure: () -> Unit = {}
) {

    try {
        FirebaseFirestore.getInstance().collection("chats/${chatRoomID}/messages").add(message).await()
        onSuccess()
    } catch (e: Exception) {
        onFailure()
    }
}