package at.htlhl.testing.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Random

/**
 * Created by Tobias Brandl.
 *
 * This class represents the shared view model, which is used to share data between different Views.
 * It also contains the logic for the communication with Firebase.
 */
class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseInstance = FirebaseFirestore.getInstance()

    private companion object {
        const val USER_COLLECTION = "user"
        const val CHATS_COLLECTION = "chats"
        const val MESSAGES_COLLECTION = "messages"
        const val FRIENDS_COLLECTION = "friends"
    }

    /**
     * This section contains some different list elements used to store specific values,
     * fetched from the Code below.
     */

    val auth: FirebaseAuth = Firebase.auth
    val friend = mutableStateOf(PersonList("", "", "", "", "", Timestamp.now(), false, ""))
    val bottomBarState = mutableStateOf(true)
    val gpsState = mutableStateOf(false)
    val imageCall = mutableStateOf(false)
    val localChatUserList = mutableStateOf<List<PersonList>>(emptyList())

    private fun getUserDocumentRef() = firebaseInstance.collection(USER_COLLECTION)

    private fun getChatDocumentRef() = firebaseInstance.collection(CHATS_COLLECTION)

    /**
     * This section contains the logic for the authentication process.
     */

    private val _loadingState = mutableStateOf(LoadingState.Loading)
    val loadingState: State<LoadingState> = _loadingState

    fun fetchAuthenticationStatus() {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    auth.currentUser
                }
                _loadingState.value = if (user != null) {
                    LoadingState.Authenticated
                } else {
                    LoadingState.NotAuthenticated
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingState.Error
            }
        }
    }

    /**
     * This section contains the logic for the Firebase communication,
     * needed for the Chats & DropIn Feature.
     */

    private val _friendListData = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListData: StateFlow<List<PersonList>> get() = _friendListData

    private val _friendListDataLocal = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListDataLocal: StateFlow<List<PersonList>> get() = _friendListDataLocal

    private val _friendListPending = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListPending: StateFlow<List<PersonList>> get() = _friendListPending

    private val _friendListInitiating = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListInitiating: StateFlow<List<PersonList>> get() = _friendListInitiating // TODO: 2023-11-16


    private var friendListDataListener: ListenerRegistration? = null

    @Suppress("LABEL_NAME_CLASH")
    fun startListeningForFriends() {
        if (auth.currentUser == null) {
            return
        }
        val subCollectionRef =
            getUserDocumentRef().document(auth.currentUser!!.uid).collection("/$FRIENDS_COLLECTION")
                .whereNotEqualTo("status", "blocked")
        friendListDataListener?.remove()
        friendListDataListener =
            subCollectionRef.addSnapshotListener { friendQuerySnapshot, friendException ->
                if (friendException != null) {
                    return@addSnapshotListener
                }
                val personListData = mutableListOf<PersonList>()
                val personListDataLocal = mutableListOf<PersonList>()
                val personListDataPending = mutableListOf<PersonList>()
                val personListDataInitiating = mutableListOf<PersonList>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    val subCollectionData = friendSnapshot.toObjects(Friend::class.java)
                    var completedCount = 0
                    val totalFriends = subCollectionData.size
                    for (friend in subCollectionData) {
                        Log.println(Log.INFO, "Friend", friend.toString())
                        getUserDocumentRef()
                            .document(friend.userID)
                            .addSnapshotListener { userDocumentSnapshot, userException ->
                                if (userException != null) {
                                    return@addSnapshotListener
                                }
                                val data = userDocumentSnapshot?.toObject(PersonList::class.java)
                                data?.let {
                                    if (friend.local) personListDataLocal.add(it)
                                    else if (friend.status == "pending")
                                        personListDataPending.add(it)
                                    else if (friend.status == "initiated")
                                        personListDataInitiating.add(it)
                                    else personListData.add(it)
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    _friendListDataLocal.value = personListDataLocal
                                    _friendListData.value = personListData
                                    _friendListPending.value = personListDataPending
                                    _friendListInitiating.value = personListDataInitiating
                                }
                                if (friend.local) {
                                    updateFriendsList(_friendListDataLocal, data)
                                } else if (friend.status == "pending") {
                                    updateFriendsList(_friendListPending, data)
                                } else if (friend.status == "initiated") {
                                    updateFriendsList(_friendListInitiating, data)
                                } else {
                                    updateFriendsList(_friendListData, data)
                                }
                            }
                    }
                }
            }
    }

    private fun updateFriendsList(list: MutableStateFlow<List<PersonList>>, data: PersonList?) {
        val userIdToRemove = data?.userID
        val currentList = list.value
        val updatedList =
            currentList.filter { it.userID != userIdToRemove }
                .toMutableList()
        if (updatedList != currentList) {
            updatedList += data!!
        }
        list.value = updatedList
    }

    fun deleteFriendFromFriendList() {
        val friendSubCollectionRef =
            getUserDocumentRef().document(auth.currentUser!!.uid).collection(FRIENDS_COLLECTION)
        friendSubCollectionRef.document(friend.value.userID).delete().addOnSuccessListener {
            val userSubCollectionRef =
                getUserDocumentRef().document(friend.value.userID).collection(FRIENDS_COLLECTION)
            userSubCollectionRef.document(auth.currentUser!!.uid).delete().addOnSuccessListener {
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }


    override fun onCleared() {
        super.onCleared()
        friendListDataListener?.remove()
    }

    /**
     * This section contains the logic for the Firebase communication, used for exchanging messages,
     * in the ChatsView.
     */

    private val _chatData = MutableStateFlow<List<Chat>>(emptyList())
    val chatData: StateFlow<List<Chat>> get() = _chatData


    @Suppress("UNCHECKED_CAST", "LABEL_NAME_CLASH")
    fun startListeningForMessagesForPairs(
        docIds: String, onComplete: () -> Unit, onError: (Exception) -> Unit
    ) {
        val chatDataSet = mutableSetOf<Chat>()
        val deletedDocumentIds = mutableSetOf<String>()
        getChatDocumentRef().whereArrayContains("participants", docIds)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    onError.invoke(error)
                    return@addSnapshotListener
                }
                querySnapshot?.documentChanges?.forEach { documentChange ->
                    val document = documentChange.document
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val data = document.data
                            data.let {
                                val subCollectionRef = getChatDocumentRef().document(document.id)
                                    .collection("/$MESSAGES_COLLECTION").orderBy("timestamp")
                                subCollectionRef.addSnapshotListener { subQuerySnapshot, exception ->
                                    if (exception != null) {
                                        onError.invoke(exception)
                                        return@addSnapshotListener
                                    }
                                    subQuerySnapshot?.let { subSnapshot ->
                                        val subCollectionData =
                                            subSnapshot.toObjects(Message::class.java)
                                        val chat = Chat(
                                            participants = data["participants"] as List<String>,
                                            chatRoomID = document.id,
                                            messages = subCollectionData
                                        )
                                        val existingChat =
                                            chatDataSet.find { it.chatRoomID == chat.chatRoomID }
                                        if (existingChat != null) {
                                            chatDataSet.remove(existingChat)
                                        }
                                        chatDataSet.add(chat)
                                        _chatData.value = chatDataSet.toList()
                                    }
                                }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val deletedDocumentId = document.id
                            deletedDocumentIds.add(deletedDocumentId)
                            val deletedChat =
                                chatDataSet.find { it.chatRoomID == deletedDocumentId }
                            if (deletedChat != null) {
                                chatDataSet.remove(deletedChat)
                                _chatData.value = chatDataSet.toList()
                            }
                        }

                        else -> {
                            return@addSnapshotListener
                        }
                    }
                }
                onComplete.invoke()
            }
    }


    fun deleteMessageForUser(
        documentId: String,
        messageId: Timestamp,
    ) {
        getChatDocumentRef().document(documentId).collection("/$MESSAGES_COLLECTION")
            .whereEqualTo("timestamp", messageId).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete().addOnSuccessListener {}
                        .addOnFailureListener { exception ->
                            exception.printStackTrace()
                        }
                }
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun deleteChatRoom() {
        for (chat in chatData.value) {
            if (chat.participants.contains(friend.value.userID) && chat.participants.contains(auth.currentUser!!.uid)) {
                val subCollectionRef =
                    getChatDocumentRef().document(chat.chatRoomID).collection(MESSAGES_COLLECTION)
                subCollectionRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        subCollectionRef.document(document.id).delete().addOnSuccessListener {
                            getChatDocumentRef().document(chat.chatRoomID).delete()
                                .addOnSuccessListener {}.addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                        }.addOnFailureListener { exception ->
                            exception.printStackTrace()
                        }
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }
        }
    }

    fun deleteChatRoomForLocal(person: String) {
        getChatDocumentRef().document(person).delete()
            .addOnSuccessListener {}.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    suspend fun saveMessages(documentId: String?, message: Message) {
        firebaseInstance.collection("${CHATS_COLLECTION}/${documentId}/${MESSAGES_COLLECTION}")
            .add(message).await()
    }

    /**
     * This section contains the logic for the Firebase communication, used for searching for users,
     * in the SearchView.
     */

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _person = MutableStateFlow<List<Person>>(emptyList())

    private suspend fun retrieveMessages(): List<Person> {
        val snapshot = getUserDocumentRef().orderBy("name").startAt(searchText.value)
            .endAt(searchText.value + '\uf8ff').get().await()
        return snapshot.documents.mapNotNull { document ->
            val firstname = document.getString("name")
            val image = document.getString("image")
            val userID = document.getString("userID")
            if (firstname != null && image != null && userID != null) {
                Person(
                    image = image,
                    name = firstname,
                    userID = userID,
                )
            } else {
                null
            }
        }
    }

    @OptIn(FlowPreview::class)
    val person = searchText.debounce(500L).onEach { _isSearching.update { true } }
        .combine(_person) { text, person ->
            if (text.isBlank()) {
                person
            } else {
                val initialMessages = retrieveMessages()
                _person.value = initialMessages.toMutableList()
                person.filter { it.doesMatch(text) }
            }
        }.onEach { _isSearching.update { false } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _person.value)

    fun saveChatRoom(person: String) {
        val participantsArray = arrayListOf(auth.currentUser!!.uid, person)
        val fieldUpdates = hashMapOf<String, Any>(
            "participants" to participantsArray,
        )
        getChatDocumentRef().document().set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriendForFriend(person: String, local: Boolean, status: String) {
        val fieldUpdates = hashMapOf<String, Any>(
            "local" to local,
            "status" to status,
            "userID" to auth.currentUser!!.uid,
        )
        firebaseInstance.collection("${USER_COLLECTION}/${person}/${FRIENDS_COLLECTION}")
            .document(auth.currentUser!!.uid).set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriendForUser(person: String, local: Boolean, status: String) {
        val fieldUpdates = hashMapOf<String, Any>(
            "local" to local,
            "status" to status,
            "userID" to person,
        )
        firebaseInstance.collection("${USER_COLLECTION}/${auth.currentUser!!.uid}/${FRIENDS_COLLECTION}")
            .document(person).set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }

    }

    fun getDocument(onSuccess: (PersonList?) -> Unit) {
        getUserDocumentRef().document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(PersonList::class.java)
                    onSuccess(personList)
                } else {
                    onSuccess(null)
                }
            }.addOnFailureListener {
                onSuccess(null)
            }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    /**
     * This section contains the logic for the Firebase communication,
     * used for the users account settings.
     */

    fun updateUserProfilePicture(imageReference: String) {
        val fieldUpdates = hashMapOf<String, Any>("image" to imageReference)
        getUserDocumentRef().document(auth.currentUser!!.uid).update(fieldUpdates)
            .addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    private val _user = MutableStateFlow(PersonList("", "", "", "", "", Timestamp.now(), false, ""))
    val user: StateFlow<PersonList> get() = _user
    fun getUserData() {
        getUserDocumentRef().document(auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(PersonList::class.java)
                    _user.value = personList!!
                }
            }
    }

    /**
     * This section contains the logic for the Firebase communication, for the randChat feature.
     */

    private val _matchedUser =
        MutableStateFlow(PersonList("", "", "", "", "", Timestamp.now(), false, ""))
    val matchedUser: StateFlow<PersonList> get() = _matchedUser

    fun pairWithRandomUser() {
        if (_matchedUser.value.userID.isNotEmpty()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val pendingUsers = mutableListOf<PersonList>()
            // Step 1: Check if any user has randChat value as "pending"
            getUserDocumentRef().whereEqualTo("randChat", "pending").limit(1).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(PersonList::class.java)
                        if (user != null && user.userID != auth.currentUser!!.uid) {
                            pendingUsers.add(user)
                        }
                    }
                    // Step 2: Pair with a random user from the pendingUsers list
                    if (pendingUsers.isNotEmpty()) {
                        val randomUser = pendingUsers[Random().nextInt(pendingUsers.size)]
                        auth.currentUser?.let {
                            getUserDocumentRef().document(it.uid)
                                .update("randChat", randomUser.userID)
                        }
                        getUserDocumentRef().document(randomUser.userID)
                            .update("randChat", auth.currentUser!!.uid)
                        _matchedUser.value = randomUser
                    } else {
                        // Step 3: Set randChat value for current user to "pending"
                        getUserDocumentRef().document(auth.currentUser!!.uid)
                            .update("randChat", "pending")
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun checkForMatches() {
        getUserDocumentRef().whereEqualTo("randChat", auth.currentUser!!.uid)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                querySnapshot?.documentChanges?.forEach { document ->

                    val user = document.document.toObject(PersonList::class.java)
                    Log.println(Log.INFO, "MatchFound", user.toString())
                    if ((document.type == DocumentChange.Type.MODIFIED || document.type == DocumentChange.Type.ADDED) && (user.randChat == auth.currentUser!!.uid || user.randChat == "pending")) {
                        if (user.userID != auth.currentUser!!.uid) {
                            _matchedUser.value = user
                        }
                    } else {
                        _matchedUser.value =
                            PersonList("", "", "", "", "", Timestamp.now(), false, "")
                        resetMatchedUser()
                    }
                }
            }
    }

    /**
     * This section contains the logic for the Firebase communication,
     * for handling different user availability states, based on app state.
     */

    fun updateOnlineStatus(status: String) {
        try {
            val fieldUpdates = hashMapOf<String, Any>(
                "online" to status,
            )
            getUserDocumentRef().document(auth.currentUser!!.uid).update(fieldUpdates)
                .addOnSuccessListener {}
                .addOnFailureListener { exception -> exception.printStackTrace() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetMatchedUser() {
        getUserDocumentRef().document(auth.currentUser!!.uid).update("randChat", "")
    }

    fun reset() {
        _chatData.value = emptyList()
        _friendListData.value = emptyList()
        _friendListDataLocal.value = emptyList()
        _user.value = PersonList("", "", "", "", "", Timestamp.now(), false, "")
        _matchedUser.value = PersonList("", "", "", "", "", Timestamp.now(), false, "")
    }
}