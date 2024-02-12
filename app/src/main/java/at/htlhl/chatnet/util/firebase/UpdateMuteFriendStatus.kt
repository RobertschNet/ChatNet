package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

fun updateMuteFriendStatus(
    userData: FirebaseUser, friendData: FirebaseUser, isAlreadyMuted: Boolean
) {
    val userRef = FirebaseFirestore.getInstance().collection("users").document(userData.id)
    val updateData = if (isAlreadyMuted) {
        mapOf("muted" to FieldValue.arrayRemove(friendData.id))
    } else {
        mapOf("muted" to FieldValue.arrayUnion(friendData.id))
    }
    userRef.update(updateData).addOnSuccessListener {}.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
}