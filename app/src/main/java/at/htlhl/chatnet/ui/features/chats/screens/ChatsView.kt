package at.htlhl.chatnet.ui.features.chats.screens

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
import at.htlhl.chatnet.data.BigUserImageDismissState.BLOCK
import at.htlhl.chatnet.data.BigUserImageDismissState.IMAGE
import at.htlhl.chatnet.data.BigUserImageDismissState.INFO
import at.htlhl.chatnet.data.BigUserImageDismissState.MESSAGE
import at.htlhl.chatnet.data.BottomSheetTagState
import at.htlhl.chatnet.data.ChatsChatItemClickState
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.features.chats.components.EmptyFriendListContent
import at.htlhl.chatnet.ui.features.chats.viewmodels.ChatsViewModel
import at.htlhl.chatnet.ui.features.dialogs.BlockUserDialog
import at.htlhl.chatnet.ui.features.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.features.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.features.mixed.LoadingUserChatsElement
import at.htlhl.chatnet.ui.features.mixed.TabsBottomSheetContent
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.util.firebase.deleteAllChatMessages
import at.htlhl.chatnet.util.firebase.markMessagesAsRead
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.util.firebase.updateMarkAsUnreadStatus
import at.htlhl.chatnet.util.firebase.updateMuteFriendStatus
import at.htlhl.chatnet.util.firebase.updatePinChatStatus
import at.htlhl.chatnet.util.generateBottomSheetItems
import at.htlhl.chatnet.util.preLoadImages
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class ChatsView {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatsScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val chatsViewModel = viewModel<ChatsViewModel>()
        val context = LocalContext.current

        val dropInState by sharedViewModel.dropInState
        val searchedValue by sharedViewModel.searchValue
        val isDataLoaded by sharedViewModel.isDataLoaded

        val lazyColumnState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        var showBigUserImageDialog by remember { mutableStateOf(false) }
        var showClearChatDialog by remember { mutableStateOf(false) }
        var showBlockUserDialog by remember { mutableStateOf(false) }
        var modelSheetState by remember { mutableStateOf(false) }

        val completeChatsListState by sharedViewModel.completeChatList.collectAsState(
            initial = arrayListOf(
                InternalChatInstance()
            )
        )
        val chatDataState by sharedViewModel.chatData.collectAsState(
            initial = arrayListOf(
                FirebaseChat()
            )
        )
        val userDataState by sharedViewModel.user.collectAsState(initial = FirebaseUser())
        val friendListDataState by sharedViewModel.friendListData.collectAsState(
            initial = arrayListOf(
                FirebaseUser()
            )
        )
        val friendDataState by sharedViewModel.friend.collectAsState(initial = InternalChatInstance())

        val friendData: InternalChatInstance = friendDataState
        val userData: FirebaseUser = userDataState
        val friendListData: List<FirebaseUser> = friendListDataState
        val chatData: List<FirebaseChat> = chatDataState
        val completeChatsList: List<InternalChatInstance> = completeChatsListState
        val friendRequests =
            friendListData.filter { friend -> friend.statusFriend == PersonType.PENDING_PERSON }

        val filteredFriendsList = chatsViewModel.filterFriendsList(
            searchedValue = searchedValue, completeChatList = completeChatsList
        )

        LaunchedEffect(chatData) {
            for (chat in chatData) {
                for (message in chat.messages) {
                    for (image in message.images) {
                        preLoadImages(context = context, imageUrls = image)
                    }
                }
            }
        }

        val bottomSheetItems = generateBottomSheetItems(
            isChatMate = false, friendData = friendData, userData = userData
        )
        Scaffold(backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(tab = CurrentTab.CHATS,
                    friendRequests = friendRequests,
                    dropInState = dropInState,
                    onActionClicked = {
                        navController.navigate(Screens.FindUserScreen.route)
                    },
                    onUpdateSearchValue = { updatedSearchValue ->
                        sharedViewModel.updateSearchValue(newSearchValue = updatedSearchValue)
                    })
            },
            content = { scaffoldPaddingValues ->
                if (filteredFriendsList.isEmpty() && isDataLoaded) {
                    EmptyFriendListContent(onGetStartedClicked = {
                        navController.navigate(Screens.FindUserScreen.route)
                    })
                } else {
                    LazyColumn(
                        Modifier
                            .padding(top = scaffoldPaddingValues.calculateTopPadding())
                            .fillMaxSize(), state = lazyColumnState
                    ) {
                        items(filteredFriendsList) { friendElement ->
                            ChatsViewChatItem(
                                friendElement = friendElement,
                                userData = userData,
                                searchedValue = searchedValue,
                                displayOnlineState = true,
                            ) { context ->
                                sharedViewModel.updateFriend(newFriend = friendElement) {
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
                                }

                            }
                        }
                        if (filteredFriendsList.isEmpty() && !isDataLoaded) {
                            items(5) {
                                LoadingUserChatsElement()
                            }
                        }
                    }
                }
            })
        if (showBigUserImageDialog) {
            ShowBigUserImageDialog(
                userData = userData,
                friendData = friendData,
                onDismiss = { action ->
                    when (action) {
                        MESSAGE -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        BLOCK -> {
                            showBlockUserDialog = true

                        }

                        IMAGE -> {
                            coroutineScope.launch {
                                SaveImageTask(WeakReference(context)).saveImage(imageUrl = friendData.personList.image)
                            }
                        }

                        INFO -> {
                            chatData.find { it.chatRoomID == friendData.chatRoomID }
                                ?.let { chatsViewModel.createImageList(messages = it.messages) }
                                ?.let { sharedViewModel.updateImageList(it) }
                            navController.navigate(Screens.ProfileInfoScreen.route)
                        }


                        else -> {

                        }
                    }
                    showBigUserImageDialog = false
                })
        }
        if (showClearChatDialog) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    deleteAllChatMessages(
                        userData = userData, friendData = friendData
                    )
                }
                showClearChatDialog = false
            })
        }
        if (showBlockUserDialog) {
            BlockUserDialog(userData = userData, friendData = friendData) {
                if (it == "blocked") {
                    updateBlockedUserList(
                        userData = userData,
                        friendData = friendData.personList,
                        isAlreadyBlocked = userData.blocked.contains(friendData.personList.id)
                    )
                }
                showBlockUserDialog = false
            }
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

                                BottomSheetTagState.MUTE -> {
                                    if (userData.muted.contains(friendData.personList.id)) {
                                        updateMuteFriendStatus(
                                            userData = userData,
                                            friendData = friendData.personList,
                                            isAlreadyMuted = true
                                        )
                                    } else {
                                        updateMuteFriendStatus(
                                            userData = userData,
                                            friendData = friendData.personList,
                                            isAlreadyMuted = false
                                        )
                                    }
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