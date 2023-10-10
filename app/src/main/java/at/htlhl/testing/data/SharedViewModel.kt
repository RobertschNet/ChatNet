package at.htlhl.testing.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
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
            chatData.collect {
                getDropInContactUsers()
            }
        }
    }

    /**
     * This section contains the logic for the Firebase communication,
     * needed for the Chats & DropIn Feature.
     */

    private val _friendListData = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListData: StateFlow<List<PersonList>> get() = _friendListData

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
                val personListDataPending = mutableListOf<PersonList>()
                val personListDataInitiating = mutableListOf<PersonList>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    val subCollectionData = friendSnapshot.toObjects(Friend::class.java)
                    var completedCount = 0
                    val totalFriends = subCollectionData.size
                    for (friend in subCollectionData) {
                        Log.println(Log.INFO, "Friend", friend.toString())
                        getUserDocumentRef()
                            .document(friend.id)
                            .addSnapshotListener { userDocumentSnapshot, userException ->
                                if (userException != null) {
                                    return@addSnapshotListener
                                }
                                val data = userDocumentSnapshot?.toObject(PersonList::class.java)
                                data?.let {
                                    when (friend.status) {
                                        "pending" -> personListDataPending.add(it)
                                        "initiated" -> personListDataInitiating.add(it)
                                        else -> personListData.add(it)
                                    }
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    _friendListData.value = personListData
                                    _friendListPending.value = personListDataPending
                                    _friendListInitiating.value = personListDataInitiating
                                }
                                when (friend.status) {
                                    "pending" -> {
                                        updateFriendsList(_friendListPending, data)
                                    }

                                    "initiated" -> {
                                        updateFriendsList(_friendListInitiating, data)
                                    }

                                    else -> {
                                        updateFriendsList(_friendListData, data)
                                    }
                                }
                            }
                    }
                }
            }
    }


    private fun updateFriendsList(list: MutableStateFlow<List<PersonList>>, data: PersonList?) {
        val userIdToRemove = data?.id
        val currentList = list.value
        val updatedList =
            currentList.filter { it.id != userIdToRemove }
                .toMutableList()
        if (updatedList != currentList) {
            updatedList += data!!
        }
        list.value = updatedList
    }

    fun deleteFriendFromFriendList() {
        val friendSubCollectionRef =
            getUserDocumentRef().document(auth.currentUser!!.uid).collection(FRIENDS_COLLECTION)
        friendSubCollectionRef.document(friend.value.id).delete().addOnSuccessListener {
            val userSubCollectionRef =
                getUserDocumentRef().document(friend.value.id).collection(FRIENDS_COLLECTION)
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
        docIds: String,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val chatDataSet = mutableSetOf<Chat>()
        getChatDocumentRef().whereArrayContains("members", docIds)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    onError.invoke(error)
                    return@addSnapshotListener
                }
                Log.println(Log.INFO, "1", querySnapshot?.documents.toString())
                querySnapshot?.documentChanges?.forEach { documentChange ->
                    if (documentChange.type == DocumentChange.Type.REMOVED) {
                        val removedDocumentId = documentChange.document.id
                        val removedChat = chatDataSet.find { it.chatRoomID == removedDocumentId }
                        if (removedChat != null) {
                            chatDataSet.remove(removedChat)
                        }
                        _chatData.value = chatDataSet.toList()
                    } else {
                        val subCollectionRef =
                            getChatDocumentRef().document(documentChange.document.id)
                                .collection("/$MESSAGES_COLLECTION").orderBy("timestamp")
                        subCollectionRef.addSnapshotListener { subQuerySnapshot, exception ->
                            if (exception != null) {
                                onError.invoke(exception)
                                return@addSnapshotListener
                            }
                            Log.println(Log.INFO, "2", subQuerySnapshot?.documents.toString())
                            subQuerySnapshot?.let { subSnapshot ->
                                val subCollectionData = subSnapshot.toObjects(Message::class.java)
                                val chat = Chat(
                                    members = documentChange.document.data["members"] as List<String>,
                                    tab = documentChange.document.data["tab"] as String,
                                    chatRoomID = documentChange.document.id,
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
            if (chat.members.contains(friend.value.id) && chat.members.contains(auth.currentUser!!.uid)) {
                val subCollectionRef =
                    getChatDocumentRef().document(chat.chatRoomID).collection(MESSAGES_COLLECTION)
                subCollectionRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        subCollectionRef.document(document.id).delete().addOnSuccessListener {
                            getChatDocumentRef().document(chat.chatRoomID).delete()
                                .addOnSuccessListener {
                                }.addOnFailureListener { exception ->
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

    private val _person = MutableStateFlow<List<PersonList>>(emptyList())

    private suspend fun retrieveMessages(): List<PersonList> {
        val snapshot = getUserDocumentRef().orderBy("username").startAt(searchText.value)
            .endAt(searchText.value + '\uf8ff').get().await()
        return snapshot.documents.mapNotNull { document ->
            val username = document.getString("username")
            val image = document.getString("image")
            val id = document.getString("id")
            val status = document.getString("status")
            val connection = document.getString("connection")
            PersonList(
                image = image!!,
                username = username!!,
                id = id!!,
                status = status!!,
                connection = connection!!,
                timestamp = Timestamp.now(),
                local = false,
                statusIntern = "",
            )
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

    fun saveChatRoom(person: String, tab: String) {
        val membersArray = arrayListOf(auth.currentUser!!.uid, person)
        val fieldUpdates = hashMapOf<String, Any>(
            "members" to membersArray,
            "tab" to tab,
        )
        getChatDocumentRef().document().set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun updateChatRoom(tab: String, chatRoomId: String, onComplete: () -> Unit) {
        val fieldUpdates = hashMapOf<String, Any>(
            "tab" to tab,
        )
        getChatDocumentRef().document(chatRoomId).update(fieldUpdates)
            .addOnSuccessListener { onComplete.invoke() }
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriendForFriend(person: PersonList, local: Boolean, status: String) {
        val fieldUpdates = hashMapOf<String, Any>(
            "local" to local,
            "status" to status,
            "id" to auth.currentUser!!.uid,
        )
        firebaseInstance.collection("${USER_COLLECTION}/${person.id}/${FRIENDS_COLLECTION}")
            .document(auth.currentUser!!.uid).set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriendForUser(person: PersonList, local: Boolean, status: String) {
        val fieldUpdates = hashMapOf<String, Any>(
            "local" to local,
            "status" to status,
            "id" to person.id,
        )
        firebaseInstance.collection("${USER_COLLECTION}/${auth.currentUser!!.uid}/${FRIENDS_COLLECTION}")
            .document(person.id).set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriendForFriendWithoutLocal(person: PersonList, status: String) {
        val fieldUpdates = mapOf(
            "status" to status,
            "id" to auth.currentUser!!.uid
        )
        firebaseInstance.collection("${USER_COLLECTION}/${person.id}/${FRIENDS_COLLECTION}")
            .document(auth.currentUser!!.uid)
            .set(fieldUpdates, SetOptions.mergeFields("status", "id"))
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }


    fun saveFriendForUserWithoutLocal(person: PersonList, status: String) {
        val fieldUpdates = mapOf(
            "status" to status,
            "id" to person.id,
        )
        firebaseInstance.collection("${USER_COLLECTION}/${auth.currentUser!!.uid}/${FRIENDS_COLLECTION}")
            .document(person.id).set(fieldUpdates, SetOptions.mergeFields("status", "id"))
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
        if (_matchedUser.value.id.isNotEmpty()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val pendingUsers = mutableListOf<PersonList>()
            // Step 1: Check if any user has connection value as "pending"
            getUserDocumentRef().whereEqualTo("connection", "pending").limit(1).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(PersonList::class.java)
                        if (user != null && user.id != auth.currentUser!!.uid) {
                            pendingUsers.add(user)
                        }
                    }
                    // Step 2: Pair with a random user from the pendingUsers list
                    if (pendingUsers.isNotEmpty()) {
                        val randomUser = pendingUsers[Random().nextInt(pendingUsers.size)]
                        auth.currentUser?.let {
                            getUserDocumentRef().document(it.uid)
                                .update("connection", randomUser.id)
                        }
                        getUserDocumentRef().document(randomUser.id)
                            .update("connection", auth.currentUser!!.uid)
                        _matchedUser.value = randomUser
                    } else {
                        // Step 3: Set connection value for current user to "pending"
                        getUserDocumentRef().document(auth.currentUser!!.uid)
                            .update("connection", "pending")
                    }
                }.addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun checkForMatches() {
        getUserDocumentRef().whereEqualTo("connection", auth.currentUser!!.uid)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                querySnapshot?.documentChanges?.forEach { document ->

                    val user = document.document.toObject(PersonList::class.java)
                    Log.println(Log.INFO, "MatchFound", user.toString())
                    if ((document.type == DocumentChange.Type.MODIFIED || document.type == DocumentChange.Type.ADDED) && (user.connection == auth.currentUser!!.uid || user.connection == "pending")) {
                        if (user.id != auth.currentUser!!.uid) {
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
                "status" to status,
            )
            getUserDocumentRef().document(auth.currentUser!!.uid).update(fieldUpdates)
                .addOnSuccessListener {}
                .addOnFailureListener { exception -> exception.printStackTrace() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetMatchedUser() {
        getUserDocumentRef().document(auth.currentUser!!.uid).update("connection", "")
    }

    fun reset() {
        _chatData.value = emptyList()
        _personData.value = emptyList()
        _friendListData.value = emptyList()
        _user.value = PersonList("", "", "", "", "", Timestamp.now(), false, "")
        _matchedUser.value = PersonList("", "", "", "", "", Timestamp.now(), false, "")
    }


    private val _personData = MutableStateFlow<List<PersonList>>(emptyList())
    val personData: StateFlow<List<PersonList>> get() = _personData
    private fun getDropInContactUsers() {
        Log.println(Log.INFO, "6", _chatData.value.toString())
        val previousChatData = _chatData.value
        val dropInUsers = previousChatData.filter { it.tab == "dropIn" }
        if (dropInUsers.isEmpty()) {
            _personData.value = emptyList()
            return
        }
        val usersToFetch = dropInUsers.flatMap { chat ->
            chat.members.filter { member ->
                member != auth.currentUser?.uid
            }
        }.distinct() // Remove duplicates

        // Filter out users that are already in _personData
        val usersToFetchFiltered = usersToFetch.filter { userId ->
            _personData.value.none { personList -> personList.id == userId }
        }

        // Fetch the user documents
        val fetchTasks = mutableListOf<Task<DocumentSnapshot>>()
        usersToFetchFiltered.forEach { userId ->
            val userDocRef = getUserDocumentRef().document(userId)
            fetchTasks.add(userDocRef.get())
        }

        // Wait for all fetch tasks to complete
        Tasks.whenAllComplete(fetchTasks)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newPersonData = _personData.value.toMutableList()

                    fetchTasks.forEachIndexed { index, fetchTask ->
                        if (fetchTask.isSuccessful) {
                            val documentSnapshot = fetchTask.result
                            if (documentSnapshot.exists()) {
                                val personList = documentSnapshot.toObject(PersonList::class.java)
                                personList?.let {
                                    if (!newPersonData.any { existingPersonList -> existingPersonList.id == usersToFetchFiltered[index] }) {
                                        newPersonData.add(it)
                                    }
                                }
                            }
                        }
                    }


                    val currentChatData = _chatData.value
                    val removedUsers = previousChatData
                        .filter { it.tab == "dropIn" }
                        .filterNot { currentChatData.contains(it) }
                        .flatMap { it.members }
                        .distinct()

                    newPersonData.removeAll { personList -> removedUsers.contains(personList.id) }
                    Log.println(Log.INFO, "3", newPersonData.toString())
                    _personData.value = newPersonData
                    Log.println(Log.INFO, "4", _personData.value.toString())
                } else {
                    // Handle errors
                }
            }
    }


}