package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

fun updateBlockedUserList(
    userData: FirebaseUser,
    friendData: FirebaseUser,
    isAlreadyBlocked: Boolean
) {
    val userRef =    FirebaseFirestore.getInstance().collection("users").document(userData.id)
    val updateData = if (isAlreadyBlocked) {
        mapOf("blocked" to FieldValue.arrayRemove(friendData.id))
    } else {
        mapOf("blocked" to FieldValue.arrayUnion(friendData.id))
    }
    userRef.update(updateData)
        .addOnSuccessListener {}
        .addOnFailureListener { exception -> exception.printStackTrace() }
}