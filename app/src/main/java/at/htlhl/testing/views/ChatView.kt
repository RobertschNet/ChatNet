package at.htlhl.testing.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.testing.data.Chat
import at.htlhl.testing.data.Message
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatView : ViewModel() {
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ChatScreen(
        navController: NavController,
        messages: List<Message>,
        onMessageSent: (Message) -> Unit,
        personList: PersonList,
        viewModel: SharedViewModel,
        documentId: String,
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
                        viewModel = viewModel,
                        messages = messages,
                        scrollState = lazyListState,
                        coroutineScope = coroutineScope,
                        documentId = documentId
                    )

                }
            }, bottomBar = {
                InputField { messageText ->
                    val message = Message(
                        sender = currentUser.toString(),
                        content = messageText,
                        timestamp = Timestamp.now()
                    )
                    onMessageSent(message)
                }
            })
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageList(
        viewModel: SharedViewModel,
        messages: List<Message>,
        scrollState: LazyListState,
        coroutineScope: CoroutineScope,
        documentId: String
    ) {
        coroutineScope.launch { scrollState.animateScrollToItem(messages.size) }
        LazyColumn(Modifier.padding(bottom = 70.dp), state = scrollState) {
            items(messages) { message ->
                MessageItem(
                    viewModel = viewModel,
                    Message(
                        message.sender,
                        message.content,
                        message.timestamp
                    ),documentId
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageItem(viewModel: SharedViewModel, message: Message, documentId: String) {
        val backgroundColor =
            if (message.sender == auth.currentUser?.uid) if (isSystemInDarkTheme()) Color.DarkGray
            else Color.White else if (isSystemInDarkTheme()) Color.Black else Color.LightGray
        val alignment =
            if (message.sender == auth.currentUser?.uid) Arrangement.End else Arrangement.Start
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime = message.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault())
            .toLocalTime().format(formatter)
        var openDialog by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = alignment,
                verticalAlignment = CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (message.sender == auth.currentUser?.uid) {
                                        openDialog = true
                                    }
                                }
                            )
                        }
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
            if (openDialog) {
                AlertDialog(
                    onDismissRequest = { openDialog = false },
                    title = { Text(text = "Delete Message") },
                    text = { Text(text = "Are you sure you want to delete this message?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteMessageForUser(
                                    documentId,
                                    message.timestamp
                                )
                                openDialog = false
                            }
                        ) {
                            Text(text = "Delete for me")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openDialog = false
                            }
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = alignment,
                verticalAlignment = CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 14.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
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
                            modifier = Modifier
                                .size(30.dp)
                                .clickable(onClick = {}),
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
    @SuppressLint("MutableCollectionMutableState", "CoroutineCreationDuringComposition")
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        auth = Firebase.auth
        val user = sharedViewModel.user.value
        val documentIdState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val documentationId: List<Chat> = documentIdState.value

        val filteredChats = documentationId.filter { chat ->
            chat.participants.contains(user.userID) && chat.participants.contains(auth.currentUser?.uid.toString())
        }

        val doc = filteredChats.firstOrNull()?.chatRoomID ?: ""

        val messageList: List<Message> = filteredChats.flatMap { chat ->
            chat.messages.map { message ->
                Message(message.sender, message.content, message.timestamp)
            }
        }
        println("ChatViewScreen: $documentationId")

        val onMessageSent: (Message) -> Unit = { message ->
            runBlocking {
                sharedViewModel.saveMessages(doc, message)

                //sharedViewModel.saveLastMessage(user.userID, message)
            }
        }
        ChatScreen(
            viewModel = sharedViewModel,
            messages = messageList,
            onMessageSent = onMessageSent,
            navController = navController,
            personList = user,
            documentId = doc,
        )

    }
}


