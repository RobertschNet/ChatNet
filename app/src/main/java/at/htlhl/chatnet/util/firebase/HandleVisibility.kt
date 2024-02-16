package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot

fun handleVisibility(
    userID: String,
    userContext: Boolean,
    chatRef: CollectionReference, document: DocumentSnapshot
) {
    val sender = document.get("visible") as List<*>
    if (userID in sender) {
        val updatedVisible = sender.toMutableList()
        if (userContext) {
            updatedVisible.remove(userID)
        } else {
            chatRef.document(document.id).delete().addOnSuccessListener {}
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            return
        }
        chatRef.document(document.id).update("visible", updatedVisible).addOnSuccessListener {}
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}