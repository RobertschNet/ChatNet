package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.CurrentTab
import com.google.firebase.firestore.FirebaseFirestore

fun updateChatRoomTab(
    newTab: CurrentTab,
    chatRoomId: String
) {
    val fieldUpdates = hashMapOf<String, Any>(
        "tab" to newTab,
    )
    FirebaseFirestore.getInstance().collection("chats").document(chatRoomId).update(fieldUpdates).addOnSuccessListener { }
        .addOnFailureListener { exception -> exception.printStackTrace() }
}