package at.htlhl.chatnet.viewmodels

import android.graphics.Bitmap
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseFriend
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.LocationUserInstance
import at.htlhl.chatnet.data.PersonType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Created by Tobias Brandl.
 *
 * This class represents the shared view model, which is used to share data between different Views.
 * It also contains parts of the logic needed for the communication with Firebase.
 */
class SharedViewModel : ViewModel() {

    private val firebaseInstance = FirebaseFirestore.getInstance()

    private companion object {
        const val USER_COLLECTION = "users"
        const val CHATS_COLLECTION = "chats"
        const val MESSAGES_COLLECTION = "messages"
        const val FRIENDS_COLLECTION = "friends"
    }

    private fun getUserDocumentRef() = firebaseInstance.collection(USER_COLLECTION)
    private fun getChatDocumentRef() = firebaseInstance.collection(CHATS_COLLECTION)

    /**
     * This section contains some different list elements used to store specific values,
     * fetched from the Code below.
     */

    val auth: FirebaseAuth = Firebase.auth
    var isDataLoaded = mutableStateOf(false)

    val chatMateResponseState = mutableStateOf(ChatMateResponseState.Success)
    var previousRandChatUsersList = mutableStateOf<List<FirebaseUser>>(emptyList())
    private val _publicUserData = MutableStateFlow(FirebaseUser())
    val publicUserData: StateFlow<FirebaseUser> get() = _publicUserData

