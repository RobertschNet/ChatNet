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
import at.htlhl.chatnet.data.BigUserImageDismissState.*
import at.htlhl.chatnet.data.BottomSheetTagState
import at.htlhl.chatnet.data.ChatsChatItemClickState
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.features.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.features.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.features.mixed.TabsBottomSheetContent
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.util.firebase.deleteAllChatMessages
import at.htlhl.chatnet.util.firebase.deleteChatRoom
import at.htlhl.chatnet.util.firebase.markMessagesAsRead
import at.htlhl.chatnet.util.firebase.updateMarkAsUnreadStatus
import at.htlhl.chatnet.util.firebase.updatePinChatStatus
import at.htlhl.chatnet.util.generateBottomSheetItems
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
        var modelSheetState by remember { mutableStateOf(false) }
        var showUserIconPrompt by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val dropInState by sharedViewModel.dropInState
        val searchedValue by sharedViewModel.searchValue
        val coroutineScope = rememberCoroutineScope()
        val messageChatRoomDataState = sharedViewModel.completeChatMateList.collectAsState()
        val messageChatRoomData: List<InternalChatInstance> = messageChatRoomDataState.value
        val friendState = sharedViewModel.friend.collectAsState()
        val friendData: InternalChatInstance = friendState.value
        val userState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userState.value
        var showClearChatDialog by remember { mutableStateOf(false) }
        val completeMessageChatRoomData =
            if (sharedViewModel.searchValue.value != "") messageChatRoomData.filter {
                it.personList.username["mixedcase"]?.contains(
                    sharedViewModel.searchValue.value, ignoreCase = true
                ) ?: false || it.lastMessage.text.contains(
                    sharedViewModel.searchValue.value, ignoreCase = true
                )
            } else messageChatRoomData
        val bottomSheetItems = generateBottomSheetItems(
            isChatMate = true, friendData = friendData, userData = userData
        )
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(tab = CurrentTab.CHATMATE, dropInState = dropInState, onActionClicked = {
                    sharedViewModel.createChatMateChat(onError = {
                        Toast.makeText(
                            context, "Only one empty chat is allowed at a time.", Toast.LENGTH_SHORT
                        ).show()
                    })
                }, onUpdateSearchValue = {
                    sharedViewModel.searchValue.value = it
                })
            },
            content = {
                LazyColumn(
                    Modifier.fillMaxSize(), state = lazyListState
                ) {
                    items(completeMessageChatRoomData) { message ->
                        ChatsViewChatItem(
                            friendElement = message,
                            userData = userData,
                            displayOnlineState = false,
                            searchedValue = searchedValue
                        ) { context ->
                            sharedViewModel.updateFriend(message) {
                                when (context) {
                                    ChatsChatItemClickState.IMAGE -> {
                                        showUserIconPrompt = true
                                    }

                                    ChatsChatItemClickState.CONTEXT_MENU -> {
                                        modelSheetState = true
                                    }

                                    ChatsChatItemClickState.MESSAGE -> {
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
            ShowBigUserImageDialog(userData = userData,
                friendData = friendData,
                onDismiss = { action ->
                    when (action) {
                        INFO -> {
                            navController.navigate(Screens.ProfileInfoScreen.route)

                        }

                        MESSAGE -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        IMAGE -> {
                            coroutineScope.launch {
                                SaveImageTask(WeakReference(context)).saveImage(sharedViewModel.friend.value.personList.image)
                            }
                        }

                        DELETE -> {
                            deleteChatRoom(friendData = friendData)
                        }

                        else -> {
                        }
                    }
                    showUserIconPrompt = false
                })
        }
        if (showClearChatDialog) {
            ClearChatDialog(onClearChatClicked = { clearClicked ->
                if (clearClicked) {
                    deleteAllChatMessages(
                        userData = userData, friendData = friendData
                    )
                }
                showClearChatDialog = false
            })
        }
        if (modelSheetState) {
            ModalBottomSheet(containerColor = MaterialTheme.colorScheme.background,
                windowInsets = WindowInsets(0, 0, 0, 0),
                onDismissRequest = { modelSheetState = false },
                dragHandle = null,
                content = {
                    TabsBottomSheetContent(
                        friendData = friendData,
                        bottomSheetItems = bottomSheetItems,
                        onItemClicked = { item ->
                            modelSheetState = false
                            when (item) {
                                BottomSheetTagState.UNREAD -> {
                                    if (friendData.read > 0) {
                                        markMessagesAsRead(
                                            userData = userData,
                                            friendData = friendData
                                        )
                                    } else if (friendData.markedAsUnread && friendData.read == 0) {
                                        updateMarkAsUnreadStatus(
                                            userData = userData,
                                            friendData = friendData,
                                            isAlreadyUnread = true
                                        )

                                    } else {
                                        updateMarkAsUnreadStatus(
                                            userData = userData,
                                            friendData = friendData,
                                            isAlreadyUnread = false
                                        )

                                    }
                                }

                                BottomSheetTagState.CLEAR -> {
                                    showClearChatDialog = true
                                }

                                BottomSheetTagState.DELETE -> {
                                    deleteChatRoom(friendData = friendData)
                                }

                                BottomSheetTagState.PIN -> {
                                    if (friendData.pinChat) {
                                        updatePinChatStatus(
                                            userData = userData,
                                            friend = friendData,
                                            isAlreadyPinned = true
                                        )
                                    } else {
                                        updatePinChatStatus(
                                            userData = userData,
                                            friend = friendData,
                                            isAlreadyPinned = false
                                        )
                                    }
                                }

                                else -> {
                                }
                            }
                        },
                    )
                })
        }
    }
}