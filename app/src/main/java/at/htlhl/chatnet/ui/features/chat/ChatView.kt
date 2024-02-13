package at.htlhl.chatnet.ui.features.chat

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.BlockUserDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteMessageDialog
import at.htlhl.chatnet.ui.features.dialogs.OptionsDialog
import at.htlhl.chatnet.ui.features.dialogs.UnblockToMessageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatViewMessageComponent
import at.htlhl.chatnet.ui.features.mixed.ChatViewTopBar
import at.htlhl.chatnet.ui.features.mixed.InputField
import at.htlhl.chatnet.util.copyToClipboard
import at.htlhl.chatnet.util.firebase.markMessagesAsRead
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.util.firebase.updateMarkAsUnreadStatus
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatView {
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {

        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )
        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChat> = chatDataState.value
        val friendDataState =
            sharedViewModel.friend.collectAsState(initial = InternalChatInstance())
        val friendData: InternalChatInstance = friendDataState.value
        val userDataState = sharedViewModel.user.collectAsState(initial = FirebaseUser())
        val userData: FirebaseUser = userDataState.value
        val matchingChat = chatData.find { chat ->
            chat.chatRoomID == friendData.chatRoomID
        }
        val chatMateChat = matchingChat?.tab == "chatmate"
        val chatRoomId = matchingChat?.chatRoomID ?: ""
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
        var chatMateResponse by remember { mutableStateOf("") }
        var blockDialog by remember { mutableStateOf(false) }
        var unblockDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val filteredMessages =
            messageListFromMatchingChat.filter { message -> message.visible.contains(sharedViewModel.auth.currentUser?.uid.toString()) }
                .toMutableList()
        val lazyListState = rememberLazyListState()
        val context = LocalContext.current
        var currentIndex = 0
        Scaffold(containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                ChatViewTopBar(navController = navController,
                    chatPartner = friendData,
                    sharedViewModel = sharedViewModel,
                    onClick = {
                        when (it) {
                            "return" -> {
                                navController.navigateUp()
                            }

                            "profile" -> {
                                sharedViewModel.imageList.value = createImageList(filteredMessages)
                                navController.navigate(Screens.ProfileInfoScreen.route)
                            }

                            else -> {
                                blockDialog = true
                            }
                        }
                    },
                    onNavigate = {
                        coroutineScope.launch {
                            val indexes = filteredMessages.mapIndexedNotNull { index, message ->
                                if (message.text.contains(
                                        sharedViewModel.searchValue.value, ignoreCase = true
                                    )
                                ) {
                                    index
                                } else {
                                    null
                                }
                            }
                            if (indexes.isNotEmpty()) {
                                currentIndex = if (it) {
                                    (currentIndex + 1)
                                } else {
                                    (currentIndex - 1)
                                }
                                if (currentIndex >= indexes.size) {
                                    currentIndex = 0
                                } else if (currentIndex < 0) {
                                    currentIndex = indexes.size - 1
                                }

                                lazyListState.animateScrollToItem(indexes[currentIndex])
                            }
                            if (indexes.isEmpty()) {
                                Toast.makeText(context, "No results", Toast.LENGTH_SHORT).show()
                            } else if (indexes.size < 2) {
                                Toast.makeText(context, "No more results", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                    })
            },
            content = {
                ChatViewContentList(sharedViewModel = sharedViewModel,
                    paddingValues = it,
                    chatMateChat = chatMateChat,
                    messages = filteredMessages,
                    lazyListState = lazyListState,
                    chatRoomId = chatRoomId,
                    navController = navController,
                    chatMateResponse = { message ->
                        chatMateResponse = message
                    })
            },
            bottomBar = {
                InputField(
                    chatPartner = friendData,
                    onChatMateResponse = chatMateResponse,
                    sharedViewModel = sharedViewModel,
                    navController = navController,
                    chatMateChat = chatMateChat,
                ) {
                    unblockDialog = true
                }
            })
        if (blockDialog) {
            BlockUserDialog(
                friendData = friendData, userData = userData
            ) { value ->
                if (value == "blocked") {
                    updateBlockedUserList(
                        userData = userData,
                        friendData = friendData.personList,
                        isAlreadyBlocked = userData.blocked.contains(friendData.personList.id)
                    )
                }
                blockDialog = false
            }
        }
        if (unblockDialog) {
            UnblockToMessageDialog(
                chatPartner = friendData,
            ) { value ->
                if (value == "unblock") {
                    updateBlockedUserList(
                        userData = userData, friendData = friendData.personList, true
                    )
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
        var animatedText by remember { mutableStateOf("ChatMate is thinking") }
        var unblockDialog by remember { mutableStateOf(false) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                reverseLayout = true, modifier = Modifier.padding(
                    bottom = paddingValues.calculateBottomPadding(),
                    top = paddingValues.calculateTopPadding()
                ), state = lazyListState
            ) {
                item {
                    if (isBlockSeparatorNeeded(sharedViewModel)) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier
                                .background(
                                    if (isSystemInDarkTheme()) Color(0xFF141419) else Color(
                                        0xFFF5F5F5
                                    ), RoundedCornerShape(30)
                                )
                                .clickable { unblockDialog = true }
                                .align(Alignment.CenterHorizontally)) {
                                Text(
                                    text = "You have blocked this user",
                                    maxLines = 1,
                                    modifier = Modifier.padding(6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                items(
                    key = { it.id }, items = messages
                ) { message ->
                    val messageIndex = messages.indexOf(message)
                    val previousMessageIndex = messages.getOrNull(messageIndex + 1)
                    val nextMessageIndex = messages.getOrNull(messageIndex - 1)
                    MessageItem(sharedViewModel = sharedViewModel,
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
                        ),
                        chatRoomId = chatRoomId,
                        onClick = { image ->
                            sharedViewModel.imageList.value = createImageList(messages)
                            sharedViewModel.imagePosition.intValue =
                                sharedViewModel.imageList.value.find { it.images[0] == image }
                                    ?.let { sharedViewModel.imageList.value.indexOf(it) } ?: 0
                            navController.navigate(Screens.ImageViewScreen.route)
                        },
                        chatMateResponse = { chatMateResponse.invoke(it) })
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
        ChatViewMessageComponent(sharedViewModel = sharedViewModel,
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
                    100, VibrationEffect.DEFAULT_AMPLITUDE
                )
                vibrator.defaultVibrator.vibrate(effect)
                menuDialog = true
            })
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
                        copyToClipboard(context = context, label = "Message", text = message.text)
                    }

                    "generate" -> {
                        sharedViewModel.sendDataToServer(message.text) {
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

    private fun createImageList(messages: List<InternalMessageInstance>): List<InternalMessageInstance> {
        val imageList = arrayListOf<InternalMessageInstance>()
        messages.forEach {
            if (it.images.isNotEmpty()) {
                it.images.forEach { image ->
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
        return imageList
    }
}