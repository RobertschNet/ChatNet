package at.htlhl.chatnet.ui.features.chat.screens

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.chat.components.ChatViewContentComponent
import at.htlhl.chatnet.ui.features.chat.viewmodels.ChatViewModel
import at.htlhl.chatnet.ui.features.dialogs.ChangeBlockStateDialog
import at.htlhl.chatnet.ui.features.dialogs.UnblockToMessageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatViewTopBar
import at.htlhl.chatnet.ui.features.mixed.ChatInputFieldComponent
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

class ChatView {
    @Composable
    fun ChatViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val chatViewModel = viewModel<ChatViewModel>()
        val context = LocalContext.current
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )

        val userDataState by sharedViewModel.userData.collectAsState(initial = FirebaseUser())
        val friendDataState by sharedViewModel.friend.collectAsState(initial = InternalChatInstance())
        val chatDataState by sharedViewModel.chatData.collectAsState(initial = emptyList())

        val userData: FirebaseUser = userDataState
        val friendData: InternalChatInstance = friendDataState
        val chatData: List<FirebaseChat> = chatDataState

        var chatMateResponseText by remember { mutableStateOf("") }
        var changeBlockStateDialog by remember { mutableStateOf(false) }
        var unblockDialog by remember { mutableStateOf(false) }
        var searchIndex = 0
        val chatMateResponseState = sharedViewModel.chatMateResponseState.value
        val searchedValue = sharedViewModel.searchValue.value
        val imageList = sharedViewModel.imageList.value

        val coroutineScope = rememberCoroutineScope()
        val lazyColumnState = rememberLazyListState()

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
        val filteredMessages =
            messageListFromMatchingChat.filter { message -> message.visible.contains(userData.id) }
                .toMutableList()

        LaunchedEffect(matchingChat?.messages?.size) {
            sharedViewModel.markMessagesAsRead()
        }
        Scaffold(containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                ChatViewTopBar(userData = userData,
                    friendData = friendData,
                    chatMateResponseState = chatMateResponseState,
                    onTopBarUserElementClicked = { userElementClicked ->
                        if (userElementClicked) {
                            sharedViewModel.updateImageList(newImageList = chatViewModel.createImageList(
                                messages = filteredMessages
                            ), onComplete = {
                                navController.navigate(Screens.UserSheetScreen.route)
                            })
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onNavigateBetweenSearchedValues = {
                        coroutineScope.launch {
                            val indexes = filteredMessages.mapIndexedNotNull { index, message ->
                                if (message.text.contains(
                                        searchedValue, ignoreCase = true
                                    )
                                ) {
                                    index
                                } else {
                                    null
                                }
                            }
                            if (indexes.isNotEmpty()) {
                                searchIndex = if (it) {
                                    (searchIndex + 1)
                                } else {
                                    (searchIndex - 1)
                                }
                                if (searchIndex >= indexes.size) {
                                    searchIndex = 0
                                } else if (searchIndex < 0) {
                                    searchIndex = indexes.size - 1
                                }

                                lazyColumnState.animateScrollToItem(indexes[searchIndex])
                            }
                            if (indexes.isEmpty()) {
                                Toast.makeText(context, "No results", Toast.LENGTH_SHORT).show()
                            } else if (indexes.size < 2) {
                                Toast.makeText(context, "No more results", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    onUpdateSearchValue = { updatedValue ->
                        sharedViewModel.updateSearchValue(newSearchValue = updatedValue)
                    })
            },
            content = { paddingValues ->
                ChatViewContentComponent(chatViewModel = chatViewModel,
                    sharedViewModel = sharedViewModel,
                    paddingValues = paddingValues,
                    context = context,
                    isChatMateChat = chatMateChat,
                    userData = userData,
                    imageList = imageList,
                    friendData = friendData,
                    messages = filteredMessages,
                    lazyColumnState = lazyColumnState,
                    chatRoomId = chatRoomId,
                    navController = navController,
                    chatMateResponseState = chatMateResponseState,
                    onChatMateResponseReceived = { responseText ->
                        chatMateResponseText = responseText
                    })
            },
            bottomBar = {
                ChatInputFieldComponent(
                    userData = userData,
                    friendData = friendData,
                    chatMateResponseText = chatMateResponseText,
                    chatMateResponseState = chatMateResponseState,
                    coroutineScope = coroutineScope,
                    context = context,
                    navController = navController,
                    isChatMateChat = chatMateChat,
                    onUpdateChatMateResponseState = { chatMateResponseState ->
                        sharedViewModel.updateChatMateResponseState(newChatMateResponseState = chatMateResponseState)
                    },
                    onSentWhileBlocked = {
                        unblockDialog = true
                    }
                )
            })
        if (changeBlockStateDialog) {
            ChangeBlockStateDialog(friendData = friendData,
                userData = userData,
                onChangeBlockStateClicked = { stateChanged ->
                    if (stateChanged) {
                        updateBlockedUserList(
                            userData = userData,
                            friendData = friendData.personList,
                            isAlreadyBlocked = userData.blocked.contains(friendData.personList.id)
                        )
                    }
                    changeBlockStateDialog = false
                })
        }
        if (unblockDialog) {
            UnblockToMessageDialog(
                friendData = friendData,
            ) { unblockPressed ->
                if (unblockPressed) {
                    updateBlockedUserList(
                        userData = userData,
                        friendData = friendData.personList,
                        isAlreadyBlocked = true
                    )
                }
                unblockDialog = false
            }
        }
    }
}