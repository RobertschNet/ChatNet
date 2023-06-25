package at.htlhl.testing.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.testing.data.Message
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatView : ViewModel() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val _subCollectionData = MutableStateFlow<List<Message>>(emptyList())
    private val subCollectionData: StateFlow<List<Message>> get() = _subCollectionData

    private val _friendSubCollectionData = MutableStateFlow<List<Message>>(emptyList())
    private val friendSubCollectionData: StateFlow<List<Message>> get() = _friendSubCollectionData
    private suspend fun saveMessage(data: String?, message: Message) {
        db.collection("user/${auth.currentUser!!.uid}/friends/${data}/chat")
            .add(message)
            .await()
    }

    private var subCollectionDataListener: ListenerRegistration? = null
    private var friendSubCollectionDataListener: ListenerRegistration? = null
    private fun startListeningForSubCollectionData(
        mainDocumentId: String,
        subCollectionId: String,
    ) {
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(mainDocumentId)
        val subCollectionRef = documentRef.collection("/friends")
        val nestedSubCollectionRef =
            subCollectionRef.document(subCollectionId).collection("/chat").orderBy("timestamp")
        subCollectionDataListener?.remove()
        subCollectionDataListener =
            nestedSubCollectionRef.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                querySnapshot?.let { snapshot ->
                    val subCollectionData = snapshot.toObjects(Message::class.java)
                    _subCollectionData.value = subCollectionData
                }
            }
    }

    private fun startListeningForFriendSubCollectionData(
        mainDocumentId: String,
        subCollectionId: String
    ) {
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(mainDocumentId)
        val subCollectionRef = documentRef.collection("/friends")
        val nestedSubCollectionRef =
            subCollectionRef.document(subCollectionId).collection("/chat").orderBy("timestamp")
        friendSubCollectionDataListener?.remove()
        friendSubCollectionDataListener =
            nestedSubCollectionRef.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                querySnapshot?.let { snapshot ->
                    val subCollectionData = snapshot.toObjects(Message::class.java)
                    _friendSubCollectionData.value = subCollectionData
                }
            }
    }


    override fun onCleared() {
        super.onCleared()
        subCollectionDataListener?.remove()
        friendSubCollectionDataListener?.remove()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ChatScreen(
        navController: NavController,
        messages: List<Message>,
        onMessageSent: (Message) -> Unit,
        personList: PersonList,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val lazyListState = rememberLazyListState()
        auth = Firebase.auth
        val currentUser = auth.currentUser?.uid

        Scaffold(
            topBar = { MessageTopBar(navController, personList) },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    MessageList(
                        messages = messages,
                        scrollState = lazyListState,
                        coroutineScope = coroutineScope
                    )

                }
            }, bottomBar = {
                InputField { messageText ->
                    val message = Message(
                        userID = currentUser.toString(),
                        content = messageText,
                        timestamp = Timestamp.now()
                    )
                    onMessageSent(message)
                    coroutineScope.launch {
                    }
                }
            })
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageList(
        messages: List<Message>,
        scrollState: LazyListState,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch { scrollState.animateScrollToItem(messages.size) }
        LazyColumn(Modifier.padding(bottom = 70.dp), state = scrollState) {
            items(messages) { message ->
                MessageItem(
                    Message(
                        message.userID,
                        message.content,
                        message.timestamp
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageItem(message: Message) {
        val backgroundColor =
            if (message.userID == auth.currentUser?.uid) if (isSystemInDarkTheme()) Color.DarkGray
            else Color.White else if (isSystemInDarkTheme()) Color.Black else Color.LightGray
        val alignment =
            if (message.userID == auth.currentUser?.uid) Arrangement.End else Arrangement.Start
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime = message.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault())
            .toLocalTime().format(formatter)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = alignment,
            verticalAlignment = CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        if (isSystemInDarkTheme()) Color.White else Color.Black,
                        RoundedCornerShape(24.dp)
                    )
                    .background(backgroundColor, shape = RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Start
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = alignment,
            verticalAlignment = CenterVertically
        ) {
            Text(
                text = formattedTime,
                modifier = Modifier.padding(2.dp),
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Start
            )
        }

    }


    @Composable
    fun InputField(onMessageSent: (String) -> Unit) {
        var badgeCount by remember { mutableIntStateOf(0) }
        var text by remember { mutableStateOf("") }
        BasicTextField(
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotEmpty()) {
                        onMessageSent(text)
                        text = ""
                    }
                }
            ),
            value = text,
            onTextLayout = { textLayoutResult ->
                when (textLayoutResult.lineCount) {
                    1 -> {
                        badgeCount = 0
                    }

                    2 -> {
                        badgeCount = 12
                    }

                    3 -> {
                        badgeCount = 24
                    }

                    4 -> {
                        badgeCount = 36
                    }
                }
            },
            maxLines = 4,
            cursorBrush = Brush.verticalGradient(
                0.00f to Color.White,
                0.35f to Color.White,
                0.35f to Color.White,
                0.90f to Color.White,
                0.90f to Color.White,
                1.00f to Color.White
            ),
            onValueChange = { text = it },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .background(
                    if (isSystemInDarkTheme()) Color.Black else Color.White,
                    RoundedCornerShape(26.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    shape = RoundedCornerShape(26.dp),
                ),
            decorationBox = { innerTextField ->
                Column(
                    Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 6.dp),
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            imageVector = Icons.Outlined.EmojiEmotions,
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        )

                        Box(Modifier.padding(end = 95.dp)) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Message...",
                                    fontSize = 20.sp,
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                )
                            }
                            innerTextField()

                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp, top = 6.dp),
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (text.isEmpty()) {
                            Icon(
                                modifier = Modifier
                                    .size(30.dp),
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            )
                        } else {
                            Text(
                                text = "Send",
                                fontSize = 20.sp,
                                color = Color(0xFF00B1A9),
                                modifier = Modifier
                                    .padding(top = badgeCount.dp)
                                    .clickable {
                                        if (text.isNotEmpty()) {
                                            onMessageSent(text)
                                            text = ""
                                        }
                                    }
                            )
                        }
                    }
                }
            },
        )
    }

    @Composable
    fun MessageTopBar(navController: NavController, user: PersonList) {
        var favorite by remember { mutableStateOf(false) }
        var comment by remember { mutableStateOf(false) }
        var pin by remember { mutableStateOf(false) }
        TopAppBar(
            backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            title = {
                Text(
                    text = user.name,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(start = 5.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            actions = {
                IconButton(onClick = { favorite = !favorite }) {
                    Icon(
                        imageVector = if (favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
                IconButton(onClick = { pin = !pin }) {
                    Icon(
                        imageVector = if (pin) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
                IconButton(onClick = { comment = !comment }) {
                    Icon(
                        imageVector = if (comment) Icons.Outlined.CommentsDisabled else Icons.Outlined.Comment,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
            },
            navigationIcon = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        contentDescription = null,
                        modifier = Modifier
                            .align(CenterVertically)
                            .size(25.dp)
                            .clickable { navController.navigate(Screens.DropInScreen.Route) }
                    )
                    Image(
                        contentDescription = null,
                        painter = rememberAsyncImagePainter("https://www.w3schools.com/howto/img_avatar.png"),
                        modifier = Modifier
                            .clip(CircleShape)
                            .align(CenterVertically)
                            .size(40.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            },
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MutableCollectionMutableState")
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val viewModel = viewModel<ChatView>()
        val user = sharedViewModel.user.value
        auth = Firebase.auth
        val docID = auth.currentUser!!.uid
        val selectedMainDocumentIdState = remember { mutableStateOf(docID) }
        val selectedSubCollectionIdState = remember { mutableStateOf(user.userID) }
        val selectedMainDocumentId: String by selectedMainDocumentIdState
        val selectedSubCollectionId: String by selectedSubCollectionIdState
        val subCollectionDataState =
            viewModel.subCollectionData.collectAsState(initial = emptyList())
        val subCollectionData: List<Message> = subCollectionDataState.value
        val friendSubCollectionDataState =
            viewModel.friendSubCollectionData.collectAsState(initial = emptyList())
        val friendSubCollectionData: List<Message> = friendSubCollectionDataState.value
        LaunchedEffect(selectedMainDocumentId, selectedSubCollectionId) {
            viewModel.startListeningForSubCollectionData(
                selectedMainDocumentId,
                selectedSubCollectionId
            )
        }
        LaunchedEffect(selectedMainDocumentId, selectedSubCollectionId) {
            viewModel.startListeningForFriendSubCollectionData(
                selectedSubCollectionId,
                selectedMainDocumentId
            )
        }
        val onMessageSent: (Message) -> Unit = { message ->
            runBlocking {
                saveMessage(user.userID, message)
            }
        }
        ChatScreen(
            messages = friendSubCollectionData.plus(subCollectionData).sortedBy { it.timestamp },
            onMessageSent = onMessageSent,
            navController = navController,
            personList = user
        )
    }
}