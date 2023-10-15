package at.htlhl.testing.views

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material3.Divider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.testing.data.Chat
import at.htlhl.testing.data.Message
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.data.ShownUsers
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatView : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ChatScreen(
        navController: NavController,
        messages: List<Message>,
        onMessageSent: (Message) -> Unit,
        personList: ShownUsers,
        viewModel: SharedViewModel,
        documentId: String,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val lazyListState = rememberLazyListState()
        val filteredMessages = messages.filter { message ->
            message.visible.contains(viewModel.auth.currentUser?.uid.toString())
        }
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
                        messages = filteredMessages,
                        scrollState = lazyListState,
                        coroutineScope = coroutineScope,
                        documentId = documentId
                    )
                }
            }, bottomBar = {
                InputField(
                    viewModel
                ) { messageText ->
                    val message = Message(
                        sender = viewModel.auth.currentUser?.uid.toString(),
                        content = messageText,
                        timestamp = Timestamp.now(),
                        read = false,
                        type = "text",
                        visible = listOf(
                            viewModel.auth.currentUser?.uid.toString(),
                            personList.personList.id
                        )
                    )
                    onMessageSent(message)
                }
            })
    }


    private fun copyToClipboard(context: Context, text: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Message", text)
        clipboardManager.setPrimaryClip(clip)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageList(
        viewModel: SharedViewModel,
        messages: List<Message>,
        scrollState: LazyListState,
        coroutineScope: CoroutineScope,
        documentId: String
    ) {
        LaunchedEffect(messages.size) {
            scrollState.animateScrollToItem(messages.size)
        }
        LazyColumn(Modifier.padding(bottom = 70.dp), state = scrollState) {
            items(messages) { message ->
                MessageItem(
                    viewModel = viewModel,
                    Message(
                        sender = message.sender,
                        type = "text",
                        read = false,
                        content = message.content,
                        timestamp = message.timestamp,
                        visible = message.visible,
                    ), documentId
                )
            }
        }
    }

    @Composable
    fun DeleteDialog(
        isUser: Boolean,
        sharedViewModel: SharedViewModel,
        onClose: (String) -> Unit = {}
    ) {
        Dialog(
            onDismissRequest = { onClose.invoke("closed") },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .width(250.dp)
                    .height(if (isUser) 240.dp else 200.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Message?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = if (!isUser) "This message can be deleted only for you, and not for everyone." else "This message can be deleted only for you, or for everyone in the chat.",
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        top = 10.dp,
                        bottom = 20.dp,
                        start = 10.dp,
                        end = 10.dp
                    )
                )
                Divider(
                    thickness = 0.3f.dp,
                    color = Color.LightGray,
                )
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClose.invoke("change") }
                ) {
                    Text(
                        text = "Delete for me",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                    )
                }
                if (isUser) {
                    Divider(
                        thickness = 0.3f.dp,
                        color = Color.LightGray,
                    )
                    Row(
                        verticalAlignment = CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClose.invoke("delete")
                            }
                    ) {
                        Text(
                            text = "Delete for everyone",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                        )
                    }
                }
                Divider(
                    thickness = 0.3f.dp,
                    color = Color.LightGray,
                )
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClose.invoke("closed") }
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun MenuDialog(offset: Offset?, onClose: (String) -> Unit = {}) {
        offset?.let {
            DropdownMenu(
                expanded = true,
                properties = PopupProperties(focusable = false),
                modifier = Modifier
                    .height(130.dp)
                    .width(120.dp),
                onDismissRequest = {
                    onClose.invoke("closed")
                },
                offset = DpOffset(it.x.dp, it.y.dp),
            ) {
                DropdownMenuItem(
                    onClick = {
                        onClose.invoke("reply")
                    },
                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.height(40.dp)

                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = CenterVertically,
                    ) {
                        Text(
                            "Reply",
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 10.dp)

                        )
                        Icon(
                            imageVector = Icons.Outlined.Reply,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 40.dp)
                                .size(25.dp)

                        )
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        onClose.invoke("copy")
                    },
                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = CenterVertically,
                    ) {

                        Text(
                            "Copy",
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(start = 10.dp)

                        )
                        Icon(
                            imageVector = Icons.Outlined.FileCopy,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 42.dp)
                                .size(25.dp)
                        )
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        onClose.invoke("delete")
                    },
                    contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.height(40.dp)

                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = CenterVertically,
                    ) {
                        Text(
                            "Delete",
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            tint = Color.Red,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 35.dp, end = 5.dp)
                                .size(25.dp)
                        )
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MessageItem(viewModel: SharedViewModel, message: Message, documentId: String) {
        val isUser = message.sender == viewModel.auth.currentUser?.uid
        val backgroundColor = if (isUser) {
            if (isSystemInDarkTheme()) Color.DarkGray else Color.White
        } else {
            if (isSystemInDarkTheme()) Color.Black else Color.LightGray
        }
        val alignment = if (isUser) Arrangement.End else Arrangement.Start
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime =
            message.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .format(formatter)
        var menuDialog by remember { mutableStateOf(false) }
        var deleteDialog by remember { mutableStateOf(false) }
        val anchorPosition = remember { mutableStateOf<Offset?>(null) }
        val context = LocalContext.current
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            TODO("VERSION.SDK_INT < S")
        }





        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = alignment,
                verticalAlignment = CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(
                            start = if (isUser) 80.dp else 10.dp,
                            end = if (isUser) 10.dp else 80.dp,
                            top = 25.dp,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    if (isUser) {
                                        anchorPosition.value = Offset(200f, 0f)
                                    } else {
                                        anchorPosition.value = Offset(20f, 0f)
                                    }
                                    val effect = VibrationEffect.createOneShot(
                                        100,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                    vibrator.defaultVibrator.vibrate(effect)
                                    menuDialog = true
                                }
                            )
                        }
                        .border(
                            1.dp,
                            if (isSystemInDarkTheme()) Color.White else Color.Black,
                            RoundedCornerShape(24.dp)
                        )
                        .background(backgroundColor, shape = RoundedCornerShape(24.dp))
                        .padding(top=4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                ) {
                    val messageContent = message.content
                    val maxLineLength = 30
                    val words = messageContent.split("\\s+".toRegex())
                    val lines = StringBuilder()
                    var currentLine = StringBuilder()

                    for (word in words) {
                        if (word.length > maxLineLength) {
                            if (currentLine.isNotEmpty()) {
                                lines.append('\n')
                            }
                            for (i in word.indices step maxLineLength) {
                                val endIndex = (i + maxLineLength).coerceAtMost(word.length)
                                val subWord = word.substring(i, endIndex)
                                if (currentLine.isNotEmpty()) {
                                    currentLine.append(' ')
                                }
                                currentLine.append(subWord)
                                if (currentLine.length >= maxLineLength) {
                                    lines.append(currentLine)
                                    currentLine = StringBuilder()
                                }
                            }
                        } else if (currentLine.isNotEmpty() && currentLine.length + word.length + 1 <= maxLineLength) {
                            currentLine.append(' ')
                            currentLine.append(word)
                        } else if (currentLine.isNotEmpty()) {
                            lines.append(currentLine)
                            lines.append('\n')
                            currentLine = StringBuilder(word)
                        } else {
                            currentLine = StringBuilder(word)
                        }
                    }

                    if (currentLine.isNotEmpty()) {
                        lines.append(currentLine)
                    }

                    Box(
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = lines.toString(),
                            fontSize = 14.sp,
                            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(backgroundColor, shape = RoundedCornerShape(24.dp)),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            textAlign = TextAlign.End,
                            modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(top = 30.dp, end = 10.dp)
                        )
                    }
                }
            }






            if (deleteDialog) {
                DeleteDialog(isUser = isUser, sharedViewModel = viewModel) { value ->
                    if (value == "delete") {
                        viewModel.deleteMessage(documentId, message.timestamp)
                    } else if (value == "change") {
                        viewModel.changeMessageVisibility(documentId, message.timestamp)
                    }
                    deleteDialog = false
                }
            }
            if (menuDialog) {
                MenuDialog(anchorPosition.value) { value ->
                    if (value == "delete") {
                        deleteDialog = true
                    } else if (value == "copy") {
                        copyToClipboard(context, message.content)
                    }
                    menuDialog = false
                }
            }
        }
    }


    @Composable
    fun InputField(sharedViewModel: SharedViewModel, onMessageSent: (String) -> Unit) {
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
                                    .size(30.dp)
                                    .clickable {
                                        sharedViewModel.imageCall.value = true
                                    },
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
    fun MessageTopBar(navController: NavController, user: ShownUsers) {
        var favorite by remember { mutableStateOf(false) }
        var comment by remember { mutableStateOf(false) }
        var pin by remember { mutableStateOf(false) }
        TopAppBar(
            backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            title = {
                Text(
                    text = user.personList.username["mixedcase"].toString(),
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
                            .clickable { navController.navigate(Screens.Chats.route) }
                    )
                    Image(
                        contentDescription = null,
                        painter = rememberAsyncImagePainter(user.personList.image),
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
        sharedViewModel.bottomBarState.value = false
        val user = sharedViewModel.friend.value
        val documentIdState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val documentationId: List<Chat> = documentIdState.value
        Log.println(Log.INFO, "ChatView", documentationId.toString())
        val filteredChats = documentationId.filter { chat ->
            chat.members.contains(user.personList.id) && chat.members.contains(sharedViewModel.auth.currentUser?.uid.toString())
        }
        val doc = filteredChats.firstOrNull()?.chatRoomID ?: ""
        Log.println(Log.INFO, "ChatView", doc)
        val messageList: List<Message> = filteredChats.flatMap { chat ->
            chat.messages.map { message ->
                Message(
                    sender = message.sender,
                    type = "text",
                    read = false,
                    content = message.content,
                    timestamp = message.timestamp,
                    visible = message.visible,
                )
            }
        }
        sharedViewModel.markMessagesAsRead(user)
        val onMessageSent: (Message) -> Unit = { message ->
            runBlocking {
                sharedViewModel.saveMessages(doc, message)
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


