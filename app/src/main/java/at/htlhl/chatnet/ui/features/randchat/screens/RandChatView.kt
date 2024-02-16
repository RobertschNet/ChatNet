package at.htlhl.chatnet.ui.features.randchat.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.ui.features.dialogs.DeleteMessageDialog
import at.htlhl.chatnet.ui.features.dialogs.OptionsDialog
import at.htlhl.chatnet.ui.features.dialogs.UnblockToMessageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatViewMessageComponent
import at.htlhl.chatnet.ui.features.mixed.ChatViewTopBar
import at.htlhl.chatnet.ui.features.mixed.InputField
import at.htlhl.chatnet.util.copyToClipboard
import at.htlhl.chatnet.util.firebase.changeMessageVisibility
import at.htlhl.chatnet.util.firebase.deleteMessage
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RandChatView {
    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RandChatScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val coroutineScope = rememberCoroutineScope()
        val friendDataState =
            sharedViewModel.friend.collectAsState(initial = InternalChatInstance())
        val friendData: InternalChatInstance = friendDataState.value
        val userDataState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userDataState.value
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
                                coroutineScope.launch {
                                    pageState.scrollToPage(1)
                                }
                            }
                        }
                        ChatViewScreen(
                            userData = userData,
                            friendData = friendData,
                            navController = navController,
                            sharedViewModel = sharedViewModel
                        )
                    } else {
                        LoadingScreen(navController)
                    }
                }

                1 -> {
                    LoadingScreen(navController)
                }
            }
        }
    }

    @Composable
    fun ChatViewScreen(
        userData: FirebaseUser,
        navController: NavController,
        sharedViewModel: SharedViewModel,
        friendData: InternalChatInstance
    ) {
        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChat> = chatDataState.value
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
        ChatViewContentStructure(
            userData = userData,
            chatPartner = friendData,
            sharedViewModel = sharedViewModel,
            messagesForChat = messageListFromMatchingChat,
            navController = navController,
            chatRoomId = chatRoomId,
            chatMateChat = chatMateChat
        )
    }

    @Composable
    fun ChatViewContentStructure(
        userData: FirebaseUser,
        chatPartner: InternalChatInstance,
        navController: NavController,
        messagesForChat: List<InternalMessageInstance>,
        sharedViewModel: SharedViewModel,
        chatRoomId: String,
        chatMateChat: Boolean
    ) {
        var chatMateResponse by remember { mutableStateOf("") }
        var unblockDialog by remember { mutableStateOf(false) }
        val filteredMessages = messagesForChat.filter { message ->
            message.visible.contains(sharedViewModel.auth.currentUser?.uid.toString())
        }.toMutableList()
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        var currentIndex = 0
        Scaffold(modifier = Modifier.imePadding(), topBar = {
            ChatViewTopBar(navController = navController,
                chatPartner = chatPartner,
                sharedViewModel = sharedViewModel,
                onClick = {
                    navController.navigateUp()
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
                            Toast.makeText(context, "No more results", Toast.LENGTH_SHORT).show()
                        }

                    }
                })
        }, content = {
            it.calculateBottomPadding()
            ChatViewContentList(sharedViewModel = sharedViewModel,
                chatMateChat = chatMateChat,
                messages = filteredMessages,
                userData = userData,
                lazyListState = lazyListState,
                chatRoomId = chatRoomId,
                onChatMateResponseState = { message ->
                    chatMateResponse = message
                }

            )
        }, bottomBar = {
            InputField(
                chatPartner = chatPartner,
                onChatMateResponse = chatMateResponse,
                sharedViewModel = sharedViewModel,
                navController = navController,
                chatMateChat = chatMateChat
            ) {
                unblockDialog = true
            }
        }

        )
        if (unblockDialog) {
            UnblockToMessageDialog(
                chatPartner = chatPartner,
            ) { value ->
                if (value == "unblock") {
                    updateBlockedUserList(
                        userData = userData, friendData = chatPartner.personList, true
                    )
                }
                unblockDialog = false
            }
        }
    }

    @Composable
    fun ChatViewContentList(
        sharedViewModel: SharedViewModel,
        userData: FirebaseUser,
        chatMateChat: Boolean,
        messages: List<InternalMessageInstance>,
        lazyListState: LazyListState,
        chatRoomId: String,
        onChatMateResponseState: (String) -> Unit
    ) {
        var animatedText by remember { mutableStateOf("ChatMate is thinking") }
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
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                modifier = Modifier.padding(bottom = 70.dp),
                reverseLayout = true,
                state = lazyListState
            ) {
                items(messages) { message ->
                    val messageIndex = messages.indexOf(message)
                    val previousMessageIndex =
                        messages.getOrNull(messageIndex + 1) // Reverse the calculation
                    val nextMessageIndex =
                        messages.getOrNull(messageIndex - 1)   // Reverse the calculation
                    MessageItem(
                        sharedViewModel = sharedViewModel,
                        chatMateChat = chatMateChat,
                        userData= userData,
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
                        chatRoomId = chatRoomId
                    ) {
                        onChatMateResponseState.invoke(it)
                    }
                }
            }

        }
    }


    @Composable
    fun MessageItem(
        sharedViewModel: SharedViewModel,
        userData: FirebaseUser,
        chatMateChat: Boolean,
        message: InternalMessageInstance,
        previousMessage: InternalMessageInstance?,
        nextMessage: InternalMessageInstance?,
        chatRoomId: String,
        onChatMateResponseState: (String) -> Unit
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
            onClick = {},
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
                    deleteMessage(chatRoomID = chatRoomId, messageID = message.id)
                } else if (value == "change") {
                    changeMessageVisibility(
                        userID = userData.id,
                        chatRoomID = chatRoomId, messageId = message.id
                    )
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
                            onChatMateResponseState.invoke(it)
                        }
                    }
                }
                menuDialog = false
            }
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun LoadingScreen(navController: NavController) {
        Scaffold(backgroundColor = MaterialTheme.colorScheme.background, topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { navController.navigateUp() }, modifier = Modifier.size(45.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = R.drawable.back_svgrepo_com_1_,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }, content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(50.dp))
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Searching for User...",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.SansSerif
                )
            }
        })
    }
}