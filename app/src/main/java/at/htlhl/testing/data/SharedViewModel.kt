package at.htlhl.testing.data

import android.app.Application
import android.net.Uri
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


class SharedViewModel(application: Application) : AndroidViewModel(application) {

    // General
    val friend = mutableStateOf(PersonList("", "", "", "", Timestamp.now(), false, ""))
    val auth: FirebaseAuth = Firebase.auth
    val bottomBarState = mutableStateOf(true)
    val gpsState = mutableStateOf(false)
    val imageCall = mutableStateOf(false)
    val imageUri = mutableStateOf(Uri.EMPTY)
    val localChatUserList = mutableStateOf<List<PersonList>>(emptyList())

    // LoadingScreen
    private val _loadingState = mutableStateOf(LoadingState.Loading)
    val loadingState: State<LoadingState> = _loadingState

    fun fetchAuthenticationStatus() {
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    auth.currentUser
                }
                Log.println(Log.INFO, "User", user.toString())
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

    // DropIn (FriendList)
    private val _friendListData = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListData: StateFlow<List<PersonList>> get() = _friendListData

    private val _friendListDataLocal = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListDataLocal: StateFlow<List<PersonList>> get() = _friendListDataLocal


    private var friendListDataListener: ListenerRegistration? = null

    @Suppress("LABEL_NAME_CLASH")
    fun startListeningForFriends() {
        if (auth.currentUser == null) {
            return
        }
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        val subCollectionRef = documentRef.collection("/friends").whereEqualTo("status", "accepted")

        friendListDataListener?.remove()
        friendListDataListener =
            subCollectionRef.addSnapshotListener { friendQuerySnapshot, friendException ->
                if (friendException != null) {
                    return@addSnapshotListener
                }
                val personListData = mutableListOf<PersonList>()
                val personListDataLocal = mutableListOf<PersonList>()
                friendQuerySnapshot?.let { friendSnapshot ->
                    val subCollectionData = friendSnapshot.toObjects(Friend::class.java)
                    var completedCount = 0
                    val totalFriends = subCollectionData.size
                    for (friend in subCollectionData) {
                        Log.println(Log.INFO, "Friend", friend.toString())
                        FirebaseFirestore.getInstance().collection("user")
                            .document(friend.userID)
                            .addSnapshotListener { userDocumentSnapshot, userException ->
                                if (userException != null) {
                                    return@addSnapshotListener
                                }
                                val data =
                                    userDocumentSnapshot?.toObject(PersonList::class.java)
                                data?.let {
                                    if (friend.local) personListDataLocal.add(it) else personListData.add(
                                        it
                                    )
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    _friendListDataLocal.value = personListDataLocal
                                    _friendListData.value = personListData
                                }
                                if (friend.local) {
                                    val userIdToRemove = data?.userID
                                    val currentList = _friendListDataLocal.value
                                    val updatedList =
                                        currentList.filter { it.userID != userIdToRemove }
                                            .toMutableList()
                                    if (updatedList != currentList) {
                                        updatedList += data!!
                                    }
                                    _friendListDataLocal.value = updatedList
                                } else {
                                    val userIdToRemove = data?.userID
                                    val currentList = _friendListData.value
                                    val updatedList =
                                        currentList.filter { it.userID != userIdToRemove }
                                            .toMutableList()
                                    if (updatedList != currentList) {
                                        updatedList += data!!
                                    }
                                    _friendListData.value = updatedList
                                }
                            }
                    }
                }
            }
    }