    private val _friend = MutableStateFlow(InternalChatInstance())
    val friend: StateFlow<InternalChatInstance> get() = _friend
    var unfinishedGoogleRegistration = mutableStateOf("")
    val dropInState = mutableStateOf(false)
    val nearbyDropInUsersList = mutableStateOf<List<LocationUserInstance>>(emptyList())
    val completeDropInNearbyUserList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    private val _bitmap = MutableStateFlow(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    val bitmap = _bitmap.asStateFlow()

    val searchValue = mutableStateOf("")
    val randState = mutableStateOf(false)
    val isConnected = mutableStateOf(false)
    val imageStartPosition = mutableIntStateOf(0)
    val imageList = mutableStateOf<List<InternalMessageInstance>>(emptyList())
    private val _friendListData = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val friendListData: StateFlow<List<FirebaseUser>> get() = _friendListData

    private var friendListDataListener: ListenerRegistration? = null

    private val _completeChatList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeChatList: StateFlow<List<InternalChatInstance>> get() = _completeChatList

    private val _completeChatMateList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeChatMateList: StateFlow<List<InternalChatInstance>> get() = _completeChatMateList
    private val _completeDropInList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeDropInList: StateFlow<List<InternalChatInstance>> get() = _completeDropInList

    private val _chatData = MutableStateFlow<List<FirebaseChat>>(emptyList())
    val chatData: StateFlow<List<FirebaseChat>> get() = _chatData
    private val _userData = MutableStateFlow(FirebaseUser())
    val userData: StateFlow<FirebaseUser> get() = _userData

    private val _friendRandomFriendsListData = MutableStateFlow<List<FirebaseUser>>(emptyList())
    val friendRandomFriendsListData: StateFlow<List<FirebaseUser>> get() = _friendRandomFriendsListData

    fun updateFriend(newFriend: InternalChatInstance, onComplete: () -> Unit = {}) {
        _friend.value = newFriend
        onComplete.invoke()
    }

    fun updateNearbyDropInUsersList(
        newNearbyUsers: List<LocationUserInstance>, onComplete: () -> Unit = {}
    ) {
        nearbyDropInUsersList.value = newNearbyUsers
        filterAndSortNearbyDropInUsers(onComplete)
    }

    fun updatePublicUser(newFriend: FirebaseUser, onComplete: () -> Unit = {}) {
        _publicUserData.value = newFriend
        onComplete()
    }

    fun updateSearchValue(newSearchValue: String, onComplete: () -> Unit = {}) {
        searchValue.value = newSearchValue
        onComplete()
    }

    fun updateImageList(newImageList: List<InternalMessageInstance>, onComplete: () -> Unit = {}) {
        imageList.value = newImageList
        onComplete()
    }

    fun updateImageStartPosition(newImageStartPosition: Int, onComplete: () -> Unit = {}) {
        imageStartPosition.intValue = newImageStartPosition
        onComplete()
    }

    fun updateChatMateResponseState(
        newChatMateResponseState: ChatMateResponseState, onComplete: () -> Unit = {}
    ) {
        chatMateResponseState.value = newChatMateResponseState
        onComplete()
    }

    fun updateDropInState(newState: Boolean, onComplete: () -> Unit = {}) {
        dropInState.value = newState
        onComplete()
    }

    fun updateRandState(newState: Boolean, onComplete: () -> Unit = {}) {
        randState.value = newState
        onComplete()
    }

    fun updatePhotoBitmap(newBitmap: Bitmap) {
        _bitmap.value = newBitmap
    }


    /**
     * This section contains the logic for the Firebase communication,
     * needed for the Chats & DropIn Feature.
     */


    fun reset() {
        updateOnlineStatus(false)
        auth.signOut()
        _chatData.value = emptyList()
        _friend.value = InternalChatInstance()
        _friendListData.value = emptyList()
        _userData.value = FirebaseUser()
        _completeChatList.value = emptyList()
        _completeChatMateList.value = emptyList()
        _completeDropInList.value = emptyList()
        _publicUserData.value = FirebaseUser()
        _friendRandomFriendsListData.value = emptyList()
        previousRandChatUsersList.value = emptyList()
        randState.value = false
        isDataLoaded.value = false
    }


    @Suppress("LABEL_NAME_CLASH")
    fun fetchFriendsFromUser(onComplete: () -> Unit) {
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

                val personListData = mutableListOf<FirebaseUser>()
                friendQuerySnapshot?.documentChanges?.forEach { docChange ->
                    if (docChange.type == DocumentChange.Type.REMOVED) {
                        val deleteList = mutableListOf<FirebaseUser>()
                        deleteList.addAll(_friendListData.value)
                        deleteList.find { it.id == docChange.document.id }
                            ?.let { user -> deleteList.remove(user) }
                        _friendListData.value = deleteList
                    }
                }
                friendQuerySnapshot?.let { friendSnapshot ->
                    val subCollectionData = friendSnapshot.toObjects(FirebaseFriend::class.java)
                    var completedCount = 0
                    val totalFriends = subCollectionData.size
                    for (friend in subCollectionData) {
                        getUserDocumentRef().document(friend.id)
                            .addSnapshotListener { userDocumentSnapshot, userException ->
                                if (userException != null) {
                                    return@addSnapshotListener
                                }
                                val data = userDocumentSnapshot?.toObject(FirebaseUser::class.java)
                                data?.let {
                                    val finalData = FirebaseUser(
                                        image = it.image,
                                        username = it.username,
                                        id = it.id,
                                        online = it.online,
                                        email = it.email,
                                        color = it.color,
                                        blocked = it.blocked,
                                        connected = it.connected,
                                        pinned = it.pinned,
                                        muted = it.muted,
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
                                        tags = it.tags
                                    )
                                    personListData.add(finalData)
                                    updateFriendsList(data = finalData)
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    onComplete()
                                    _friendListData.value = personListData
                                }
                            }
                    }
                }
            }
    }

    private fun filterAndSortNearbyDropInUsers(onComplete: () -> Unit) {
        completeDropInNearbyUserList.value = nearbyDropInUsersList.value.map { person ->
            val matchingChat = chatData.value.find { chat ->
                chat.members.contains(person.id)
            }
            InternalChatInstance(personList = FirebaseUser(
                blocked = person.blocked,
                email = "",
                pinned = listOf(),
                color = "",
                connected = false,
                muted = person.muted,
                statusFriend = PersonType.EMPTY_PERSON,
                image = person.image,
                username = person.username,
                online = person.online,
                id = person.id,
                tags = listOf()
            ),
                timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                    ?: Timestamp.now(),
                lastMessage = matchingChat?.messages?.lastOrNull() ?: InternalMessageInstance(),
                markedAsUnread = matchingChat?.unread?.contains(auth.currentUser?.uid) == true,
                pinChat = false,
                chatRoomID = matchingChat?.chatRoomID ?: "",
                read = matchingChat?.messages?.count { it.sender != auth.currentUser?.uid && !it.read }
                    ?: 0)
        }
        onComplete()
    }

