package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.ChatViewMessageComponent
import at.htlhl.chatnet.ui.components.mixed.DeleteMessageDialog
import at.htlhl.chatnet.ui.components.mixed.InputField
import at.htlhl.chatnet.ui.components.mixed.MessageTopBar
import at.htlhl.chatnet.ui.components.mixed.OptionsDialog
import at.htlhl.chatnet.ui.components.randchat.LoadingChat
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class RandChatView {
    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RandChatScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val pageState = rememberPagerState { 2 }
        if (pageState.currentPage == 1 && !pageState.isScrollInProgress) {
            sharedViewModel.randState.value = false
            coroutineScope.launch { pageState.scrollToPage(0) }
            sharedViewModel.getRandChat(sharedViewModel, true, navController) {}
        }
        HorizontalPager(
            state = pageState,
            beyondBoundsPageCount = 1,
            userScrollEnabled = sharedViewModel.randState.value
        ) { page ->
            when (page) {
                0 -> {
                    if (sharedViewModel.randState.value) {
                        if (!sharedViewModel.isConnected.value) {
                            LaunchedEffect(Unit) {
                                Toast.makeText(
                                    context,
                                    "User left\nSwipe right to search for another user",
                                    Toast.LENGTH_SHORT
                                ).show()
                                delay(500)
                                pageState.animateScrollBy(150f)
                                delay(1000)
                                pageState.animateScrollBy(-150f)
                            }
                        }
                        ChatViewScreen(
                            navController = navController,
                            sharedViewModel = sharedViewModel
                        )
                    } else {
                        LoadingChat(navController)
                    }
                }

                1 -> {
                    LoadingChat(navController)
                }
            }
        }
    }

    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {

        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChat> = chatDataState.value
        val matchingChat = chatData.find { chat ->
            chat.chatRoomID == sharedViewModel.friend.value.chatRoomID
        }
        val chatMateChat = matchingChat?.tab == "chatmate"
        val chatRoomId = matchingChat?.chatRoomID ?: ""
        val messageListFromMatchingChat: List<FirebaseMessage> = matchingChat?.let { chat ->
            chat.messages.map { message ->
                FirebaseMessage(
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
                                id = message.id,
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

    @Composable
    fun ChatViewContentStructure(
        navController: NavController,
        messagesForChat: List<FirebaseMessage>,
        onMessageSent: (FirebaseMessage) -> Unit,
        sharedViewModel: SharedViewModel,
        chatRoomId: String,
        chatMateChat: Boolean
    ) {
        val coroutineScope = rememberCoroutineScope()
        val filteredMessages = messagesForChat.filter { message ->
            message.visible.contains(sharedViewModel.auth.currentUser?.uid.toString())
        }.toMutableList()
        val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
        Scaffold(
            modifier = Modifier
                .imePadding()
                .onSizeChanged {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(
                            messagesForChat.size
                        )
                    }
                },
            topBar = {
                MessageTopBar(
                    chatInstance = sharedViewModel.friend.value,
                    sharedViewModel = sharedViewModel
                ) {
                    navController.navigate(
                        Screens.RandChatStartScreen.route
                    )
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
                        onMessageSent(
                            FirebaseMessage(
                                id = "",
                                sender = sharedViewModel.auth.currentUser?.uid.toString(),
                                content = messageText,
                                timestamp = Timestamp.now(),
                                read = false,
                                image = image,
                                visible = listOf(
                                    sharedViewModel.auth.currentUser?.uid.toString(),
                                    sharedViewModel.friend.value.personList.id
                                )
                            )
                        )
                    },
                )
            }
        )
    }

    @Composable
    fun ChatViewContentList(
        sharedViewModel: SharedViewModel,
        chatMateChat: Boolean,
        messages: List<FirebaseMessage>,
        lazyListState: LazyListState,
        chatRoomId: String
    ) {
        var animatedText by remember { mutableStateOf("Robert is writing") }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(modifier = Modifier.padding(bottom = 70.dp), state = lazyListState) {
                items(messages) { message ->
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
                        message = FirebaseMessage(
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
            }

        }
    }


    @Composable
    fun MessageItem(
        sharedViewModel: SharedViewModel,
        chatMateChat: Boolean,
        message: FirebaseMessage,
        previousMessage: FirebaseMessage?,
        nextMessage: FirebaseMessage?,
        chatRoomId: String
    ) {
        val context = LocalContext.current
        var menuDialog by remember { mutableStateOf(false) }
        var deleteDialog by remember { mutableStateOf(false) }
        val anchorPosition = remember { mutableStateOf<Offset?>(null) }
        val isUser = message.sender == sharedViewModel.auth.currentUser?.uid
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            TODO("VERSION.SDK_INT < S")
        }
        ChatViewMessageComponent(
            isUser = isUser,
            context = context,
            chatMateChat = chatMateChat,
            previousMessage = previousMessage,
            nextMessage = nextMessage,
            message = message,
            onClick = {},
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
    }
}