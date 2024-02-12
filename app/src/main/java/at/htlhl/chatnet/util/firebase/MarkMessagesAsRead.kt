package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FirebaseFirestore

fun markMessagesAsRead(
    userData: FirebaseUser, friendData: InternalChatInstance
) {
    val chatRef =
        FirebaseFirestore.getInstance().collection("chats").document(friendData.chatRoomID)
            .collection("messages")
    chatRef.get().addOnSuccessListener { querySnapshot ->
        for (document in querySnapshot.documents) {
            val sender = document.getString("sender")
            if (sender != userData.id) {
                chatRef.document(document.id).update("read", true).addOnSuccessListener {}
                    .addOnFailureListener { exception -> exception.printStackTrace() }
            }
        }
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
    }


}