package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.features.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.features.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatsViewBottomSheetContent
import at.htlhl.chatnet.ui.features.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ChatMateView {


    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun ChatMateScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val lazyListState = rememberLazyListState()
        val modelSheetState = remember { mutableStateOf(false) }
        var showUserIconPrompt by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val messageChatRoomDataState = sharedViewModel.completeChatMateList.collectAsState()
        val messageChatRoomData: List<InternalChatInstance> = messageChatRoomDataState.value
        val friendState = sharedViewModel.friend.collectAsState()
        val friend: InternalChatInstance = friendState.value
        val userState = sharedViewModel.user.collectAsState()
        val user: FirebaseUser = userState.value
        var showClearChatPrompt by remember { mutableStateOf(false) }
        val completeMessageChatRoomData =
            if (sharedViewModel.searchValue.value != "") messageChatRoomData.filter {
                it.personList.username["mixedcase"]?.contains(
                    sharedViewModel.searchValue.value,
                    ignoreCase = true
                ) ?: false
                        ||
                        it.lastMessage.text.contains(
                            sharedViewModel.searchValue.value,
                            ignoreCase = true
                        )
            } else messageChatRoomData
        val bottomSheetItems = listOf(
            BottomSheetItem(
                title = if (friend.markedAsUnread || friend.read > 0) "Mark as Read" else "Mark as Unread",
                icon = if (friend.markedAsUnread || friend.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com,
                tag = "unread"
            ),
            BottomSheetItem(
                title = "Clear Chat", icon = R.drawable.comment_delete_svgrepo_com, tag = "clear"
            ),
            BottomSheetItem(
                title = if (friend.pinChat) "Unpin Chat" else "Pin Chat",
                icon = if (friend.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                tag = "pin"
            ),
            BottomSheetItem(
                title = "Delete Chat",
                icon = R.drawable.garbage_bin_recycle_bin_svgrepo_com,
                tag = "delete"
            ),
        )
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(
                    tab = CurrentTab.CHATMATE,
                    sharedViewModel = sharedViewModel,
                    availableUsers = listOf(FirebaseUser()),
                ) {
                    sharedViewModel.createChatMateChat(
                        onError = {
                            Toast.makeText(
                                context,
                                "Only one empty chat is allowed at a time.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            },
            content = {
                LazyColumn(
                    Modifier
                        .fillMaxSize(),
                    state = lazyListState
                ) {
                    items(completeMessageChatRoomData) { message ->
                        ChatsViewChatItem(
                            chatFriend = message,
                            chatUser = user,
                            displayOnlineState = false,
                            sharedViewModel = sharedViewModel,
                        ) { context,selectedChat ->
                            sharedViewModel.updateFriend(selectedChat){
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
                            }
                        }
                    }
                }
            },
        )
        if (showUserIconPrompt) {
            ShowBigUserImageDialog(
                sharedViewModel = sharedViewModel,
                userData = friend,
                onDismiss = { action ->
                    when (action) {
                        "message" -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        "delete" -> {
                            sharedViewModel.deleteChatRoom()

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
                }
            )
        }
        if (showClearChatPrompt) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    sharedViewModel.deleteMessagesForUser(friend.chatRoomID)
                }
                showClearChatPrompt = false
            })
        }
        if (modelSheetState.value) {
            ModalBottomSheet(
                containerColor = MaterialTheme.colorScheme.background,
                windowInsets = WindowInsets(0, 0, 0, 0),
                onDismissRequest = { modelSheetState.value = false },
                dragHandle = null,
                content = {
                    ChatsViewBottomSheetContent(
                        bottomSheetItems,
                        onItemClicked = { item ->
                            modelSheetState.value = false
                            when (item.tag) {
                                "unread" -> {
                                    if (sharedViewModel.friend.value.read > 0) {
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

                                "delete" -> {
                                    sharedViewModel.deleteChatRoom()
                                }

                                "pin" -> {
                                    if (sharedViewModel.friend.value.pinChat) {
                                        sharedViewModel.updatePinChatStatus(true)
                                    } else {
                                        sharedViewModel.updatePinChatStatus(false)
                                    }
                                }
                            }
                        },
                        friend = friend,
                    )
                }
            )
        }
    }
}