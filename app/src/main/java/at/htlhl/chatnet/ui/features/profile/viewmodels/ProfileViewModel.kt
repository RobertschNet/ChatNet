package at.htlhl.chatnet.ui.features.profile.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ProfileViewModel : ViewModel() {
    val auth: FirebaseAuth = Firebase.auth

    fun checkIfUsernameExists(
        name: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("username.lowercase", name.lowercase(Locale.ROOT))
            .limit(1)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val usernameField = documentSnapshot.get("username.lowercase")
                    if (usernameField is String) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                callback(false)
            }
    }

    fun logout(context: Context, googleSignInOptions: GoogleSignInOptions) {
        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        googleSignInClient.signOut()
            .addOnCompleteListener {}

    }

    fun updateUserProfilePicture(imageReference: String, onComplete: (Boolean) -> Unit) {
        val fieldUpdates = hashMapOf<String, Any>("image" to imageReference)
        FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid).update(fieldUpdates)
            .addOnSuccessListener {
                onComplete.invoke(true)
            }
            .addOnFailureListener { _ ->
                onComplete.invoke(false)
            }
    }
}