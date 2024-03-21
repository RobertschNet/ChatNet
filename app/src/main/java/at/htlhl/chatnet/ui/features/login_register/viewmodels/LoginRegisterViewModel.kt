package at.htlhl.chatnet.ui.features.login_register.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class LoginRegisterViewModel : ViewModel() {
    val auth: FirebaseAuth = Firebase.auth

    fun createUserEntry(
        name: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {},
    ) {
        val userRef = FirebaseFirestore.getInstance().collection("users")
            .document(auth.currentUser?.uid.toString())
        val userData = hashMapOf(
            "blocked" to emptyList<String>(),
            "pinned" to emptyList<String>(),
            "tags" to emptyList<String>(),
            "color" to "blue",
            "connected" to false,
            "muted" to emptyList<String>(),
            "email" to auth.currentUser?.email.toString(),
            "id" to auth.currentUser?.uid.toString(),
            "image" to "https://www.w3schools.com/howto/img_avatar2.png",
            "online" to true,
            "username" to mapOf(
                "lowercase" to name.lowercase(Locale.ROOT),
                "mixedcase" to name,
            ),
        )
        userRef.set(userData)
            .addOnSuccessListener {
                onSuccess.invoke()
            }
            .addOnFailureListener { _ ->
                onFailure.invoke()
            }
    }

    fun checkIfUserExists(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .limit(1)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { _ ->
                callback(false)
            }
    }

    fun checkIfEmailExists(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
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
            .addOnFailureListener { _ ->
                callback(false)
            }
    }

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
            .addOnFailureListener { _ ->
                callback(false)
            }
    }

    fun sendVerificationEmail(onComplete: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            it.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete.invoke(true)
                    } else {
                        onComplete.invoke(false)
                    }
                }
        }
    }

    fun isUserEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }
}