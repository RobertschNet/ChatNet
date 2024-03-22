package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.FirebaseFirestore

fun changeTypingStatus(
    userId: String,
    isTyping: Boolean,
    chatRoomId: String,
    onSuccess: () -> Unit = {},
    onFailure: () -> Unit = {}
) {
    val userRef = FirebaseFirestore.getInstance().collection("users")
        .document(userId)
    userRef.update("typing", if (isTyping) chatRoomId else "")
        .addOnSuccessListener {
            onSuccess.invoke()
        }
        .addOnFailureListener { _ ->
            onFailure.invoke()
        }
}