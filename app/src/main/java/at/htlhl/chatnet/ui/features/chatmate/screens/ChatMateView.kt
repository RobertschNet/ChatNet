package at.htlhl.chatnet.ui.features.chatmate.screens

import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.BigUserImageDismissState.DELETE
import at.htlhl.chatnet.data.BigUserImageDismissState.IMAGE
import at.htlhl.chatnet.data.BigUserImageDismissState.INFO
import at.htlhl.chatnet.data.BigUserImageDismissState.MESSAGE
import at.htlhl.chatnet.data.BottomSheetTagState
import at.htlhl.chatnet.data.ChatsChatItemClickState
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.features.chatmate.viewmodels.ChatMateViewModel
import at.htlhl.chatnet.ui.features.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteChatDialog
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
    @Composable
    fun ChatMateScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val chatMateViewModel = viewModel<ChatMateViewModel>()
        val context = LocalContext.current

        val dropInState by sharedViewModel.dropInState
        val searchedValue by sharedViewModel.searchValue

        val lazyColumnState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        var modelSheetState by remember { mutableStateOf(false) }
        var showBigUserImageDialog by remember { mutableStateOf(false) }
        var showClearChatDialog by remember { mutableStateOf(false) }
        var deleteChatDialog by remember { mutableStateOf(false) }

        val completeChatMateListState by sharedViewModel.completeChatMateList.collectAsState()
        val friendDataState by sharedViewModel.friend.collectAsState()
        val userDataState by sharedViewModel.user.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()

        val completeChatMateChatsList: List<InternalChatInstance> = completeChatMateListState
        val friendData: InternalChatInstance = friendDataState
        val userData: FirebaseUser = userDataState
        val chatData: List<FirebaseChat> = chatDataState

        val filteredChatMateChatsList = chatMateViewModel.filterChatMateChatsList(
            completeChatMateChatsList = completeChatMateChatsList, searchedValue = searchedValue
        )
        val bottomSheetItems = generateBottomSheetItems(
            isChatMate = true, friendData = friendData, userData = userData
        )

        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(tab = CurrentTab.CHATMATE, dropInState = dropInState, onActionClicked = {
                    chatMateViewModel.createChatMateChat(
                        userID = userData.id,
                        chatData = chatData,
                        onEmptyChatAlreadyExists = {
                            Toast.makeText(
                                context,
                                "Only one empty chat is allowed at a time.",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }, onUpdateSearchValue = { updatedSearchValue ->
                    sharedViewModel.updateSearchValue(newSearchValue = updatedSearchValue)
                })
            },
            content = { paddingValues ->
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    state = lazyColumnState
                ) {
                    items(filteredChatMateChatsList) { chatMateChat ->
                        ChatsViewChatItem(
                            friendElement = chatMateChat,
                            userData = userData,
                            displayOnlineState = false,
                            searchedValue = searchedValue
                        ) { context ->
                            sharedViewModel.updateFriend(newFriend = chatMateChat, onComplete = {
                                when (context) {
                                    ChatsChatItemClickState.IMAGE -> {
                                        showBigUserImageDialog = true
                                    }

                                    ChatsChatItemClickState.CONTEXT_MENU -> {
                                        modelSheetState = true
                                    }

                                    ChatsChatItemClickState.MESSAGE -> {
                                        navController.navigate(Screens.ChatViewScreen.route)
                                    }
                                }
                            })
                        }
                    }
                }
            },
        )
        if (showBigUserImageDialog) {
            ShowBigUserImageDialog(
                userData = userData,
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
                                SaveImageTask(WeakReference(context)).saveImage(friendData.personList.image)
                            }
                        }

                        DELETE -> {
                            deleteChatDialog = true
                        }

                        else -> {
                        }
                    }
                    showBigUserImageDialog = false
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
        if (deleteChatDialog) {
           DeleteChatDialog(onChatDeletedClicked = { chatDeleted ->
               if (chatDeleted) {
                   deleteChatRoom(friendData = friendData)
               }
               deleteChatDialog = false
              }
           )
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
                                            userData = userData, friendData = friendData
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
                                    deleteChatDialog = true
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