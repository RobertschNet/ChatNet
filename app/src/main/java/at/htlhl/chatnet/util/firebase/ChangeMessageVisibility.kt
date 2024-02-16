package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

fun changeMessageVisibility(
    userID: String, chatRoomID: String, messageId: String
) {
    FirebaseFirestore.getInstance().collection("chats").document(chatRoomID).collection("/messages")
        .document(messageId).update(
            "visible", FieldValue.arrayRemove(userID)
        ).addOnSuccessListener {}.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
}