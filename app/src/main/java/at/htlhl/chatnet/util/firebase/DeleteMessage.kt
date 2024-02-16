package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.FirebaseFirestore

fun deleteMessage(
    chatRoomID: String,
    messageID: String,
) {
    FirebaseFirestore.getInstance().collection("chats").document(chatRoomID).collection("/messages")
        .document(messageID).delete().addOnSuccessListener {}.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
}