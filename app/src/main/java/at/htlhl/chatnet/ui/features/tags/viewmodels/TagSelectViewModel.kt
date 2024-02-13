package at.htlhl.chatnet.ui.features.tags.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class TagSelectViewModel : ViewModel() {

    fun updateUserTagList(
        userData: FirebaseUser, tags: List<String>
    ) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userData.id)
        val updateData = mapOf("tags" to tags)
        userRef.update(updateData).addOnSuccessListener {}.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }
}