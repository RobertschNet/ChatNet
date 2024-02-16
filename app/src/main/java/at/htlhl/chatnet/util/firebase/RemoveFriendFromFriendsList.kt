package at.htlhl.chatnet.util.firebase

import android.service.autofill.UserData
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.firebase.firestore.FirebaseFirestore

fun removeFriendFromFriendsList(
    userData: FirebaseUser,
    friendData: FirebaseUser,
    onSuccess: () -> Unit,
    onFailure: () -> Unit={}
) {
    val friendSubCollectionRef =
       FirebaseFirestore.getInstance().collection("users").document(userData.id).collection("friends")
    friendSubCollectionRef.document(friendData.id).delete().addOnSuccessListener {
        val userSubCollectionRef =
          FirebaseFirestore.getInstance().collection("users").document(friendData.id).collection("friends")
        userSubCollectionRef.document(userData.id).delete().addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { _ ->
            onFailure()
        }
    }.addOnFailureListener { _ ->
        onFailure()
    }
}