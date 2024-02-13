package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun markMessagesAsRead(userData: FirebaseUser, friendData: InternalChatInstance) {
    val chatRef = FirebaseFirestore.getInstance().collection("chats")
        .document(friendData.chatRoomID)
        .collection("messages")

    val querySnapshot = withContext(Dispatchers.IO) {
        chatRef.limit(1).get().await()
    }

    val batch = FirebaseFirestore.getInstance().batch()
    for (document in querySnapshot.documents) {
        val sender = document.getString("sender")
        if (sender != userData.id) {
            val docRef = chatRef.document(document.id)
            batch.update(docRef, "read", true)
        }
    }

    try {
        withContext(Dispatchers.IO) {
            batch.commit().await()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}