package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.BlockUserDialog
import at.htlhl.chatnet.ui.components.mixed.ChatViewMessageComponent
import at.htlhl.chatnet.ui.components.mixed.DeleteMessageDialog
import at.htlhl.chatnet.ui.components.mixed.InputField
import at.htlhl.chatnet.ui.components.mixed.MessageTopBar
import at.htlhl.chatnet.ui.components.mixed.OptionsDialog
import at.htlhl.chatnet.ui.components.mixed.UnblockToMessageDialog
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatView : ViewModel() {
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChat> = chatDataState.value
        val matchingChat = chatData.find { chat ->
            chat.chatRoomID == sharedViewModel.friend.value.chatRoomID
        }
        val chatMateChat = matchingChat?.tab == "chatmate"
        val chatRoomId = matchingChat?.chatRoomID ?: ""
        val messageListFromMatchingChat: List<InternalMessageInstance> = matchingChat?.let { chat ->
            chat.messages.map { message ->
                InternalMessageInstance(
                    id = message.id,
                    sender = message.sender,
                    image = message.image,
                    read = message.read,
                    content = message.content,
                    timestamp = message.timestamp,
                    visible = message.visible,
                )
            }
        } ?: emptyList()
        val onMessageSent: (FirebaseMessage) -> Unit = { message ->
            if (chatMateChat) {
                sharedViewModel.chatMateResponseState.value = ChatMateResponseState.Loading
                sharedViewModel.sendDataToServer(message.content) { response ->
                    runBlocking {
                        sharedViewModel.saveMessages(
                            documentId = chatRoomId,
                            message = FirebaseMessage(
                                sender = "chatmate",
                                content = response,
                                timestamp = Timestamp.now(),
                                read = false,
                                image = "",
                                visible = listOf(
                                    sharedViewModel.auth.currentUser?.uid.toString(),
                                )
                            )
                        )
                    }
                }
            }
            runBlocking {
                sharedViewModel.saveMessages(documentId = chatRoomId, message = message)
            }
        }
        sharedViewModel.markMessagesAsRead(user = sharedViewModel.friend.value)
        sharedViewModel.updateMarkAsReadStatus(isAlreadyUnread = true)
        ChatViewContentStructure(
            sharedViewModel = sharedViewModel,
            messagesForChat = messageListFromMatchingChat,
            onMessageSent = onMessageSent,
            navController = navController,
            chatRoomId = chatRoomId,
            chatMateChat = chatMateChat
        )
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun ChatViewContentStructure(
        navController: NavController,
        messagesForChat: List<InternalMessageInstance>,
        onMessageSent: (FirebaseMessage) -> Unit,
        sharedViewModel: SharedViewModel,
        chatRoomId: String,
        chatMateChat: Boolean
    ) {
        Log.println(Log.ERROR, "ChatView", messagesForChat.toString())
        val coroutineScope = rememberCoroutineScope()
        var blockDialog by remember { mutableStateOf(false) }
        var unblockDialog by remember { mutableStateOf(false) }
        val filteredMessages = messagesForChat.filter { message ->
            message.visible.contains(sharedViewModel.auth.currentUser?.uid.toString())
        }.toMutableList()
        val lazyListState =
            rememberLazyListState(initialFirstVisibleItemIndex = sharedViewModel.getMessageLengthForChat()!!)
        Scaffold(
            modifier = Modifier
                .imePadding()
                .onSizeChanged { coroutineScope.launch { lazyListState.scrollToItem(messagesForChat.size) } },
            topBar = {
                MessageTopBar(
                    chatInstance = sharedViewModel.friend.value,
                    sharedViewModel = sharedViewModel
                ) {
                    if (it == "return") {
                        navController.navigate(
                            if (chatMateChat) Screens.ChatMateScreen.route else Screens.ChatsViewScreen.route
                        )
                    } else {
                        blockDialog = true
                    }
                }
            },
            content = {
                it.calculateBottomPadding()
                ChatViewContentList(
                    sharedViewModel = sharedViewModel,
                    chatMateChat = chatMateChat,
                    messages = filteredMessages,
                    lazyListState = lazyListState,
                    chatRoomId = chatRoomId
                )
            }, bottomBar = {
                InputField(
                    sharedViewModel = sharedViewModel,
                    navController = navController,
                    chatMateChat = chatMateChat,
                    onMessageSent = { messageText, image ->
                        if (!sharedViewModel.user.value.blocked.contains(sharedViewModel.friend.value.personList.id)) {
                            onMessageSent(
                                FirebaseMessage(
                                    sender = sharedViewModel.auth.currentUser?.uid.toString(),
                                    content = messageText,
                                    timestamp = Timestamp.now(),
                                    read = false,
                                    image = image,
                                    visible =
                                    if (sharedViewModel.friend.value.personList.blocked.contains(
                                            sharedViewModel.user.value.id
                                        )
                                    ) {
                                        listOf(
                                            sharedViewModel.auth.currentUser?.uid.toString(),
                                        )
                                    } else {
                                        listOf(
                                            sharedViewModel.auth.currentUser?.uid.toString(),
                                            sharedViewModel.friend.value.personList.id
                                        )
                                    }
                                )
                            )
                        } else {
                            unblockDialog = true
                        }
                    },
                )
            }
        )
        if (blockDialog) {
            BlockUserDialog(
                friend = sharedViewModel.friend.value,
                user = sharedViewModel.user.value
            ) { value ->
                if (value == "blocked") {
                    sharedViewModel.updateBlockedUserList(
                        sharedViewModel.user.value.blocked.contains(
                            sharedViewModel.friend.value.personList.id
                        )
                    )
                }
                blockDialog = false
            }
        }
        if (unblockDialog) {
            UnblockToMessageDialog(
                friend = sharedViewModel.friend.value,
            ) { value ->
                if (value == "unblock") {
                    sharedViewModel.updateBlockedUserList(true)
                }
                unblockDialog = false
            }
        }
    }

    @Composable
    fun ChatViewContentList(
        sharedViewModel: SharedViewModel,
        chatMateChat: Boolean,
        messages: List<InternalMessageInstance>,
        lazyListState: LazyListState,
        chatRoomId: String
    ) {
        var animatedText by remember { mutableStateOf("Robert is writing") }
        var unblockDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
            while (true) {
                delay(750)
                animatedText = "ChatMate is thinking."
                delay(750)
                animatedText = "ChatMate is thinking.."
                delay(750)
                animatedText = "ChatMate is thinking..."
                delay(750)
                animatedText = "ChatMate is thinking..."
            }
        }
        LaunchedEffect(messages.size) {
            lazyListState.animateScrollToItem(messages.size)
        }
        val filteredMessages = messages.filter {
            it.content.contains(sharedViewModel.searchValue.value, ignoreCase = true)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LaunchedEffect(sharedViewModel.searchValue.value) {
                coroutineScope.launch {
                    lazyListState.scrollToItem(filteredMessages.size)
                }
            }
            LazyColumn(modifier = Modifier.padding(bottom = 70.dp), state = lazyListState) {
                items(filteredMessages) { message ->
                    val messageIndex = messages.indexOf(message)
                    val previousMessageIndex =
                        if (messageIndex > 0) messages.getOrNull(messageIndex - 1) else null
                    val nextMessageIndex =
                        if (messageIndex > 0) messages.getOrNull(messageIndex + 1) else null
                    MessageItem(
                        sharedViewModel = sharedViewModel,
                        chatMateChat = chatMateChat,
                        previousMessage = previousMessageIndex,
                        nextMessage = nextMessageIndex,
                        message = InternalMessageInstance(
                            id = message.id,
                            sender = message.sender,
                            image = message.image,
                            read = message.read,
                            content = message.content,
                            timestamp = message.timestamp,
                            visible = message.visible,
                        ), chatRoomId = chatRoomId
                    )
                }
                item {
                    if (isBlockSeparatorNeeded(sharedViewModel)) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center, modifier = Modifier
                                    .background(
                                        if (isSystemInDarkTheme()) Color.DarkGray else Color(
                                            0xFFF5F5F5
                                        ),
                                        RoundedCornerShape(30)
                                    )
                                    .clickable { unblockDialog = true }
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "You have blocked this user",
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun MessageItem(
        sharedViewModel: SharedViewModel,
        chatMateChat: Boolean,
        message: InternalMessageInstance,
        previousMessage: InternalMessageInstance?,
        nextMessage: InternalMessageInstance?,
        chatRoomId: String
    ) {
        val context = LocalContext.current
        var menuDialog by remember { mutableStateOf(false) }
        var isFullScreenImageDialog by remember { mutableStateOf(false) }
        var fullScreenImage by remember { mutableStateOf("") }
        var deleteDialog by remember { mutableStateOf(false) }
        val anchorPosition = remember { mutableStateOf<Offset?>(null) }
        val isUser = message.sender == sharedViewModel.auth.currentUser?.uid
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            TODO("VERSION.SDK_INT < S")
        }
        ChatViewMessageComponent(
            sharedViewModel = sharedViewModel,
            isUser = isUser,
            context = context,
            chatMateChat = chatMateChat,
            previousMessage = previousMessage,
            nextMessage = nextMessage,
            message = message,
            onClick = {
                fullScreenImage = message.image
                isFullScreenImageDialog = true
            },
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
        if (deleteDialog) {
            DeleteMessageDialog(isUser = isUser) { value ->
                if (value == "delete") {
                    sharedViewModel.deleteMessage(chatRoomId, message.id)
                } else if (value == "change") {
                    sharedViewModel.changeMessageVisibility(chatRoomId, message.id)
                }
                deleteDialog = false
            }
        }

        if (menuDialog) {
            OptionsDialog(offset = anchorPosition.value) { value ->
                when (value) {
                    "delete" -> {
                        deleteDialog = true
                    }

                    "copy" -> {
                        sharedViewModel.copyToClipboard(context, message.content)
                    }

                    "generate" -> {
                        sharedViewModel.sendDataToServer(message.content) {
                            sharedViewModel.text.value = it
                        }
                    }
                }
                menuDialog = false
            }
        }
        if (isFullScreenImageDialog) {
            FullScreenImageDialog(imageUrl = fullScreenImage) {
                isFullScreenImageDialog = false
            }
        }
    }

    private fun isBlockSeparatorNeeded(
        sharedViewModel: SharedViewModel
    ): Boolean {
        return sharedViewModel.user.value.blocked.contains(sharedViewModel.friend.value.personList.id)
    }

    @Composable
    fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
        Dialog(
            onDismissRequest = { onDismiss.invoke() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}