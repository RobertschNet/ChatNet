package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FirebaseFirestore

fun changeMediaVisibility(
    userID: String,
    friendData: InternalChatInstance,
    userContext: Boolean,
    isMedia: Boolean
) {
    val chatRef =
        FirebaseFirestore.getInstance().collection("users").document(friendData.chatRoomID)
            .collection("messages")
    chatRef.get().addOnSuccessListener { querySnapshot ->
        for (document in querySnapshot.documents) {
            if (isMedia) {
                val images = document.get("images") as List<*>?
                if (images != null && images.isNotEmpty()) {
                    handleVisibility(
                        userID = userID,
                        userContext = userContext,
                        chatRef = chatRef,
                        document = document
                    )
                }
            } else {
                val text = document.getString("text")
                if (!text.isNullOrBlank()) {
                    handleVisibility(
                        userID = userID,
                        userContext = userContext,
                        chatRef = chatRef,
                        document = document
                    )
                }
            }
        }
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
    }
}