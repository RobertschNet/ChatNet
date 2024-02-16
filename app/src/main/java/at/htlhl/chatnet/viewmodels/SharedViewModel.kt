package at.htlhl.chatnet.viewmodels

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseFriend
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.LocationUserInstance
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
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
class SharedViewModel : ViewModel() {
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
    var isDataLoaded = mutableStateOf(false)

    val chatMateResponseState = mutableStateOf(ChatMateResponseState.Success)
    var previousRandChatUsersList = mutableStateOf<List<FirebaseUser>>(emptyList())
    private val publicUser = MutableStateFlow(FirebaseUser())
    val publicUserData: StateFlow<FirebaseUser> get() = publicUser

    private val _friend = MutableStateFlow(InternalChatInstance())
    val friend: StateFlow<InternalChatInstance> get() = _friend
    var unfinishedGoogleRegistration = mutableStateOf("")
    val dropInState = mutableStateOf(false)
    val nearbyDropInUsersList = mutableStateOf<List<LocationUserInstance>>(emptyList())
    val completeDropInNearbyUserList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    private val _bitmap = MutableStateFlow(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
    val bitmap = _bitmap.asStateFlow()

    val searchValue = mutableStateOf("")
    val text = mutableStateOf("")
    val randState = mutableStateOf(false)
    val isConnected = mutableStateOf(false)
    val imageStartPosition = mutableIntStateOf(0)
    val imageList = mutableStateOf<List<InternalMessageInstance>>(emptyList())

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
        publicUser.value = newFriend
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

    fun updateDropInState(newState: Boolean, onComplete: () -> Unit = {}) {
        dropInState.value = newState
        onComplete()
    }

    fun updatePhotoBitmap(newBitmap: Bitmap) {
        _bitmap.value = newBitmap
    }

    private fun getUserDocumentRef() = firebaseInstance.collection(USER_COLLECTION)

    private fun getChatDocumentRef() = firebaseInstance.collection(CHATS_COLLECTION)

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
        val subCollectionRef =
            getUserDocumentRef().document(auth.currentUser!!.uid).collection("/$FRIENDS_COLLECTION")
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
                        Log.println(Log.INFO, "Friend", friend.toString())
                        getUserDocumentRef().document(friend.id)
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
                                    updateFriendsList(_friendListData, finalData)
                                }
                                completedCount++
                                if (completedCount == totalFriends) {
                                    onComplete.invoke()
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
        list: MutableStateFlow<List<FirebaseUser>>, data: FirebaseUser?
    ) {
        val userIdToRemove = data?.id
        val currentList = list.value
        val updatedList = currentList.filter { it.id != userIdToRemove }.toMutableList()
        if (updatedList != currentList) {
            updatedList += data!!
        }
        if (updatedList != currentList) {
            if (updatedList.find { friend.value.personList.id == it.id } != null) {
                updateFriend(friend.value.copy(personList = updatedList.find { friend.value.personList.id == it.id }!!))
            }
        }
        list.value = updatedList
        sortDataChats {}
    }


    private val _completeChatList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeChatList: StateFlow<List<InternalChatInstance>> get() = _completeChatList

    private val _completeChatMateList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeChatMateList: StateFlow<List<InternalChatInstance>> get() = _completeChatMateList
    private val _completeDropInList = MutableStateFlow<List<InternalChatInstance>>(emptyList())
    val completeDropInList: StateFlow<List<InternalChatInstance>> get() = _completeDropInList

