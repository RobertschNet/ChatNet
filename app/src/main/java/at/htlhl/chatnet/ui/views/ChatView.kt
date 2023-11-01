package at.htlhl.chatnet.ui.views

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.foundation.background
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
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChats
import at.htlhl.chatnet.data.FirebaseMessages
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.ChatViewMessageComponent
import at.htlhl.chatnet.ui.components.DeleteMessageDialog
import at.htlhl.chatnet.ui.components.InputField
import at.htlhl.chatnet.ui.components.MessageTopBar
import at.htlhl.chatnet.ui.components.OptionsDialog
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ChatView : ViewModel() {
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChats> = chatDataState.value
        val filteredChats = chatData.find { chat ->
            chat.members.contains(sharedViewModel.friend.value.personList.id) && chat.members.contains(
                sharedViewModel.auth.currentUser?.uid.toString()
            )
        }
        val chatRoomId = filteredChats?.chatRoomID ?: ""
        val messageList: List<FirebaseMessages> = filteredChats?.let { chat ->
            chat.messages.map { message ->
                FirebaseMessages(
                    sender = message.sender,
                    type = message.type,
                    read = message.read,
                    content = message.content,
                    timestamp = message.timestamp,
                    visible = message.visible,
                )
            }
        } ?: emptyList()
        val onMessageSent: (FirebaseMessages) -> Unit = { message ->
            if (filteredChats?.tab == "chatmate") {
                sharedViewModel.sendDataToServer(message.content) { response ->
                    runBlocking {
                        sharedViewModel.saveMessages(
                            documentId = chatRoomId,
                            message = FirebaseMessages(
                                sender = "chatmate",
                                content = response,
                                timestamp = Timestamp.now(),
                                read = false,
                                type = "text",
                                visible = listOf(
                                    sharedViewModel.auth.currentUser?.uid.toString(),
                                )
                            )
                        )
                    }
                }
            }
            runBlocking {
                sharedViewModel.saveMessages(chatRoomId, message)
            }
        }
        sharedViewModel.markMessagesAsRead(sharedViewModel.friend.value)
        sharedViewModel.updateMarkAsReadStatus(true)
        ChatViewContentStructure(
            sharedViewModel = sharedViewModel,
            messages = messageList,
            onMessageSent = onMessageSent,
            navController = navController,
            chatRoomId = chatRoomId,
        )
    }

    @Composable
    fun ChatViewContentStructure(
        navController: NavController,
        messages: List<FirebaseMessages>,
        onMessageSent: (FirebaseMessages) -> Unit,
        sharedViewModel: SharedViewModel,
        chatRoomId: String,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val filteredMessages = messages.filter { message ->
            message.visible.contains(sharedViewModel.auth.currentUser?.uid.toString())
        }.toMutableList()
        val lazyListState =
            rememberLazyListState(initialFirstVisibleItemIndex = sharedViewModel.getMessageLengthForChat()!!)
        Scaffold(
            modifier = Modifier
                .imePadding()
                .onSizeChanged { coroutineScope.launch { lazyListState.scrollToItem(messages.size) } },
            topBar = {
                MessageTopBar(sharedViewModel.friend.value) {
                    navController.navigate(Screens.ChatsViewScreen.route)
                }
            },
            content = {
                it.calculateBottomPadding()
                ChatViewContentList(
                    sharedViewModel = sharedViewModel,
                    messages = filteredMessages,
                    lazyListState = lazyListState,
                    chatRoomId = chatRoomId
                )
            }, bottomBar = {
                InputField(
                    sharedViewModel = sharedViewModel,
                    navController = navController,
                    onMessageSent = { messageText, image ->
                        sharedViewModel.chatData.value + (FirebaseMessages(
                            sender = sharedViewModel.auth.currentUser?.uid.toString(),
                            content = messageText,
                            timestamp = Timestamp.now(),
                            read = false,
                            type = image,
                            visible = listOf(
                                sharedViewModel.auth.currentUser?.uid.toString(),
                                sharedViewModel.friend.value.personList.id
                            )
                        ))
                        val message = FirebaseMessages(
                            sender = sharedViewModel.auth.currentUser?.uid.toString(),
                            content = messageText,
                            timestamp = Timestamp.now(),
                            read = false,
                            type = image,
                            visible = listOf(
                                sharedViewModel.auth.currentUser?.uid.toString(),
                                sharedViewModel.friend.value.personList.id
                            )
                        )
                        onMessageSent(message)
                    },
                )

            })
    }

    @Composable
    fun ChatViewContentList(
        sharedViewModel: SharedViewModel,
        messages: List<FirebaseMessages>,
        lazyListState: LazyListState,
        chatRoomId: String
    ) {
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
            LazyColumn(Modifier.padding(bottom = 70.dp), state = lazyListState) {
                items(messages) { message ->
                    val messageIndex = messages.indexOf(message)
                    val previousMessageIndex =
                        if (messageIndex > 0) messages.getOrNull(messageIndex - 1) else null

                    MessageItem(
                        sharedViewModel = sharedViewModel,
                        previousMessage = previousMessageIndex,
                        message = FirebaseMessages(
                            sender = message.sender,
                            type = message.type,
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
        message: FirebaseMessages,
        previousMessage: FirebaseMessages?,
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
            previousMessage = previousMessage,
            message = message,
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
                    sharedViewModel.deleteMessage(chatRoomId, message.timestamp)
                } else if (value == "change") {
                    sharedViewModel.changeMessageVisibility(chatRoomId, message.timestamp)
                }
                deleteDialog = false
            }
        }
        if (menuDialog) {
            OptionsDialog(anchorPosition.value) { value ->
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