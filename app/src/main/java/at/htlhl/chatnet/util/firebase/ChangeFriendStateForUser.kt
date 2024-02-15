package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun changeFriendStateForUser(
    userID: String, personID: String, status: String
) {
    val fieldUpdates = mapOf(
        "status" to status,
        "id" to personID,
    )
    FirebaseFirestore.getInstance().collection("users/${userID}/friends").document(personID)
        .set(fieldUpdates, SetOptions.mergeFields("status", "id"))
        .addOnFailureListener { exception -> exception.printStackTrace() }
}