    private fun createInternalChatInstance(chat: FirebaseChat): InternalChatInstance {
        val lastMessage = chat.messages.firstOrNull { it.visible.contains(auth.currentUser?.uid) }
            ?: InternalMessageInstance()

        return InternalChatInstance(personList = FirebaseUser(blocked = emptyList(),
            image = "https://firebasestorage.googleapis.com/v0/b/chatnet-97f9a.appspot.com/o/images%2FDALL%C2%B7E%202023-09-17%2014.29.57%20-%20Profile%20picture%20for%20an%20AI-Asistent%2C%20digital%20art.png?alt=media&token=f41b85e7-8012-4d5d-87f0-e4bdd7f55030",
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
            pinChat = _user.value.pinned.contains(chat.chatRoomID),
            read = if (lastMessage != InternalMessageInstance()) {
                chat.messages.count { it.sender != auth.currentUser?.uid.toString() && !it.read }
            } else {
                0
            },
            chatRoomID = chat.chatRoomID)
    }

    fun sortDataChats(onComplete: () -> Unit) {
        Log.println(Log.INFO, "Was triggered", _friendListData.value.toString())
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
        sortDataDropIn { onComplete.invoke() }
    }

    private fun sortDataChatMate(onComplete: () -> Unit) {
        val matchingChats = _chatData.value.filter { chat -> chat.tab == "chatmate" }

        val updatedPersonList = matchingChats.mapNotNull { chat ->
            val chatInstance = createInternalChatInstance(chat)
            if (isChatInstanceValid(chatInstance)) chatInstance else null
        }

        val sortedPersonList =
            updatedPersonList.sortedWith(compareByDescending<InternalChatInstance> { it.pinChat }.thenByDescending { it.timestampMessage })

        _completeChatMateList.value = sortedPersonList
        onComplete.invoke()
    }

    private fun sortDataDropIn(onComplete: () -> Unit) {
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
                                pinChat = _user.value.pinned.contains(chat.chatRoomID),
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
                    if (user.id == _friend.value.personList.id) {
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

    /**
     * This section contains the logic for the Firebase communication, used for exchanging messages,
     * in the ChatsView.
     */

    private val _chatData = MutableStateFlow<List<FirebaseChat>>(emptyList())
    val chatData: StateFlow<List<FirebaseChat>> get() = _chatData

    @Suppress("UNCHECKED_CAST", "LABEL_NAME_CLASH")
    fun fetchChatsWithMessages(): Boolean {
        val chatDataSet = mutableSetOf<FirebaseChat>()
        // Step 1: Listen for changes in the chats collection
        Log.println(Log.INFO, "ChatData!!!", auth.currentUser!!.uid)
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
                                isDataLoaded.value = true
                                sortDataChats { }
                                Log.println(Log.INFO, "ChatData", chatDataSet.toString())
                            }
                        }
                    } else {
                        sortDataChats { }
                    }
                }
            }
        return true
    }

    private val _user = MutableStateFlow(FirebaseUser())
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
                    sortDataChats {}
                }
            }
    }

    /**
     * This section contains the logic for the Firebase communication,
     * for handling different user availability states, based on app state.
     */

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

    fun reset() {
        updateOnlineStatus(false)
        auth.signOut()
        _chatData.value = emptyList()
        _friend.value = InternalChatInstance()
        _friendListData.value = emptyList()
        _user.value = FirebaseUser()
        previousRandChatUsersList.value = emptyList()
        dropInState.value = true
        randState.value = false
        isDataLoaded.value = false
    }


    fun sendDataToServer(data: String, onReceived: (String) -> Unit) {
        chatMateResponseState.value = ChatMateResponseState.Loading
        val url = "https://getresponse-ie4mphraqq-uc.a.run.app/getResponse?text=$data"
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()
        val request = Request.Builder().url(url).get().build()
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

    fun sendDeviceToken(data: String) {
        val url = "https://settoken-ie4mphraqq-uc.a.run.app/setToken"
        val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()

        val requestBody =
            FormBody.Builder().add("token", data).add("uid", auth.currentUser?.uid.toString())
                .build()

        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {

            }
        })
    }

    fun resetRandChat() {
        val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
        val client = OkHttpClient()
        val requestData =
            "{\"user\":\"${auth.currentUser?.uid.toString()}\",\"newUser\":${false},\"action\":\"disconnect\"}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestData.toRequestBody(mediaType)

        val request = Request.Builder().url(url).post(requestBody).build()

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
        onComplete: () -> Unit = {}
    ) {
        val url = "https://randchat-ie4mphraqq-uc.a.run.app/randChat"
        val client = OkHttpClient()
        val requestData =
            "{\"user\":\"${sharedViewModel.auth.currentUser?.uid.toString()}\", \"newUser\":${state}}"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestData.toRequestBody(mediaType)

        val request = Request.Builder().url(url).post(requestBody).build()

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
                            { getRandChat(sharedViewModel, false, navController) {} }, delayMillis
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
                    if (!previousRandChatUsersList.value.contains(personList)) {
                        previousRandChatUsersList.value += personList!!
                    }
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

    // Needed due to performance issues
    suspend fun markMessagesAsRead() {
        val chatRef =
            FirebaseFirestore.getInstance().collection("chats").document(friend.value.chatRoomID)
                .collection("messages")
        try {
            withContext(Dispatchers.IO) {
                val querySnapshot = chatRef.whereEqualTo("read", false).get().await()
                val batch = FirebaseFirestore.getInstance().batch()
                for (document in querySnapshot.documents) {
                    val sender = document.getString("sender")
                    if (sender != user.value.id) {
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
