package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

fun updatePinChatStatus(
    userData: FirebaseUser, friend: InternalChatInstance, isAlreadyPinned: Boolean
) {
    val userRef = FirebaseFirestore.getInstance().collection("users").document(userData.id)
    val updateData = if (isAlreadyPinned) {
        mapOf("pinned" to FieldValue.arrayRemove(friend.chatRoomID))
    } else {
        mapOf("pinned" to FieldValue.arrayUnion(friend.chatRoomID))
    }
    userRef.update(updateData).addOnSuccessListener {}
        .addOnFailureListener { exception -> exception.printStackTrace() }


}