package at.htlhl.chatnet.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.htlhl.chatnet.data.FirebaseChats
import at.htlhl.chatnet.data.FirebaseFriends
import at.htlhl.chatnet.data.FirebaseMessages
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.data.InternalChatInstances
import at.htlhl.chatnet.data.LoadingStates
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * Created by Tobias Brandl.
 *
 * This class represents the shared view model, which is used to share data between different Views.
 * It also contains the logic for the communication with Firebase.
 */
class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val firebaseInstance = FirebaseFirestore.getInstance()

    private val _bitmaps = MutableStateFlow(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    val bitmaps = _bitmaps.asStateFlow()

    fun onTakePhoto(bitmap: Bitmap) {
        _bitmaps.value = bitmap
    }

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
    val finishedLoadingData = mutableStateOf(false)
    val friend =
        mutableStateOf(
            InternalChatInstances(
                FirebaseUsers(),
                Timestamp.now(),
                FirebaseMessages(),
                false,
                0,
                false
            )
        )
    val matchedUser = mutableStateOf(FirebaseUsers("", mapOf(), "", "", "", "", "", false, ""))
    val bottomBarState = mutableStateOf(true)
    val gpsState = mutableStateOf(false)
    val localChatUserList = mutableStateOf<List<FirebaseUsers>>(emptyList())
    val searchtext = mutableStateOf("")
    val text = mutableStateOf("")


    fun getMessageLengthForChat(): Int? {
        return _chatData.value.find {
            it.members.contains(friend.value.personList.id) && it.members.contains(
                auth.currentUser?.uid
            )
        }?.messages?.size
    }

    private fun getUserDocumentRef() = firebaseInstance.collection(USER_COLLECTION)

    private fun getChatDocumentRef() = firebaseInstance.collection(CHATS_COLLECTION)

    /**
     * This section contains the logic for the authentication process.
     */

    private val _loadingState = mutableStateOf(LoadingStates.Loading)
    val loadingState: State<LoadingStates> = _loadingState

    fun fetchAuthenticationStatus() {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    auth.currentUser
                }
                _loadingState.value = if (user != null) {
                    LoadingStates.Authenticated
                } else {
                    LoadingStates.NotAuthenticated
                }
            } catch (e: Exception) {
                _loadingState.value = LoadingStates.Error
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

    private val _friendListData = MutableStateFlow<List<FirebaseUsers>>(emptyList())
    val friendListData: StateFlow<List<FirebaseUsers>> get() = _friendListData

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
                val personListData = mutableListOf<FirebaseUsers>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    val subCollectionData = friendSnapshot.toObjects(FirebaseFriends::class.java)
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
                                val data = userDocumentSnapshot?.toObject(FirebaseUsers::class.java)
                                val finalData = FirebaseUsers(
                                    image = data?.image!!,
                                    username = data.username,
                                    id = data.id,
                                    status = data.status,
                                    email = data.email,
                                    color = data.color,
                                    connection = data.connection,
                                    mutedFriend = friend.muted,
                                    statusFriend = friend.status,
                                )
                                finalData.let {
                                    personListData.add(it)
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    _friendListData.value = personListData
                                    Log.println(Log.INFO, "FriendList", "of")
                                }
                                updateFriendsList(_friendListData, finalData)
                            }
                    }
                }
            }
    }


    private fun updateFriendsList(
        list: MutableStateFlow<List<FirebaseUsers>>,
        data: FirebaseUsers?
    ) {
        val userIdToRemove = data?.id
        val currentList = list.value
        val updatedList =
            currentList.filter { it.id != userIdToRemove }
                .toMutableList()
        if (updatedList != currentList) {
            updatedList += data!!
        }
        list.value = updatedList
        sortData {}
    }

    fun deleteFriendFromFriendList() {
        val friendSubCollectionRef =
            getUserDocumentRef().document(auth.currentUser!!.uid).collection(FRIENDS_COLLECTION)
        friendSubCollectionRef.document(friend.value.personList.id).delete().addOnSuccessListener {
            val userSubCollectionRef =
                getUserDocumentRef().document(friend.value.personList.id)
                    .collection(FRIENDS_COLLECTION)
            userSubCollectionRef.document(auth.currentUser!!.uid).delete().addOnSuccessListener {
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    private val _completeChatList = MutableStateFlow<List<InternalChatInstances>>(emptyList())
    val completeChatList: StateFlow<List<InternalChatInstances>> get() = _completeChatList

    fun sortData(onComplete: () -> Unit) {
        val updatedPersonList: List<InternalChatInstances> = _friendListData.value.map { person ->
            val matchingChat = _chatData.value.find { chat ->
                chat.members.contains(person.id) && chat.tab == "chats"
            }
            val lastVisibleMessage = matchingChat?.messages?.lastOrNull { message ->
                auth.currentUser?.uid.toString() in message.visible
            }
            val updatedStatus = lastVisibleMessage ?: FirebaseMessages()
            if (matchingChat?.messages?.lastOrNull()?.sender != person.id && updatedStatus != FirebaseMessages()) {
                InternalChatInstances(personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    markedAsUnread = matchingChat?.unread?.contains(auth.currentUser?.uid.toString()) == true,
                    pinChat = matchingChat?.pinned?.contains(auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
                        ?: 0)
            } else {
                InternalChatInstances(personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    markedAsUnread = matchingChat?.unread?.contains(auth.currentUser?.uid.toString()) == true,
                    pinChat = matchingChat?.pinned?.contains(auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
                        ?: 0)
            }
        }
        val finalPersonList =
            updatedPersonList.filter { person -> person.personList.statusFriend == "accepted" }
        val sortedPersonList =
            finalPersonList.sortedWith(compareByDescending<InternalChatInstances> { it.pinChat }.thenByDescending { it.timestampMessage })
        val completePersonList =
            if (searchtext.value != "") sortedPersonList.filter {
                it.personList.username["mixedcase"]?.contains(
                    searchtext.value, ignoreCase = true
                ) ?: false
            } else sortedPersonList
        onComplete.invoke()
        _completeChatList.value = completePersonList
    }

    /**
     * This section contains the logic for the Firebase communication, used for exchanging messages,
     * in the ChatsView.
     */

    private val _chatData = MutableStateFlow<List<FirebaseChats>>(emptyList())
    val chatData: StateFlow<List<FirebaseChats>> get() = _chatData

    @Suppress("UNCHECKED_CAST", "LABEL_NAME_CLASH")
    fun startListeningForMessagesForPairs(
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (auth.currentUser == null) {
            return
        }
        val chatDataSet = mutableSetOf<FirebaseChats>()
        // Step 1: Listen for changes in the chats collection
        getChatDocumentRef().whereArrayContains("members", auth.currentUser!!.uid)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    onError.invoke(error)
                    return@addSnapshotListener
                }
                // Step 2: For each change in the chats collection, fetch the messages subcollection
                querySnapshot?.documentChanges?.forEach { documentChange ->
                    if (documentChange.type == DocumentChange.Type.REMOVED) {
                        val removedDocumentId = documentChange.document.id
                        val removedChat = chatDataSet.find { it.chatRoomID == removedDocumentId }
                        if (removedChat != null) {
                            chatDataSet.remove(removedChat)
                        }
                        _chatData.value = chatDataSet.toList()
                    } else {
                        // Step 3: Listen for changes in the messages subcollection
                        val subCollectionRef =
                            getChatDocumentRef().document(documentChange.document.id)
                                .collection("/$MESSAGES_COLLECTION").orderBy("timestamp")
                        subCollectionRef.addSnapshotListener { subQuerySnapshot, exception ->
                            if (exception != null) {
                                onError.invoke(exception)
                                return@addSnapshotListener
                            }
                            // Step 4: For each change in the messages subcollection, update the chatDataSet
                            Log.println(Log.INFO, "2", subQuerySnapshot?.documents.toString())
                            subQuerySnapshot?.let { subSnapshot ->
                                val subCollectionData =
                                    subSnapshot.toObjects(FirebaseMessages::class.java)
                                val chat = FirebaseChats(
                                    members = documentChange.document.data["members"] as List<String>,
                                    pinned = documentChange.document.data["pinned"] as List<String>,
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
                                sortData { onComplete.invoke() }
                                Log.println(Log.INFO, "ChatData", chatDataSet.toString())
                            }
                        }
                    }
                }
            }
    }


    fun deleteMessage(
        documentId: String,
        timestamp: Timestamp,
    ) {
        getChatDocumentRef().document(documentId).collection("/$MESSAGES_COLLECTION")
            .whereEqualTo("timestamp", timestamp).get()
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

    fun changeMessageVisibility(
        documentId: String,
        timestamp: Timestamp
    ) {
        getChatDocumentRef().document(documentId)
            .collection("/$MESSAGES_COLLECTION")
            .whereEqualTo("timestamp", timestamp).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.update(
                        "visible",
                        FieldValue.arrayRemove(auth.currentUser?.uid.toString())
                    ).addOnSuccessListener {}
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
            if (chat.members.contains(friend.value.personList.id) && chat.members.contains(auth.currentUser!!.uid)) {
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

    suspend fun saveMessages(documentId: String?, message: FirebaseMessages) {
        firebaseInstance.collection("$CHATS_COLLECTION/${documentId}/$MESSAGES_COLLECTION")
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

    private val _person = MutableStateFlow<List<FirebaseUsers>>(emptyList())

    private suspend fun retrieveMessages(): List<FirebaseUsers> {
        val snapshot = getUserDocumentRef().orderBy("username.mixedcase").startAt(searchText.value)
            .endAt(searchText.value + '\uf8ff').get().await()
        return snapshot.documents.mapNotNull { document ->
            val usernameMap = document["username"] as Map<String, String>?
            val image = document.getString("image")
            val id = document.getString("id")
            val status = document.getString("status")
            val email = document.getString("email")
            val color = document.getString("color")
            val connection = document.getString("connection")
            FirebaseUsers(
                image = image!!,
                username = usernameMap!!,
                id = id!!,
                status = status!!,
                email = email!!,
                color = color!!,
                connection = connection!!,
                mutedFriend = false,
                statusFriend = "",
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
                person.filter { it.doesMatchUsername(text) }
            }
        }.onEach { _isSearching.update { false } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _person.value)

    fun saveChatRoom(person: String, tab: String) {
        val membersArray = arrayListOf(auth.currentUser!!.uid, person)
        val fieldUpdates = hashMapOf(
            "members" to membersArray,
            "tab" to tab,
            "pinned" to emptyList<String>(),
            "unread" to emptyList<String>(),
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

    fun saveFriendForFriend(person: FirebaseUsers, status: String) {
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


    fun saveFriendForUser(person: FirebaseUsers, status: String) {
        val fieldUpdates = mapOf(
            "status" to status,
            "muted" to false,
            "id" to person.id,
        )
        firebaseInstance.collection("$USER_COLLECTION/${auth.currentUser!!.uid}/$FRIENDS_COLLECTION")
            .document(person.id).set(fieldUpdates, SetOptions.mergeFields("status", "muted", "id"))
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun getDocument(onSuccess: (FirebaseUsers?) -> Unit) {
        getUserDocumentRef().document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(FirebaseUsers::class.java)
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

    private val _user =
        MutableStateFlow(FirebaseUsers("", mapOf(), "", "", "", "", "", false, ""))
    val user: StateFlow<FirebaseUsers> get() = _user
    fun getUserData() {
        if (auth.currentUser == null) {
            return
        }
        getUserDocumentRef().document(auth.currentUser!!.uid)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(FirebaseUsers::class.java)
                    _user.value = personList!!
                }
            }
    }

    /**
     * This section contains the logic for the Firebase communication, for the randChat feature.
     */

    /*
    fun pairWithRandomUser() {
        if (_matchedUser.value.id.isNotEmpty()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val pendingUsers = mutableListOf<FirebaseUsers>()
            // Step 1: Check if any user has connection value as "pending"
            getUserDocumentRef().whereEqualTo("connection", "pending").limit(1).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(FirebaseUsers::class.java)
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

                    val user = document.document.toObject(FirebaseUsers::class.java)
                    Log.println(Log.INFO, "MatchFound", user.toString())
                    if ((document.type == DocumentChange.Type.MODIFIED || document.type == DocumentChange.Type.ADDED) && (user.connection == auth.currentUser!!.uid || user.connection == "pending")) {
                        if (user.id != auth.currentUser!!.uid) {
                            _matchedUser.value = user
                        }
                    } else {
                        _matchedUser.value =
                            FirebaseUsers("", mapOf(), "", "", "", "", "", false, "")
                        resetMatchedUser()
                    }
                }
            }
    }


     */
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

    fun resetMatchedUser() {
        getUserDocumentRef().document(auth.currentUser!!.uid).update("connection", "")
    }

    fun reset() {
        _chatData.value = emptyList()
        friend.value = InternalChatInstances(
            FirebaseUsers(),
            Timestamp.now(),
            FirebaseMessages(),
            false,
            0,
            false
        )
        _personData.value = emptyList()
        _friendListData.value = emptyList()
        _user.value = FirebaseUsers("", mapOf(), "", "", "", "", "", false, "")
        matchedUser.value = FirebaseUsers("", mapOf(), "", "", "", "", "", false, "")
    }


    private val _personData = MutableStateFlow<List<FirebaseUsers>>(emptyList())
    val personData: StateFlow<List<FirebaseUsers>> get() = _personData
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
                                val personList =
                                    documentSnapshot.toObject(FirebaseUsers::class.java)
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

    fun updatePinChatStatus(isAlreadyPinned: Boolean) {
        for (chat in chatData.value) {
            if (chat.members.contains(friend.value.personList.id) && chat.members.contains(auth.currentUser?.uid.toString())) {
                val chatRef = getChatDocumentRef().document(chat.chatRoomID)
                val updateData = if (isAlreadyPinned) {
                    mapOf("pinned" to FieldValue.arrayRemove(auth.currentUser?.uid.toString()))
                } else {
                    mapOf("pinned" to FieldValue.arrayUnion(auth.currentUser?.uid.toString()))
                }

                chatRef.update(updateData)
                    .addOnSuccessListener {}
                    .addOnFailureListener { exception -> exception.printStackTrace() }
            }
        }
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

    fun markMessagesAsRead(user: InternalChatInstances) {
        for (chat in chatData.value) {
            if (chat.members.contains(user.personList.id) && chat.members.contains(auth.currentUser?.uid.toString())) {
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
            if (chat.members.contains(friend.value.personList.id) && chat.members.contains(auth.currentUser?.uid.toString())) {
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

    fun deleteMessagesForUser() {
        for (chat in chatData.value) {
            if (chat.members.contains(friend.value.personList.id) && chat.members.contains(auth.currentUser?.uid.toString())) {
                val chatRef =
                    getChatDocumentRef().document(chat.chatRoomID).collection(MESSAGES_COLLECTION)
                chatRef.get().addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val sender = document.get("visible") as List<String>
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
        }
    }

    fun sendDataToServer(data: String, onReceived: (String) -> Unit) {
        val url = "https://getresponse-ie4mphraqq-uc.a.run.app/getResponse"
        val client = OkHttpClient()
        val requestData = ("{\"text\":\"$data\"}")
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
                    val jsonObject = JSONObject(responseBody ?: "")
                    val resultText = jsonObject.optString("result", "")
                    onReceived.invoke(resultText)
                    Log.println(Log.INFO, "Response", responseBody ?: "")
                } else {
                    Log.println(Log.INFO, "Response", response.toString())
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
        val url = "https://disconnect-ie4mphraqq-uc.a.run.app/disconnect"
        val client = OkHttpClient()
        val requestData = "{\"user\":\"${auth.currentUser?.uid.toString()}\"}"
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
}