    private fun updateFriendsList(
        data: FirebaseUser?
    ) {
        val userIdToRemove = data?.id
        val currentList = _friendListData.value
        val updatedList = currentList.filter { it.id != userIdToRemove }.toMutableList()
        if (updatedList != currentList) {
            updatedList += data!!
        }
        if (updatedList != currentList) {
            if (updatedList.find { friend.value.personList.id == it.id } != null) {
                updateFriend(friend.value.copy(personList = updatedList.find { friend.value.personList.id == it.id }!!))
            }
        }
        _friendListData.value = updatedList
        sortDataChats()
    }

    fun sortDataChats() {
        val updatedPersonList: List<InternalChatInstance> = _friendListData.value.map { person ->
            val matchingChat = _chatData.value.find { chat ->
                chat.members.contains(person.id) && chat.tab == "chats"
            }
            val internalChatInstance = createInternalChatInstance(matchingChat ?: FirebaseChat())

            internalChatInstance.copy(personList = person)
        }
        val finalPersonList =
            updatedPersonList.filter { person -> person.personList.statusFriend == PersonType.ACCEPTED_PERSON }
        val sortedPersonList =
            finalPersonList.sortedWith(compareByDescending<InternalChatInstance> { it.pinChat }.thenByDescending { it.timestampMessage })
        _completeChatList.value = sortedPersonList
        sortDataDropIn()
    }

    private fun sortDataDropIn() {
        val matchingChats = _chatData.value.filter { chat -> chat.tab == "dropin" }
        if (matchingChats.isEmpty()) {
            _completeDropInList.value = emptyList()
        } else {
            matchingChats.forEach { chat ->
                _completeDropInList.value =
                    _completeDropInList.value.filter { it.chatRoomID != chat.chatRoomID }
                val friend = chat.members.find { it != auth.currentUser?.uid.toString() } ?: ""
                if (_friendListData.value.any { it.id == friend && it.statusFriend == PersonType.ACCEPTED_PERSON }) {
                    _completeDropInList.value =
                        _completeDropInList.value.filter { it.chatRoomID != chat.chatRoomID }
                } else {
                    fetchUser(friend) { instance ->
                        instance?.let { user ->
                            _completeDropInList.value =
                                _completeDropInList.value.filter { it.chatRoomID != chat.chatRoomID }
                            _completeDropInList.value += InternalChatInstance(personList = user,
                                lastMessage = chat.messages.firstOrNull { it.visible.contains(auth.currentUser?.uid) }
                                    ?: InternalMessageInstance(),
                                timestampMessage = chat.messages.firstOrNull {
                                    it.visible.contains(
                                        auth.currentUser?.uid
                                    )
                                }?.timestamp ?: Timestamp.now(),
                                markedAsUnread = chat.unread.contains(auth.currentUser?.uid.toString()),
                                pinChat = _userData.value.pinned.contains(chat.chatRoomID),
                                read = if (chat.messages.firstOrNull() != InternalMessageInstance()) {
                                    chat.messages.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
                                } else {
                                    0
                                },
                                chatRoomID = chat.chatRoomID)
                        }
                    }
                }
            }
        }
        sortDataChatMate()
    }

    private fun sortDataChatMate() {
        val matchingChats = _chatData.value.filter { chat -> chat.tab == "chatmate" }

        val updatedPersonList = matchingChats.mapNotNull { chat ->
            val chatInstance = createInternalChatInstance(chat)
            if (isChatInstanceValid(chatInstance)) chatInstance else null
        }

        val sortedPersonList =
            updatedPersonList.sortedWith(compareByDescending<InternalChatInstance> { it.pinChat }.thenByDescending { it.timestampMessage })

        _completeChatMateList.value = sortedPersonList
    }

