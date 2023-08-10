package at.htlhl.testing.data

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

class SharedViewModel : ViewModel() {
    //General
    val user = mutableStateOf(PersonList("", "", ""))
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCleared() {
        super.onCleared()
        friendListDataListener?.remove()
    }

    // LoadingScreen
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

    // DropIn (FriendList)
    private val _friendListData = MutableStateFlow<List<PersonList>>(emptyList())
    val friendListData: StateFlow<List<PersonList>> get() = _friendListData

    private val _friendStatusInformationData = MutableStateFlow<List<Friend>>(emptyList())
    val friendStatusInformationData: StateFlow<List<Friend>> get() = _friendStatusInformationData

    private var friendListDataListener: ListenerRegistration? = null

    fun startListeningForFriends() {
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        val subCollectionRef = documentRef.collection("/friends").whereEqualTo("status", "accepted")

        friendListDataListener?.remove()
        friendListDataListener = subCollectionRef.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }
            val personListData = mutableListOf<PersonList>()

            querySnapshot?.let { snapshot ->
                val subCollectionData = snapshot.toObjects(Friend::class.java)
                _friendStatusInformationData.value = subCollectionData

                for (friend in subCollectionData) {
                    FirebaseFirestore.getInstance().collection("user").document(friend.userID).get()
                        .addOnSuccessListener { documentSnapshot ->
                            val data = documentSnapshot.toObject(PersonList::class.java)
                            data?.let { personListData.add(it) }

                            _friendListData.value = personListData
                        }
                }
            }
        }
    }

    // ChatMessages
    private val _documentId = MutableStateFlow("")
    val documentId: StateFlow<String> get() = _documentId

    private val _messageData = MutableStateFlow<List<Message>>(emptyList())
    val messageData: StateFlow<List<Message>> get() = _messageData

    private var messageDataListener: ListenerRegistration? = null

    suspend fun startListeningForMessages(
        documentId: String,
        docId: String,
    ) {
        val collectionRef = FirebaseFirestore.getInstance().collection("chats")
        try {
            val querySnapshot =
                collectionRef.whereIn("participants", listOf(documentId, docId)).get().await()

            for (document in querySnapshot.documents) {
                val data = document.data
                data?.let {
                    println(querySnapshot.documents.firstOrNull()?.id)
                    val documentRef = querySnapshot.documents.firstOrNull()?.id?.let { it1 ->
                        collectionRef.document(
                            it1
                        )
                    }
                    val subCollectionRef =
                        documentRef?.collection("/messages")?.orderBy("timestamp")
                    messageDataListener?.remove()
                    if (subCollectionRef != null) {
                        messageDataListener =
                            subCollectionRef.addSnapshotListener { querySnapshot, exception ->
                                if (exception != null) {
                                    return@addSnapshotListener
                                }
                                querySnapshot?.let { snapshot ->
                                    val subCollectionData = snapshot.toObjects(Message::class.java)
                                    _messageData.value = subCollectionData
                                    _documentId.value = document.id
                                }
                            }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun saveLastMessage(documentId: String, message: Message) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${auth.currentUser!!.uid}/friends")
        val documentRef = collectionRef.document(documentId)
        val fieldUpdates = hashMapOf<String, Any>(
            "lastMessage" to message.content, "lastMessageTimestamp" to message.timestamp
        )
        documentRef.update(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
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
            val lastMessage = document.getString("lastMessage")
            val userID = document.getString("userID")
            val timestamp = document.getTimestamp("timestamp")
            if (firstname != null && image != null && lastMessage != null && userID != null && timestamp != null) {
                Person(
                    image = image,
                    name = firstname,
                    lastMessage = lastMessage,
                    userID = userID,
                    timestamp = timestamp
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

    fun saveChatRoom(person: Person) {
        val collectionRef = FirebaseFirestore.getInstance().collection("chats")
        val documentRef = collectionRef.document()
        val fieldUpdates = hashMapOf<String, Any>(
            "participants" to auth.currentUser!!.uid + person.userID,
        )
        documentRef.set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveFriend(person: Person) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${person.userID}/friends")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        val fieldUpdates = hashMapOf<String, Any>(
            "status" to "pending",
            "userID" to auth.currentUser!!.uid,
        )
        documentRef.set(fieldUpdates).addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    fun saveSubscribed(person: Person) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${auth.currentUser!!.uid}/friends")
        val documentRef = collectionRef.document(person.userID)
        val fieldUpdates = hashMapOf<String, Any>(
            "status" to "accepted",
            "userID" to person.userID,
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
}