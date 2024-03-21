package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.FirebaseFirestore

fun updateChatRoomTab(
    newTab: String,
    chatRoomId: String
) {
    val fieldUpdates = hashMapOf<String, Any>(
        "tab" to newTab.lowercase(),
    )
    FirebaseFirestore.getInstance().collection("chats").document(chatRoomId).update(fieldUpdates)
        .addOnSuccessListener { }
        .addOnFailureListener { exception -> exception.printStackTrace() }
}