    fun deleteFriendFromFriendList() {
        val userId = auth.currentUser!!.uid
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val userDocumentRef = collectionRef.document(userId)
        val friendSubCollectionRef = userDocumentRef.collection("friends")

        friendSubCollectionRef.document(friend.value.userID).delete().addOnSuccessListener {
            println("Friend deleted successfully.")
            val friendDocumentRef = collectionRef.document(friend.value.userID)
            val userSubCollectionRef = friendDocumentRef.collection("friends")
            userSubCollectionRef.document(userId).delete().addOnSuccessListener {
                println("Friend deleted successfully.")
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

    // ChatMessages
    private val _chatData = MutableStateFlow<List<Chat>>(emptyList())
    val chatData: StateFlow<List<Chat>> get() = _chatData


    @Suppress("UNCHECKED_CAST", "LABEL_NAME_CLASH")
    fun startListeningForMessagesForPairs(
        docIds: String,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val collectionRef = FirebaseFirestore.getInstance().collection("chats")
        val chatDataSet = mutableSetOf<Chat>()
        val deletedDocumentIds = mutableSetOf<String>()
        collectionRef.whereArrayContains("participants", docIds)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    onError.invoke(error)
                    return@addSnapshotListener
                }
                querySnapshot?.documentChanges?.forEach { documentChange ->
                    val document = documentChange.document
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.println(Log.INFO, "Added", document.data.toString())
                            val data = document.data
                            data.let {
                                val subCollectionRef =
                                    collectionRef.document(document.id).collection("/messages")
                                        .orderBy("timestamp")
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
                                Log.println(Log.INFO, "Removed", chatDataSet.toString())
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
        val collectionRef = FirebaseFirestore.getInstance().collection("chats")
        val documentRef = collectionRef.document(documentId)
        documentRef.collection("/messages").whereEqualTo("timestamp", messageId).get()
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
                val subCollectionRef = FirebaseFirestore.getInstance()
                    .collection("chats").document(chat.chatRoomID).collection("messages")
                subCollectionRef.get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            subCollectionRef.document(document.id).delete()
                                .addOnSuccessListener {
                                    val collectionRef =
                                        FirebaseFirestore.getInstance().collection("chats")
                                    val documentRef = collectionRef.document(chat.chatRoomID)
                                    documentRef.delete().addOnSuccessListener {
                                    }.addOnFailureListener { exception ->
                                        exception.printStackTrace()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    exception.printStackTrace()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                    }
            }
        }
    }

    fun deleteChatRoomForLocal(person: String) {
        val collectionRef = FirebaseFirestore.getInstance().collection("chats")
        val documentRef = collectionRef.document(person)
        documentRef.delete().addOnSuccessListener {
            println("ChatRoom deleted successfully.")
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }

    }

    suspend fun saveMessages(documentId: String?, message: Message) {
        FirebaseFirestore.getInstance().collection("chats/${documentId}/messages").add(message)
            .await()
    }


    // SearchView (Create Friend Element)
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _person = MutableStateFlow<List<Person>>(emptyList())

    private suspend fun retrieveMessages(): List<Person> {
        val snapshot = FirebaseFirestore.getInstance().collection("user").orderBy("name")
            .startAt(searchText.value).endAt(searchText.value + '\uf8ff').get().await()
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
        val collectionRef = FirebaseFirestore.getInstance().collection("chats")
        val documentRef = collectionRef.document()
        val participantsArray = arrayListOf(auth.currentUser!!.uid, person)
        val fieldUpdates = hashMapOf<String, Any>(
            "participants" to participantsArray,
        )
        documentRef.set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriend(person: String, local: Boolean) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${person}/friends")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        val fieldUpdates = hashMapOf<String, Any>(
            "local" to local,
            "status" to "accepted",
            "userID" to auth.currentUser!!.uid,
        )
        documentRef.set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveSubscribed(person: String, local: Boolean) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${auth.currentUser!!.uid}/friends")
        val documentRef = collectionRef.document(person)
        val fieldUpdates = hashMapOf<String, Any>(
            "local" to local,
            "status" to "accepted",
            "userID" to person,
        )
        documentRef.set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }

    }

    fun getDocument(onSuccess: (PersonList?) -> Unit) {
        val documentRef =
            FirebaseFirestore.getInstance().collection("user").document(auth.currentUser!!.uid)
        documentRef.get().addOnSuccessListener { documentSnapshot ->
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

    fun updateOnlineStatus(boolean: String) {
        try {
            val collectionRef =
                FirebaseFirestore.getInstance().collection("user")
            val documentRef = collectionRef.document(auth.currentUser!!.uid)
            val fieldUpdates = hashMapOf<String, Any>(
                "online" to boolean,
            )
            documentRef.update(fieldUpdates).addOnSuccessListener {}
                .addOnFailureListener { exception -> exception.printStackTrace() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateUserProfilePicture(imageReference: String) {
        try {
            val collectionRef =
                FirebaseFirestore.getInstance().collection("user")
            val documentRef = collectionRef.document(auth.currentUser!!.uid)
            val fieldUpdates = hashMapOf<String, Any>(
                "image" to imageReference,
            )
            documentRef.update(fieldUpdates).addOnSuccessListener {}
                .addOnFailureListener { exception -> exception.printStackTrace() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _user = MutableStateFlow(PersonList("", "", "", "", Timestamp.now(), false, ""))
    val user: StateFlow<PersonList> get() = _user
    fun getUserData() {
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        documentRef.addSnapshotListener { documentSnapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                val personList = documentSnapshot.toObject(PersonList::class.java)
                _user.value = personList!!
            }
        }
    }


    fun reset() {
        _chatData.value = emptyList()
        _friendListData.value = emptyList()
        _friendListDataLocal.value = emptyList()
    }
}