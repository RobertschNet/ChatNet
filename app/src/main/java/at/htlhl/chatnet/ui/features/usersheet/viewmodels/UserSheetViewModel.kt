package at.htlhl.chatnet.ui.features.usersheet.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.FirebaseFriend
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserSheetViewModel:ViewModel() {
    val auth: FirebaseAuth = Firebase.auth

    fun fetchFriendsFromPerson(
        friend: FirebaseUser,
        onSuccess: (List<FirebaseUser>) -> Unit
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val randomFriend = friend.id
            val friendQuerySnapshot =
                FirebaseFirestore.getInstance().collection("users").document(randomFriend).collection("friends").get()
                    .await()

            val personListData = mutableListOf<FirebaseUser>()
            val subCollectionData = friendQuerySnapshot.toObjects(FirebaseFriend::class.java)

            subCollectionData.forEach { friend ->
                try {
                    val userDocumentSnapshot =
                       FirebaseFirestore.getInstance().collection("users").document(friend.id).get().await()
                    val data = userDocumentSnapshot?.toObject(FirebaseUser::class.java)
                    if (data != null) {
                        val finalData = FirebaseUser(
                            image = data.image,
                            username = data.username,
                            id = data.id,
                            online = data.online,
                            email = data.email,
                            color = data.color,
                            blocked = data.blocked,
                            connected = data.connected,
                            pinned = data.pinned,
                            muted = data.muted,
                            statusFriend = when (friend.status) {
                                "accepted" -> {
                                    PersonType.ACCEPTED_PERSON
                                }

                                "pending" -> {
                                    PersonType.PENDING_PERSON
                                }

                                "requested" -> {
                                    PersonType.REQUESTED_PERSON
                                }

                                else -> {
                                    PersonType.EMPTY_PERSON
                                }
                            },
                            tags = data.tags
                        )
                        personListData.add(finalData)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val filteredList = personListData.filter { it.id != auth.currentUser?.uid.toString() && it.statusFriend == PersonType.ACCEPTED_PERSON }
            onSuccess(filteredList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}