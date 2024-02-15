package at.htlhl.chatnet.util.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun changeFriendStateForPerson(
    userID: String, personID: String, status: String
) {
    val fieldUpdates = mapOf(
        "status" to status, "id" to userID
    )
    FirebaseFirestore.getInstance().collection("users/${personID}/friends").document(userID)
        .set(fieldUpdates, SetOptions.mergeFields("status", "id"))
        .addOnFailureListener { exception ->
            exception.printStackTrace()
        }
}