    private fun createInternalChatInstance(chat: FirebaseChat): InternalChatInstance {
        val lastMessage = chat.messages.firstOrNull { it.visible.contains(auth.currentUser?.uid) }
            ?: InternalMessageInstance()

        return InternalChatInstance(personList = FirebaseUser(blocked = emptyList(),
            image = "https://firebasestorage.googleapis.com/v0/b/chatnet-97f9a.appspot.com/o/assets%2FChatmate_image.png?alt=media&token=747812db-b49c-4d8d-8a5a-03e39293bf9c",
            username = mapOf("lowercase" to "chatmate", "mixedcase" to "ChatMate"),
            online = false,
            id = chat.members.find { it != auth.currentUser?.uid.toString() } ?: "",
            email = "",
            pinned = emptyList(),
            color = "",
            connected = false,
            muted = emptyList(),
            statusFriend = PersonType.EMPTY_PERSON,
            tags = listOf("No Tags")),
            lastMessage = lastMessage,
            timestampMessage = lastMessage.timestamp,
            markedAsUnread = chat.unread.contains(auth.currentUser?.uid.toString()),
            pinChat = _userData.value.pinned.contains(chat.chatRoomID),
            read = if (lastMessage != InternalMessageInstance()) {
                chat.messages.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
            } else {
                0
            },
            chatRoomID = chat.chatRoomID)
    }

    private fun fetchUser(friend: String, onComplete: (FirebaseUser?) -> Unit) {
        val userDocumentRef = FirebaseFirestore.getInstance().collection("users").document(friend)
        userDocumentRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                exception.printStackTrace()
                onComplete(null)
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val user = documentSnapshot.toObject(FirebaseUser::class.java)

                if (user != null) {
                    if (user.id == _friend.value.personList.id) {
                        updateFriend(_friend.value.copy(personList = user))

                    }
                }
                onComplete(user)
            } else {
                onComplete(null)
            }
        }
    }

    private fun isChatInstanceValid(chatInstance: InternalChatInstance): Boolean {
        return chatInstance.personList.id.isNotBlank() && chatInstance.chatRoomID.isNotBlank()
    }

    @Suppress("UNCHECKED_CAST", "LABEL_NAME_CLASH")
    fun fetchChatsWithMessages(): Boolean {
        val chatDataSet = mutableSetOf<FirebaseChat>()
        // Step 1: Listen for changes in the chats collection
        getChatDocumentRef().whereArrayContains("members", auth.currentUser!!.uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (querySnapshot == null || querySnapshot.isEmpty) {
                    isDataLoaded.value = true
                    return@addSnapshotListener
                }
                // Step 2: For each change in the chats collection, fetch the messages subCollection
                querySnapshot.documentChanges.forEach { documentChange ->
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
                                isDataLoaded.value = true
                                sortDataChats()
                            }
                        }
                    } else {
                        sortDataChats()
                    }
                }
            }
        return true
    }

    fun getUserData(onComplete: () -> Unit = {}) {
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
                    _userData.value = personList!!
                    isConnected.value = personList.connected
                    onComplete()
                    sortDataChats()
                }
            }
    }

    fun updateOnlineStatus(status: Boolean) {
        if (auth.currentUser == null) {
            return
        }
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
                            getUserDocumentRef().document(friend.id).get()
                                .addOnSuccessListener { userDocumentSnapshot ->
                                    try {
                                        val data =
                                            userDocumentSnapshot?.toObject(FirebaseUser::class.java)
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
                                        completedCount++
                                        if (completedCount == totalFriends) {
                                            _friendRandomFriendsListData.value = personListData
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

    fun fetchRandChatPairedUser(partnerID: String, onComplete: () -> Unit = {}) {
        randState.value = true
        FirebaseFirestore.getInstance().collection("users").document(partnerID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(FirebaseUser::class.java)
                    if (!previousRandChatUsersList.value.contains(personList)) {
                        previousRandChatUsersList.value += personList!!
                    }
                    val specificChat = chatData.value.find {
                        it.tab == "randchat" && it.members.contains(partnerID) && it.members.contains(
                            auth.currentUser?.uid.toString()
                        )
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
                    onComplete()
                }
            }
    }

    // Needed due to performance issues
    suspend fun markMessagesAsRead() {
        if (friend.value.chatRoomID.isEmpty()) {
            return
        }
        val chatRef =
            FirebaseFirestore.getInstance().collection("chats").document(friend.value.chatRoomID)
                .collection("messages")
        try {
            withContext(Dispatchers.IO) {
                val querySnapshot = chatRef.whereEqualTo("read", false).get().await()
                val batch = FirebaseFirestore.getInstance().batch()
                for (document in querySnapshot.documents) {
                    val sender = document.getString("sender")
                    if (sender != userData.value.id) {
                        val docRef = chatRef.document(document.id)
                        batch.update(docRef, "read", true)
                    }
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
