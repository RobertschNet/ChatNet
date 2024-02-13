package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun updateMarkAsUnreadStatus(
    userData: FirebaseUser,
    friendData: InternalChatInstance,
    isAlreadyUnread: Boolean
) {
    val chatRef = FirebaseFirestore.getInstance().collection("chats")
        .document(friendData.chatRoomID)

    val updateData = if (isAlreadyUnread) {
        mapOf("unread" to FieldValue.arrayRemove(userData.id))
    } else {
        mapOf("unread" to FieldValue.arrayUnion(userData.id))
    }

    try {
        withContext(Dispatchers.IO) {
            chatRef.update(updateData).await()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}