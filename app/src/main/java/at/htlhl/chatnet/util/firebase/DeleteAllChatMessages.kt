package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FirebaseFirestore

fun deleteAllChatMessages(
    userData: FirebaseUser,
    friendData: InternalChatInstance,
) {
    val chatRef =
        FirebaseFirestore.getInstance().collection("chats").document(friendData.chatRoomID)
            .collection("messages")
    chatRef.get().addOnSuccessListener { querySnapshot ->
        for (document in querySnapshot.documents) {
            val sender = document.get("visible") as List<*>
            if (userData.id in sender) {
                val updatedVisible = sender.toMutableList()
                updatedVisible.remove(userData.id)

                chatRef.document(document.id).update("visible", updatedVisible)
                    .addOnSuccessListener {
                        // Successfully removed the entry from the "visible" array
                    }.addOnFailureListener { exception ->
                        exception.printStackTrace()
                        // Handle the failure here
                    }
            }
        }
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
        // Handle the failure here
    }


}