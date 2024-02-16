package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

fun cancelFriendRequest(
    userID: String, person: FirebaseUser
) {
    val userRef = FirebaseFirestore.getInstance().collection("users").document(userID)
    val friendRef = FirebaseFirestore.getInstance().collection("users").document(person.id)
    userRef.collection("friends").document(person.id).delete().addOnSuccessListener {
        friendRef.collection("friends").document(userID).delete().addOnSuccessListener {}
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
    }
}