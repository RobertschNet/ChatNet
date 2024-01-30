package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.components.chats.EmptyChatContent
import at.htlhl.chatnet.ui.components.dialogs.BlockUserDialog
import at.htlhl.chatnet.ui.components.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.components.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.components.mixed.ChatsViewBottomSheetContent
import at.htlhl.chatnet.ui.components.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.components.mixed.TabsTopBar
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ChatsView {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatsScreen(
        navController: NavController, sharedViewModel: SharedViewModel
    ) {
        val context = LocalContext.current
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showUserIconPrompt by remember { mutableStateOf(false) }
        var showClearChatPrompt by remember { mutableStateOf(false) }
        var showBlockUserDialog by remember { mutableStateOf(false) }
        val modelSheetState = remember { mutableStateOf(false) }
        val userDataInstanceState = sharedViewModel.completeChatList.collectAsState(
            initial = arrayListOf(InternalChatInstance())
        )
        val userDataInstance: List<InternalChatInstance> = userDataInstanceState.value
        val chatDataState =
            sharedViewModel.chatData.collectAsState(initial = arrayListOf(FirebaseChat()))
        val chatData: List<FirebaseChat> = chatDataState.value
        val friendListDataState =
            sharedViewModel.friendListData.collectAsState(initial = arrayListOf(FirebaseUser()))
        val friendListData: List<FirebaseUser> = friendListDataState.value
        val friendDataState =
            sharedViewModel.friend.collectAsState(initial = InternalChatInstance())
        val friendData: InternalChatInstance = friendDataState.value
        val userDataState = sharedViewModel.user.collectAsState(initial = FirebaseUser())
        val userData: FirebaseUser = userDataState.value
        val availableUsers = friendListData.filter { friend -> friend.statusFriend == "pending" }
        val completePersonList =
            if (sharedViewModel.searchValue.value != "") userDataInstance.filter {
                it.personList.username["mixedcase"]?.contains(
                    sharedViewModel.searchValue.value,
                    ignoreCase = true
                ) ?: false
            } else userDataInstance
        LaunchedEffect(chatData) {
            for (chat in chatData) {
                for (message in chat.messages) {
                    if (message.images.isNotEmpty()) {
                        for (image in message.images) {
                            val request = ImageRequest.Builder(context)
                                .data(image)
                                .build()
                            context.imageLoader.enqueue(request)
                        }
                    }
                }
            }
        }
        LaunchedEffect(sharedViewModel.localChatUserList.value) {
            for (user in sharedViewModel.localChatUserList.value) {
                val request = ImageRequest.Builder(context)
                    .data(user.image)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
        val bottomSheetItems = listOf(
            BottomSheetItem(
                title = if (friendData.markedAsUnread || friendData.read > 0) "Mark as Read" else "Mark as Unread",
                icon = if (friendData.markedAsUnread || friendData.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com,
                tag = "unread"
            ),
            BottomSheetItem(
                title = "Clear Chat", icon = R.drawable.comment_delete_svgrepo_com, tag = "clear"
            ),
            BottomSheetItem(
                title = if (userData.muted.contains(friendData.personList.id)) "Unmute User" else "Mute User",
                icon = if (userData.muted.contains(friendData.personList.id)) R.drawable.speaker_none_svgrepo_com else R.drawable.speaker_svgrepo_com,
                tag = "mute"
            ),
            BottomSheetItem(
                title = if (friendData.pinChat) "Unpin Chat" else "Pin Chat",
                icon = if (friendData.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                tag = "pin"
            ),
        )
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TabsTopBar(
                    tab = "Chats",
                    availableUsers = availableUsers,
                    sharedViewModel = sharedViewModel,
                ) {
                    navController.navigate(Screens.FindUserScreen.route)
                }
            },
            content = {
                if (userDataInstance.isEmpty()) {
                    EmptyChatContent(onClicked = {
                        navController.navigate(Screens.FindUserScreen.route)
                    })
                }
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary),
                    state = lazyListState
                ) {
                    items(completePersonList) { message ->
                        ChatsViewChatItem(
                            chatFriend = message,
                            chatUser = userData,
                            displayOnlineState = true,
                            sharedViewModel = sharedViewModel,
                        ) { context ->
                            when (context) {
                                "image" -> {
                                    showUserIconPrompt = true
                                }

                                "message" -> {
                                    modelSheetState.value = true
                                }

                                "navigate" -> {
                                    navController.navigate(Screens.ChatViewScreen.route)
                                }
                            }
                            sharedViewModel.updateFriend(message)
                        }
                    }
                }
            }
        )
        if (showUserIconPrompt) {
            ShowBigUserImageDialog(
                sharedViewModel = sharedViewModel,
                userData = friendData,
                onDismiss = { action ->
                    when (action) {
                        "message" -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        "block" -> {
                            showBlockUserDialog = true

                        }

                        "image" -> {
                            coroutineScope.launch {
                                SaveImageTask(WeakReference(context)).saveImage(sharedViewModel.friend.value.personList.image)
                            }
                        }

                        "info" -> {
                            navController.navigate(Screens.ProfileInfoScreen.route)
                        }
                    }
                    showUserIconPrompt = false
                })
        }
        if (showClearChatPrompt) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    sharedViewModel.deleteMessagesForUser(friendData.chatRoomID)
                }
                showClearChatPrompt = false
            })
        }
        if (showBlockUserDialog) {
            BlockUserDialog(chatUser = userData, chatPartner = friendData) {
                if (it == "blocked") {
                    sharedViewModel.updateBlockedUserList(
                        sharedViewModel.user.value.blocked.contains(
                            sharedViewModel.friend.value.personList.id
                        )
                    )
                }
                showBlockUserDialog = false
            }
        }
        if (modelSheetState.value) {
            ModalBottomSheet(
                windowInsets = WindowInsets(0, 0, 0, 0),
                onDismissRequest = {
                    modelSheetState.value = false
                }, dragHandle = null, content = {
                    ChatsViewBottomSheetContent(
                        bottomSheetItems,
                        onItemClicked = { item ->
                            modelSheetState.value = false
                            when (item.tag) {
                                "unread" -> {
                                    if (friendData.read > 0) {
                                        sharedViewModel.markMessagesAsRead(sharedViewModel.friend.value)
                                    } else if (sharedViewModel.friend.value.markedAsUnread && sharedViewModel.friend.value.read == 0) {
                                        sharedViewModel.updateMarkAsReadStatus(true)
                                    } else {
                                        sharedViewModel.updateMarkAsReadStatus(false)
                                    }
                                }

                                "clear" -> {
                                    showClearChatPrompt = true
                                }

                                "mute" -> {
                                    if (userData.muted.contains(friendData.personList.id)) {
                                        sharedViewModel.updateMuteFriendStatus(true)
                                    } else {
                                        sharedViewModel.updateMuteFriendStatus(false)
                                    }
                                }

                                "pin" -> {
                                    if (friendData.pinChat) {
                                        sharedViewModel.updatePinChatStatus(true)
                                    } else {
                                        sharedViewModel.updatePinChatStatus(false)
                                    }
                                }
                            }
                        },
                        friend = friendData,
                    )
                }
            )
        }
    }
}