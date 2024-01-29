package at.htlhl.chatnet.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseFriend
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.LocationUserInstance
import at.htlhl.chatnet.navigation.Screens
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Tobias Brandl.
 *
 * This class represents the shared view model, which is used to share data between different Views.
 * It also contains the logic for the communication with Firebase.
 */
class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseInstance = FirebaseFirestore.getInstance()


    private companion object {
        const val USER_COLLECTION = "users"
        const val CHATS_COLLECTION = "chats"
        const val MESSAGES_COLLECTION = "messages"
        const val FRIENDS_COLLECTION = "friends"
    }

    /**
     * This section contains some different list elements used to store specific values,
     * fetched from the Code below.
     */

    val auth: FirebaseAuth = Firebase.auth
    val chatMateResponseState = mutableStateOf(ChatMateResponseState.Success)
    private val _friend = MutableStateFlow(
        InternalChatInstance(
            FirebaseUser(
                blocked = emptyList(),
                image = "https://firebasestorage.googleapis.com/v0/b/chatnet-97f9a.appspot.com/o/images%2FDALL%C2%B7E%202023-09-17%2014.29.57%20-%20Profile%20picture%20for%20an%20AI-Asistent%2C%20digital%20art.png?alt=media&token=f41b85e7-8012-4d5d-87f0-e4bdd7f55030",
                username = mapOf("lowercase" to "chatmate", "mixedcase" to "ChatMate"),
                status = "online",
                id = "1s8vrpKw4GXO6HbaUKl3w2wdP3w2",
                email = "dick@lol.at",
                pinned = emptyList(),
                color = "",
                connected = false,
                mutedFriend = false,
                statusFriend = "accepted"
            ),
            Timestamp.now(),
            InternalMessageInstance(
                isFromCache = false,
                id = "1szRRaAM2MEYnPEsQAIX",
                sender = "E8LJ593frbcCjOPrLhXRvn1W0Du1",
                images = arrayListOf(),
                read = false,
                text = "Hey",
                timestamp = Timestamp.now(),
                visible = arrayListOf()
            ),
            false,
            0,
            false,
            "Run6JfBNdYtH3C6g2HQT"
        )
    )
    val friend: StateFlow<InternalChatInstance> get() = _friend
    var unfinishedGoogleRegistration = mutableStateOf("")
    val bottomBarState = mutableStateOf(false)
    val gpsState = mutableStateOf(false)
    val localChatUserList = mutableStateOf<List<LocationUserInstance>>(emptyList())
    val searchValue = mutableStateOf("")
    val text = mutableStateOf("")
    val randState = mutableStateOf(false)
    val isConnected = mutableStateOf(false)
    val imagePosition = mutableIntStateOf(0)
    val imageList = mutableStateOf<List<InternalMessageInstance>>(emptyList())

    fun updateFriend(newFriend: InternalChatInstance, onComplete: () -> Unit = {}) {
        _friend.value = newFriend
        onComplete.invoke()
    }

    private fun getUserDocumentRef() = firebaseInstance.collection(USER_COLLECTION)

    private fun getChatDocumentRef() = firebaseInstance.collection(CHATS_COLLECTION)


    private val _bitmaps = MutableStateFlow(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    val bitmaps = _bitmaps.asStateFlow()

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value = bitmap
    }


    /**
     * This section contains the logic for the Firebase communication,
     * needed for the Chats & DropIn Feature.
     */

    private val _friendListData = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val friendListData: StateFlow<List<FirebaseUser>> get() = _friendListData

    private var friendListDataListener: ListenerRegistration? = null

    @Suppress("LABEL_NAME_CLASH")
    fun fetchFriendsFromUser(onComplete: () -> Unit) {
        if (auth.currentUser == null) {
            return
        }
        val subCollectionRef = getUserDocumentRef()
            .document(auth.currentUser!!.uid)
            .collection("/$FRIENDS_COLLECTION")
            .whereNotEqualTo("status", "blocked")
        friendListDataListener?.remove()
        friendListDataListener =
            subCollectionRef.addSnapshotListener { friendQuerySnapshot, friendException ->
                if (friendException != null) {
                    Log.e(
                        "FriendListener",
                        "Error listening for friend data: ${friendException.message}"
                    )
                    return@addSnapshotListener
                }
                val personListData = mutableListOf<FirebaseUser>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    val subCollectionData = friendSnapshot.toObjects(FirebaseFriend::class.java)
                    var completedCount = 0
                    val totalFriends = subCollectionData.size
                    for (friend in subCollectionData) {
                        Log.println(Log.INFO, "Friend", friend.toString())
                        getUserDocumentRef()
                            .document(friend.id)
                            .addSnapshotListener { userDocumentSnapshot, userException ->
                                if (userException != null) {
                                    Log.e(
                                        "FriendListener",
                                        "Error listening for user data: ${userException.message}"
                                    )
                                    return@addSnapshotListener
                                }
                                val data = userDocumentSnapshot?.toObject(FirebaseUser::class.java)
                                data?.let {
                                    val finalData = FirebaseUser(
                                        image = it.image,
                                        username = it.username,
                                        id = it.id,
                                        status = it.status,
                                        email = it.email,
                                        color = it.color,
                                        blocked = it.blocked,
                                        connected = it.connected,
                                        pinned = it.pinned,
                                        mutedFriend = friend.muted,
                                        statusFriend = friend.status
                                    )
                                    personListData.add(finalData)
                                    updateFriendsList(_friendListData, finalData)
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    onComplete.invoke()
                                    _friendListData.value = personListData
                                    Log.println(Log.INFO, "FriendList", personListData.toString())
                                }
                            }
                    }
                }
            }
    }


    private fun updateFriendsList(
        list: MutableStateFlow<List<FirebaseUser>>,
        data: FirebaseUser?
    ) {
        val userIdToRemove = data?.id
        val currentList = list.value
        val updatedList =
            currentList.filter { it.id != userIdToRemove }
                .toMutableList()
        if (updatedList != currentList) {
            updatedList += data!!
        }
        if (updatedList != currentList) {
            if (updatedList.find { friend.value.personList.id == it.id}!=null) {
                updateFriend(friend.value.copy(personList = updatedList.find { friend.value.personList.id == it.id}!!))
            }
        }
        list.value = updatedList
        sortDataChats {}
    }

    fun deleteFriendFromFriendList() {
        val friendSubCollectionRef =
            getUserDocumentRef().document(auth.currentUser!!.uid).collection(FRIENDS_COLLECTION)
        friendSubCollectionRef.document(friend.value.personList.id).delete().addOnSuccessListener {
            val userSubCollectionRef = getUserDocumentRef().document(friend.value.personList.id)
                .collection(FRIENDS_COLLECTION)
            userSubCollectionRef.document(auth.currentUser!!.uid).delete().addOnSuccessListener {
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    private val _completeChatList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeChatList: StateFlow<List<InternalChatInstance>> get() = _completeChatList

    private val _completeChatMateList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeChatMateList: StateFlow<List<InternalChatInstance>> get() = _completeChatMateList
    private val _completeDropInList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeDropInList: StateFlow<List<InternalChatInstance>> get() = _completeDropInList

    private fun createInternalChatInstance(chat: FirebaseChat): InternalChatInstance {
        val lastMessage= chat.messages.firstOrNull { it.visible.contains(auth.currentUser?.uid) } ?: InternalMessageInstance()

        return InternalChatInstance(
            personList = FirebaseUser(
                blocked = emptyList(),
                image = "https://firebasestorage.googleapis.com/v0/b/chatnet-97f9a.appspot.com/o/images%2FDALL%C2%B7E%202023-09-17%2014.29.57%20-%20Profile%20picture%20for%20an%20AI-Asistent%2C%20digital%20art.png?alt=media&token=f41b85e7-8012-4d5d-87f0-e4bdd7f55030",
                username = mapOf("lowercase" to "chatmate", "mixedcase" to "ChatMate"),
                status = "online",
                id = chat.members.find { it != auth.currentUser?.uid.toString() } ?: "",
                email = "",
                pinned = emptyList(),
                color = "",
                connected = false,
                mutedFriend = false,
                statusFriend = ""
            ),
            lastMessage = lastMessage,
            timestampMessage = lastMessage.timestamp,
            markedAsUnread = chat.unread.contains(auth.currentUser?.uid.toString()),
            pinChat = _user.value.pinned.contains(chat.chatRoomID),
            read = if (lastMessage != InternalMessageInstance()) {
                chat.messages.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
            } else {
                0
            },
            chatRoomID = chat.chatRoomID
        )
    }

    private fun sortDataChats(onComplete: () -> Unit) {
        val updatedPersonList: List<InternalChatInstance> = _friendListData.value.map { person ->
            val matchingChat = _chatData.value.find { chat ->
                chat.members.contains(person.id) && chat.tab == "chats"
            }
            val internalChatInstance = createInternalChatInstance(matchingChat ?: FirebaseChat())

            internalChatInstance.copy(personList = person)

        }

        val finalPersonList =
            updatedPersonList.filter { person -> person.personList.statusFriend == "accepted" }
        val sortedPersonList = finalPersonList.sortedWith(
            compareByDescending<InternalChatInstance> { it.pinChat }.thenByDescending { it.timestampMessage }
        )
        _completeChatList.value = sortedPersonList
        sortDataDropIn { onComplete.invoke() }

    }

    private fun sortDataChatMate(onComplete: () -> Unit) {
        val matchingChats = _chatData.value.filter { chat -> chat.tab == "chatmate" }

        val updatedPersonList = matchingChats.mapNotNull { chat ->
            val chatInstance = createInternalChatInstance(chat)
            if (isChatInstanceValid(chatInstance)) chatInstance else null
        }

        val sortedPersonList = updatedPersonList.sortedWith(
            compareByDescending<InternalChatInstance> { it.pinChat }.thenByDescending { it.timestampMessage }
        )

        _completeChatMateList.value = sortedPersonList
        onComplete.invoke()
    }

    private fun sortDataDropIn(onComplete: () -> Unit) {
        val matchingChats = _chatData.value.filter { chat -> chat.tab == "dropIn" }
        matchingChats.forEach { chat ->
            val friend = chat.members.find { it != auth.currentUser?.uid.toString() } ?: ""
            if (_friendListData.value.any { it.id == friend && it.statusFriend == "accepted" }) {
                _completeDropInList.value =
                    _completeDropInList.value.filter { it.chatRoomID != chat.chatRoomID }
            } else {
                fetchUser(friend) { instance ->
                    instance?.let { user ->
                        _completeDropInList.value = _completeDropInList.value.filter { it.chatRoomID != chat.chatRoomID }
                        _completeDropInList.value += InternalChatInstance(
                            personList = user,
                            lastMessage = chat.messages.firstOrNull { it.visible.contains(auth.currentUser?.uid) } ?: InternalMessageInstance(),
                            timestampMessage = chat.messages.firstOrNull{it.visible.contains(auth.currentUser?.uid)}?.timestamp ?: Timestamp.now(),
                            markedAsUnread = chat.unread.contains(auth.currentUser?.uid.toString()),
                            pinChat = _user.value.pinned.contains(chat.chatRoomID),
                            read = if (chat.messages.firstOrNull() != InternalMessageInstance()) {
                                chat.messages.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
                            } else {
                                0
                            },
                            chatRoomID = chat.chatRoomID
                        )
                    }
                }
            }
        }

        sortDataChatMate { onComplete.invoke() }
    }

    private fun fetchUser(friend: String, onComplete: (FirebaseUser?) -> Unit) {
        val userDocumentRef = FirebaseFirestore.getInstance().collection("users").document(friend)

        // Add a SnapshotListener to get real-time updates
        userDocumentRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                exception.printStackTrace()
                onComplete(null)
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // The document exists and has been updated
                val user = documentSnapshot.toObject(FirebaseUser::class.java)

                if (user != null) {
                    if (user.id==_friend.value.personList.id){
                        updateFriend(_friend.value.copy(personList = user))

                    }
                }
                onComplete(user)
            } else {
                // The document doesn't exist or has been deleted
                onComplete(null)
            }
        }
    }


    private fun isChatInstanceValid(chatInstance: InternalChatInstance): Boolean {
        return chatInstance.personList.id.isNotBlank() && chatInstance.chatRoomID.isNotBlank()
    }

    fun removeDropInUser(uID: String) {
        _completeDropInList.value = _completeDropInList.value.filter { it.personList.id != uID }
    }

    /**
     * This section contains the logic for the Firebase communication, used for exchanging messages,
     * in the ChatsView.
     */

    private val _chatData = MutableStateFlow<List<FirebaseChat>>(emptyList())
    val chatData: StateFlow<List<FirebaseChat>> get() = _chatData

    @Suppress("UNCHECKED_CAST", "LABEL_NAME_CLASH")
    fun fetchChatsWithMessages() {
        val chatDataSet = mutableSetOf<FirebaseChat>()
        // Step 1: Listen for changes in the chats collection
        Log.println(Log.INFO, "ChatData!!!", auth.currentUser!!.uid)
        getChatDocumentRef().whereArrayContains("members", auth.currentUser!!.uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                // Step 2: For each change in the chats collection, fetch the messages subcollection
                querySnapshot?.documentChanges?.forEach { documentChange ->
                    // Step 3: Listen for changes in the messages subCollection
                    val removedDocumentId = documentChange.document.id
                    val removedChat = chatDataSet.find { it.chatRoomID == removedDocumentId }
                    if (removedChat != null) {
                        chatDataSet.remove(removedChat)
                    }
                    _chatData.value = chatDataSet.toList()
                    if (documentChange.type != DocumentChange.Type.REMOVED) {
                        val subCollectionRef =
                            getChatDocumentRef().document(documentChange.document.id)
                                .collection("/$MESSAGES_COLLECTION")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                        subCollectionRef.addSnapshotListener(MetadataChanges.INCLUDE) { subQuerySnapshot, exception ->
                            if (exception != null) {
                                return@addSnapshotListener
                            }
                            // Step 4: For each change in the messages subCollection, update the chatDataSet
                            Log.println(Log.INFO, "2", subQuerySnapshot?.documents.toString())
                            subQuerySnapshot?.let { subSnapshot ->
                                val subCollectionData =
                                    subSnapshot.documents.map { messageDocument ->
                                        InternalMessageInstance(
                                            id = messageDocument.id,
                                            sender = messageDocument.data?.get("sender") as? String
                                                ?: "",
                                            images = messageDocument.data?.get("images") as? List<String>
                                                ?: emptyList(),
                                            read = messageDocument.data?.get("read") as? Boolean
                                                ?: false,
                                            text = messageDocument.data?.get("text") as? String
                                                ?: "",
                                            timestamp = messageDocument.data?.get("timestamp") as? Timestamp
                                                ?: Timestamp.now(),
                                            visible = messageDocument.data?.get("visible") as? List<String>
                                                ?: emptyList(),
                                            isFromCache = if (subQuerySnapshot.metadata.isFromCache) {
                                                messageDocument.metadata.hasPendingWrites()
                                            } else {
                                                false
                                            }
                                        )
                                    }
                                val chat = FirebaseChat(
                                    members = documentChange.document.data["members"] as List<String>,
                                    unread = documentChange.document.data["unread"] as List<String>,
                                    tab = documentChange.document.data["tab"] as String,
                                    chatRoomID = documentChange.document.id,
                                    messages = subCollectionData
                                )
                                // Step 5: Update the chatDataSet
                                val existingChat =
                                    chatDataSet.find { it.chatRoomID == chat.chatRoomID }
                                if (existingChat != null) {
                                    chatDataSet.remove(existingChat)
                                }
                                chatDataSet.add(chat)
                                _chatData.value = chatDataSet.toList()

                                sortDataChats { }
                                Log.println(Log.INFO, "ChatData", chatDataSet.toString())
                            }
                        }
                    } else {
                        sortDataChats { }
                    }
                }
            }
    }


    fun deleteMessage(
        documentId: String,
        messageId: String,
    ) {
        getChatDocumentRef().document(documentId).collection("/$MESSAGES_COLLECTION")
            .document(messageId)
            .delete()
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    fun changeMessageVisibility(
        documentId: String,
        messageId: String
    ) {
        getChatDocumentRef().document(documentId)
            .collection("/$MESSAGES_COLLECTION")
            .document(messageId)
            .update(
                "visible",
                FieldValue.arrayRemove(auth.currentUser?.uid.toString())
            )
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    fun deleteChatRoom() {
        for (chat in chatData.value) {
            if (chat.chatRoomID == friend.value.chatRoomID) {
                val subCollectionRef =
                    getChatDocumentRef().document(chat.chatRoomID).collection(MESSAGES_COLLECTION)

                // Delete messages
                subCollectionRef.get().addOnCompleteListener { messagesTask ->
                    if (messagesTask.isSuccessful) {
                        for (document in messagesTask.result!!) {
                            subCollectionRef.document(document.id).delete()
                                .addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                        }

                        // After deleting messages, delete the chat
                        getChatDocumentRef().document(chat.chatRoomID).delete()
                            .addOnCompleteListener { chatDeleteTask ->
                                if (chatDeleteTask.isSuccessful) {
                                    // Handle successful chat deletion
                                } else {
                                    // Handle chat deletion failure
                                    chatDeleteTask.exception?.printStackTrace()
                                }
                            }
                    } else {
                        // Handle messages fetch failure
                        messagesTask.exception?.printStackTrace()
                    }
                }
            }
        }
    }

    suspend fun saveMessages(
        documentId: String?,
        message: FirebaseMessage,
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                firebaseInstance.collection("$CHATS_COLLECTION/${documentId}/$MESSAGES_COLLECTION")
                    .add(message)
                    .await()
                onComplete.invoke()
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }


    /**
     * This section contains the logic for the Firebase communication, used for searching for users,
     * in the SearchView.
     */


    fun saveChatRoom(person: String, tab: String, onComplete: (String) -> Unit = {}) {
        val membersArray = arrayListOf(auth.currentUser!!.uid, person)
        val fieldUpdates = hashMapOf(
            "members" to membersArray,
            "tab" to tab,
            "unread" to emptyList<String>(),
        )

        val chatDocumentRef = getChatDocumentRef().document()

        chatDocumentRef.set(fieldUpdates)
            .addOnSuccessListener {
                onComplete.invoke(chatDocumentRef.id)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    fun updateChatRoom(tab: String, chatRoomId: String) {
        val fieldUpdates = hashMapOf<String, Any>(
            "tab" to tab,
        )
        getChatDocumentRef().document(chatRoomId).update(fieldUpdates)
            .addOnSuccessListener { }
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriendForFriend(person: FirebaseUser, status: String) {
        val fieldUpdates = mapOf(
            "status" to status,
            "muted" to false,
            "id" to auth.currentUser!!.uid
        )
        firebaseInstance.collection("$USER_COLLECTION/${person.id}/$FRIENDS_COLLECTION")
            .document(auth.currentUser!!.uid)
            .set(fieldUpdates, SetOptions.mergeFields("status", "muted", "id"))
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    fun saveFriendForUser(person: FirebaseUser, status: String) {
        val fieldUpdates = mapOf(
            "status" to status,
            "muted" to false,
            "id" to person.id,
        )
        firebaseInstance.collection("$USER_COLLECTION/${auth.currentUser!!.uid}/$FRIENDS_COLLECTION")
            .document(person.id).set(fieldUpdates, SetOptions.mergeFields("status", "muted", "id"))
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun updateUsername(name: String, callback: (Boolean) -> Unit) {
        val fieldUpdates = mapOf(
            "username.lowercase" to name.lowercase(),
            "username.mixedcase" to name,
        )

        firebaseInstance.collection(USER_COLLECTION).document(auth.currentUser!!.uid)
            .update(fieldUpdates)
            .addOnSuccessListener {
                callback.invoke(true)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                callback.invoke(false)
            }
    }

    /**
     * This section contains the logic for the Firebase communication,
     * used for the users account settings.
     */

    fun updateUserProfilePicture(imageReference: String, onComplete: (Boolean) -> Unit) {
        val fieldUpdates = hashMapOf<String, Any>("image" to imageReference)
        getUserDocumentRef().document(auth.currentUser!!.uid).update(fieldUpdates)
            .addOnSuccessListener {
                onComplete.invoke(true)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onComplete.invoke(false)
            }
    }

    private val _user =
        MutableStateFlow(FirebaseUser())
    val user: StateFlow<FirebaseUser> get() = _user
    fun getUserData(onComplete: () -> Unit = {}) {
        Log.println(Log.INFO, "$§$§User", auth.currentUser!!.uid)
        if (auth.currentUser == null) {
            return
        }
        getUserDocumentRef().document(auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(FirebaseUser::class.java)
                    _user.value = personList!!
                    isConnected.value = personList.connected
                    onComplete.invoke()
                    Log.println(Log.INFO, "User", personList.toString())
                    sortDataChats {}
                }
            }
    }

    /**
     * This section contains the logic for the Firebase communication,
     * for handling different user availability states, based on app state.
     */

    fun updateOnlineStatus(status: String) {
        if (auth.currentUser == null) {
            return
        }
        try {
            val fieldUpdates = hashMapOf<String, Any>(
                "status" to status,
            )
            getUserDocumentRef().document(auth.currentUser!!.uid).update(fieldUpdates)
                .addOnSuccessListener {}
                .addOnFailureListener { exception -> exception.printStackTrace() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reset() {
        _chatData.value = emptyList()
        _friend.value = InternalChatInstance()
        _friendListData.value = emptyList()
        _user.value = FirebaseUser()
        randState.value = false
    }


    fun updatePinChatStatus(isAlreadyPinned: Boolean) {
        for (chat in chatData.value) {
            if (chat.chatRoomID == friend.value.chatRoomID) {
                val userRef = getUserDocumentRef().document(auth.currentUser?.uid.toString())
                val updateData = if (isAlreadyPinned) {
                    mapOf("pinned" to FieldValue.arrayRemove(chat.chatRoomID))
                } else {
                    mapOf("pinned" to FieldValue.arrayUnion(chat.chatRoomID))
                }
                userRef.update(updateData)
                    .addOnSuccessListener {}
                    .addOnFailureListener { exception -> exception.printStackTrace() }
            }
        }
    }

    fun updateBlockedUserList(isAlreadyBlocked: Boolean) {
        val userRef = getUserDocumentRef().document(auth.currentUser?.uid.toString())
        val updateData = if (isAlreadyBlocked) {
            mapOf("blocked" to FieldValue.arrayRemove(friend.value.personList.id))
        } else {
            mapOf("blocked" to FieldValue.arrayUnion(friend.value.personList.id))
        }
        userRef.update(updateData)
            .addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun updateMuteFriendStatus(isAlreadyMuted: Boolean) {
        val chatRef =
            firebaseInstance.collection("$USER_COLLECTION/${auth.currentUser?.uid.toString()}/$FRIENDS_COLLECTION")
                .document(friend.value.personList.id)
        val updateData = if (isAlreadyMuted) {
            mapOf("muted" to false)
        } else {
            mapOf("muted" to true)
        }
        chatRef.update(updateData)
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun markMessagesAsRead(user: InternalChatInstance) {
        for (chat in chatData.value) {
            if (chat.chatRoomID == user.chatRoomID) {
                val chatRef =
                    getChatDocumentRef().document(chat.chatRoomID).collection(MESSAGES_COLLECTION)
                chatRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val sender = document.getString("sender")
                        if (sender != auth.currentUser?.uid.toString()) {
                            chatRef.document(document.id).update("read", true)
                                .addOnSuccessListener {}
                                .addOnFailureListener { exception -> exception.printStackTrace() }
                        }
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
        }
    }

    fun updateMarkAsReadStatus(isAlreadyUnread: Boolean) {
        for (chat in chatData.value) {
            if (chat.chatRoomID == friend.value.chatRoomID) {
                val chatRef = getChatDocumentRef().document(chat.chatRoomID)
                val updateData = if (isAlreadyUnread) {
                    mapOf("unread" to FieldValue.arrayRemove(auth.currentUser?.uid.toString()))
                } else {
                    mapOf("unread" to FieldValue.arrayUnion(auth.currentUser?.uid.toString()))
                }

                chatRef.update(updateData)
                    .addOnSuccessListener {}
                    .addOnFailureListener { exception -> exception.printStackTrace() }
            }
        }
    }

    fun deleteMessagesForUser(chatRoomId: String) {

        val specificChat = chatData.value.find { it.chatRoomID == chatRoomId }


        val chatRef = getChatDocumentRef().document(specificChat!!.chatRoomID).collection(MESSAGES_COLLECTION)
        chatRef.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val sender = document.get("visible") as List<*>
                if (auth.currentUser?.uid.toString() in sender) {
                    val updatedVisible = sender.toMutableList()
                    updatedVisible.remove(auth.currentUser?.uid.toString())

                    chatRef.document(document.id)
                        .update("visible", updatedVisible)
                        .addOnSuccessListener {
                            // Successfully removed the entry from the "visible" array
                        }
                        .addOnFailureListener { exception ->
                            exception.printStackTrace()
                            // Handle the failure here
                        }
                }
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            // Handle the failure here
        }


    }

    fun sendDataToServer(data: String, onReceived: (String) -> Unit) {
        chatMateResponseState.value = ChatMateResponseState.Loading
        val url = "https://getresponse-ie4mphraqq-uc.a.run.app/getResponse?text=$data"
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.println(Log.INFO, "Response3", e.toString())
                chatMateResponseState.value = ChatMateResponseState.Success
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody ?: "")
                    val resultText = jsonObject.optString("result", "")
                    chatMateResponseState.value = ChatMateResponseState.Success
                    onReceived.invoke(resultText)
                    Log.println(Log.INFO, "Response1", responseBody ?: "")
                } else {
                    chatMateResponseState.value = ChatMateResponseState.Success
                    Log.println(Log.INFO, "Response2", response.toString())
                }
            }
        })
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Message", text)
        clipboardManager.setPrimaryClip(clip)
    }

    fun resetRandChat() {
        val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
        val client = OkHttpClient()
        val requestData =
            "{\"user\":\"${auth.currentUser?.uid.toString()}\", \"newUser\":${false},\"action\":\"disconnect\"}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestData.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.println(Log.INFO, "Response", responseBody ?: "")
                    // Response is null, so schedule a retry after 5 seconds
                } else {
                    Log.println(Log.INFO, "Response", response.toString())
                }
            }
        })
    }

    fun checkIfUserIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun createChatMateChat() {
        val membersArray = arrayListOf(auth.currentUser!!.uid, "ChatMate")
        val fieldUpdates = hashMapOf(
            "members" to membersArray,
            "tab" to "chatmate",
            "unread" to emptyList<String>(),
        )
        getChatDocumentRef().document().set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }


    private val _friendRandomFriendsListData = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val friendRandomFriendsListData: StateFlow<List<FirebaseUser>> get() = _friendRandomFriendsListData

    fun fetchRandomFriendsFromFriend() {
        if (friendListData.value.isEmpty()) {
            return
        }
        val randomFriend = friendListData.value.random()
        getUserDocumentRef().document(randomFriend.id).collection("/$FRIENDS_COLLECTION")
            .whereNotEqualTo("status", "pending").limit(5).get()
            .addOnSuccessListener { friendQuerySnapshot ->
                val personListData = mutableListOf<FirebaseUser>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    try {
                        val subCollectionData = friendSnapshot.toObjects(FirebaseFriend::class.java)
                        var completedCount = 0
                        val totalFriends = subCollectionData.size
                        for (friend in subCollectionData) {
                            Log.println(Log.INFO, "Friend", friend.toString())
                            getUserDocumentRef()
                                .document(friend.id)
                                .get().addOnSuccessListener { userDocumentSnapshot ->
                                    try {
                                        val data =
                                            userDocumentSnapshot?.toObject(FirebaseUser::class.java)
                                        if (data != null) {
                                            val finalData = FirebaseUser(
                                                image = data.image,
                                                username = data.username,
                                                id = data.id,
                                                status = data.status,
                                                email = data.email,
                                                color = data.color,
                                                blocked = data.blocked,
                                                connected = data.connected,
                                                pinned = data.pinned,
                                                mutedFriend = friend.muted,
                                                statusFriend = friend.status,
                                            )
                                            personListData.add(finalData)
                                        }
                                        completedCount++
                                        if (completedCount == totalFriends) {
                                            _friendRandomFriendsListData.value = personListData
                                            Log.println(
                                                Log.INFO,
                                                "FriendFriendsList",
                                                personListData.toString()
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }.addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis = 5000L
    fun getRandChat(
        sharedViewModel: SharedViewModel,
        state: Boolean,
        navController: NavController,
        onComplete: () -> Unit
    ) {
        val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
        val client = OkHttpClient()
        val requestData =
            "{\"user\":\"${sharedViewModel.auth.currentUser?.uid.toString()}\", \"newUser\":${state}}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestData.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.println(Log.INFO, "Response", responseBody ?: "")
                    // Response is null, so schedule a retry after 5 seconds
                    if (navController.currentDestination?.route != Screens.RandChatScreen.route) {
                        Log.println(Log.INFO, "Response", "Not on RandChatScreen")
                    } else if (responseBody == "{\"partner\":null}") {
                        handler.postDelayed(
                            { getRandChat(sharedViewModel, false, navController) {} },
                            delayMillis
                        )
                    } else {
                        val partner =
                            responseBody?.substringAfter("partner\":\"")?.substringBefore("\"")
                        Log.println(Log.INFO, "Response", partner ?: "")
                        if (partner != null) {
                            randState.value = true
                            fetchUsers(partner, sharedViewModel) { onComplete.invoke() }
                        }
                    }
                } else {
                    Log.println(Log.INFO, "Response", response.toString())
                }
            }
        })
    }

    fun fetchUsers(uID: String, sharedViewModel: SharedViewModel, onComplete: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(uID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.println(Log.INFO, "Response", sharedViewModel.chatData.value.toString())
                    val personList = documentSnapshot.toObject(FirebaseUser::class.java)
                    val specificChat = sharedViewModel.chatData.value.find {
                        it.tab == "randchat" && it.members.contains(uID) && it.members.contains(auth.currentUser?.uid.toString())
                    }
                    _friend.value = InternalChatInstance(
                        personList = personList!!,
                        timestampMessage = Timestamp.now(),
                        lastMessage = InternalMessageInstance(),
                        pinChat = false,
                        read = 0,
                        markedAsUnread = false,
                        chatRoomID = specificChat?.chatRoomID ?: "",
                    )
                    onComplete.invoke()
                }
            }
    }

    fun saveBitmapToGallery(
        bitmap: Bitmap,
        displayName: String,
        context: Context,
        onSaved: () -> Unit
    ): Uri? {
        val chatNetFolderName = "ChatNet"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$chatNetFolderName")
        }
        val contentResolver = context.contentResolver
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        try {
            uri?.let { imageUri ->
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    onSaved.invoke()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return uri
    }

    private val _friendFriendsListData = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val friendFriendsListData: StateFlow<List<FirebaseUser>> get() = _friendFriendsListData

    fun fetchFriendsFriends() {
        val randomFriend = friend.value.personList.id
        getUserDocumentRef().document(randomFriend).collection("/$FRIENDS_COLLECTION")
            .whereNotEqualTo("status", "accepted").get()
            .addOnSuccessListener { friendQuerySnapshot ->
                val personListData = mutableListOf<FirebaseUser>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    try {
                        val subCollectionData = friendSnapshot.toObjects(FirebaseFriend::class.java)
                        var completedCount = 0
                        val totalFriends = subCollectionData.size
                        for (friend in subCollectionData) {
                            Log.println(Log.INFO, "Friend", friend.toString())
                            getUserDocumentRef()
                                .document(friend.id)
                                .get().addOnSuccessListener { userDocumentSnapshot ->
                                    try {
                                        val data =
                                            userDocumentSnapshot?.toObject(FirebaseUser::class.java)
                                        if (data != null) {
                                            val finalData = FirebaseUser(
                                                image = data.image,
                                                username = data.username,
                                                id = data.id,
                                                status = data.status,
                                                email = data.email,
                                                color = data.color,
                                                blocked = data.blocked,
                                                connected = data.connected,
                                                pinned = data.pinned,
                                                mutedFriend = friend.muted,
                                                statusFriend = friend.status,
                                            )
                                            personListData.add(finalData)
                                        }
                                        completedCount++
                                        if (completedCount == totalFriends) {
                                            _friendFriendsListData.value =
                                                personListData.filter { person ->
                                                    friendListData.value.any { friend -> friend.id == person.id }
                                                }
                                            Log.println(
                                                Log.INFO,
                                                "FriendFriendsList",
                                                personListData.toString()
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }.addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun changeMediaVisibility(userContext: Boolean, isMedia: Boolean) {
        for (chat in chatData.value) {
            if (chat.members.contains(friend.value.personList.id) && chat.members.contains(auth.currentUser?.uid.toString())) {
                val chatRef =
                    getChatDocumentRef().document(chat.chatRoomID).collection(MESSAGES_COLLECTION)
                chatRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        if (isMedia) {
                            // Handle media visibility
                            val images = document.get("images") as List<*>?
                            if (images != null && images.isNotEmpty()) {
                                handleVisibility(userContext, chatRef, document)
                            }
                        } else {
                            // Handle text visibility
                            val text = document.getString("text")
                            if (!text.isNullOrBlank()) {
                                handleVisibility(userContext, chatRef, document)
                            }
                        }
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                    // Handle the failure here
                }
            }
        }
    }

    private fun handleVisibility(
        userContext: Boolean,
        chatRef: CollectionReference,
        document: DocumentSnapshot
    ) {
        val sender = document.get("visible") as List<*>
        if (auth.currentUser?.uid.toString() in sender) {
            val updatedVisible = sender.toMutableList()
            if (userContext) {
                updatedVisible.remove(auth.currentUser?.uid.toString())
            } else {
                updatedVisible.clear()
            }
            chatRef.document(document.id)
                .update("visible", updatedVisible)
                .addOnSuccessListener {
                    // Successfully removed the entry from the "visible" array
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    // Handle the failure here
                }
        }
    }


}
