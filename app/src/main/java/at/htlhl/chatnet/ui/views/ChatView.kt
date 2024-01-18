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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.BlockUserDialog
import at.htlhl.chatnet.ui.components.mixed.ChatViewMessageComponent
import at.htlhl.chatnet.ui.components.mixed.ChatViewTopBar
import at.htlhl.chatnet.ui.components.mixed.DeleteMessageDialog
import at.htlhl.chatnet.ui.components.mixed.InputField
import at.htlhl.chatnet.ui.components.mixed.OptionsDialog
import at.htlhl.chatnet.ui.components.mixed.UnblockToMessageDialog
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatView : ViewModel() {
    @SuppressLint("StateFlowValueCalledInComposition")
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = true)
        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChat> = chatDataState.value
        val friendDataState = sharedViewModel.friend.collectAsState()
        val friendData: InternalChatInstance = friendDataState.value
        val matchingChat = chatData.find { chat ->
            chat.chatRoomID == friendData.chatRoomID
        }
        val chatMateChat = matchingChat?.tab == "chatmate"
        val chatRoomId = matchingChat?.chatRoomID ?: ""
        Log.println(Log.ERROR, "ssssss", matchingChat.toString())

        val messageListFromMatchingChat: List<InternalMessageInstance> = matchingChat?.let { chat ->
            chat.messages.map { message ->
                InternalMessageInstance(
                    isFromCache = message.isFromCache,
                    id = message.id,
                    sender = message.sender,
                    images = message.images,
                    read = message.read,
                    text = message.text,
                    timestamp = message.timestamp,
                    visible = message.visible,
                )
            }
        } ?: emptyList()
        sharedViewModel.imageList.value =
            createImageList(messageListFromMatchingChat, sharedViewModel)
        sharedViewModel.markMessagesAsRead(user = friendData)
        sharedViewModel.updateMarkAsReadStatus(isAlreadyUnread = true)
        ChatViewContentStructure(
            sharedViewModel = sharedViewModel,
            messagesForChat = messageListFromMatchingChat,
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
        sharedViewModel: SharedViewModel,
        chatRoomId: String,
        chatMateChat: Boolean
    ) {
        val coroutineScope = rememberCoroutineScope()
        var chatMateResponse by remember { mutableStateOf("") }
        var blockDialog by remember { mutableStateOf(false) }
        var unblockDialog by remember { mutableStateOf(false) }
        val filteredMessages = messagesForChat.filter { message ->
            message.visible.contains(sharedViewModel.auth.currentUser?.uid.toString())
        }.toMutableList()
        val lazyListState = rememberLazyListState()
        Scaffold(
            modifier = Modifier
                .imePadding(),
            topBar = {
                ChatViewTopBar(
                    navController = navController,
                    chatInstance = sharedViewModel.friend.value,
                    sharedViewModel = sharedViewModel
                ) {
                    if (it == "return") {
                        navController.navigate(if (chatMateChat) Screens.ChatMateScreen.route else Screens.ChatsViewScreen.route)
                    } else {
                        blockDialog = true
                    }
                }
            },
            content = {
                ChatViewContentList(
                    sharedViewModel = sharedViewModel,
                    paddingValues = it,
                    chatMateChat = chatMateChat,
                    messages = filteredMessages,
                    lazyListState = lazyListState,
                    chatRoomId = chatRoomId,
                    navController = navController,
                    chatMateResponse = { chatMateResponse = it }
                )
            }, bottomBar = {
                InputField(
                    onChatMateResponse = chatMateResponse,
                    sharedViewModel = sharedViewModel,
                    navController = navController,
                    chatMateChat = chatMateChat,
                ){
                    unblockDialog = true
                }
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
        paddingValues: PaddingValues,
        navController: NavController,
        chatMateChat: Boolean,
        messages: List<InternalMessageInstance>,
        lazyListState: LazyListState,
        chatRoomId: String,
        chatMateResponse: (String) -> Unit
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
            lazyListState.animateScrollToItem(0)
        }
        val filteredMessages = messages.filter {
            it.text.contains(sharedViewModel.searchValue.value, ignoreCase = true)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier.padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    top = paddingValues.calculateTopPadding()
                ),
                state = lazyListState
            ) {
                items(
                    key = { it.id },
                    items = filteredMessages
                ) { message ->
                    val messageIndex = messages.indexOf(message)
                    val previousMessageIndex = messages.getOrNull(messageIndex + 1) // Reverse the calculation
                    val nextMessageIndex = messages.getOrNull(messageIndex - 1)   // Reverse the calculation
                    MessageItem(
                        sharedViewModel = sharedViewModel,
                        chatMateChat = chatMateChat,
                        previousMessage = previousMessageIndex,
                        nextMessage = nextMessageIndex,
                        message = InternalMessageInstance(
                            isFromCache = message.isFromCache,
                            id = message.id,
                            sender = message.sender,
                            images = message.images,
                            read = message.read,
                            text = message.text,
                            timestamp = message.timestamp,
                            visible = message.visible,
                        ), chatRoomId = chatRoomId, onClick = { image ->
                            sharedViewModel.imagePosition.intValue =
                                sharedViewModel.imageList.value.find { it.images[0] == image }
                                    ?.let { sharedViewModel.imageList.value.indexOf(it) } ?: 0
                            navController.navigate(Screens.ImageViewScreen.route)
                        }, chatMateResponse = { chatMateResponse.invoke(it) }
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
        chatRoomId: String,
        onClick: (String) -> Unit,
        chatMateResponse: (String) -> Unit
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
            sharedViewModel = sharedViewModel,
            isUser = isUser,
            chatMateChat = chatMateChat,
            previousMessage = previousMessage,
            nextMessage = nextMessage,
            message = message,
            onClick = {
                onClick.invoke(it)
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
                        sharedViewModel.copyToClipboard(context, message.text)
                    }

                    "generate" -> {
                        sharedViewModel.sendDataToServer(message.text) {
                            Log.println(Log.ERROR, "walllumm", it)
                            chatMateResponse.invoke(it)
                        }
                    }
                }
                menuDialog = false
            }
        }
    }

    private fun isBlockSeparatorNeeded(
        sharedViewModel: SharedViewModel
    ): Boolean {
        return sharedViewModel.user.value.blocked.contains(sharedViewModel.friend.value.personList.id)
    }

    private fun createImageList(
        messages: List<InternalMessageInstance>,
        sharedViewModel: SharedViewModel
    ): List<InternalMessageInstance> {
        val imageList = arrayListOf<InternalMessageInstance>()
        messages.forEach {
            if (it.images.isNotEmpty()) {
                it.images.forEach { image ->
                    if (it.visible.contains(sharedViewModel.auth.currentUser?.uid.toString())) {
                        imageList.add(
                            InternalMessageInstance(
                                isFromCache = it.isFromCache,
                                id = it.id,
                                sender = it.sender,
                                images = arrayListOf(image),
                                read = it.read,
                                text = it.text,
                                timestamp = it.timestamp,
                                visible = it.visible
                            )
                        )
                    }
                }
            }
        }
        return imageList
    }
}