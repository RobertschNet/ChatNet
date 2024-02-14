package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.CurrentTab
import com.google.firebase.firestore.FirebaseFirestore

fun saveChatRoom(
    userID: String, friendID: String, tab: CurrentTab, onChatCreated: (String) -> Unit = {}
) {
    val membersArray = arrayListOf(userID, friendID)
    val fieldUpdates = hashMapOf(
        "members" to membersArray,
        "tab" to tab.name.lowercase(),
        "unread" to emptyList<String>(),
    )

    val chatDocumentRef = FirebaseFirestore.getInstance().collection("chats").document()

    chatDocumentRef.set(fieldUpdates).addOnSuccessListener {
        onChatCreated(chatDocumentRef.id)
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
    }
}