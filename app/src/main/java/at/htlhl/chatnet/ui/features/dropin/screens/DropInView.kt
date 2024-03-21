package at.htlhl.chatnet.ui.features.dropin.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.features.dialogs.ChangeBlockStateDialog
import at.htlhl.chatnet.ui.features.dialogs.ChangeDropInStateDialog
import at.htlhl.chatnet.ui.features.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.features.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.features.dropin.components.DropInNearbyPersonComponent
import at.htlhl.chatnet.ui.features.dropin.components.DropInUserComponent
import at.htlhl.chatnet.ui.features.dropin.viewmodels.DropInViewModel
import at.htlhl.chatnet.ui.features.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.features.mixed.TabsBottomSheetContent
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.util.firebase.createMediaImageList
import at.htlhl.chatnet.util.firebase.deleteAllChatMessages
import at.htlhl.chatnet.util.firebase.markMessagesAsRead
import at.htlhl.chatnet.util.firebase.saveChatRoom
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.util.firebase.updateMarkAsUnreadStatus
import at.htlhl.chatnet.util.firebase.updateMuteFriendStatus
import at.htlhl.chatnet.util.firebase.updatePinChatStatus
import at.htlhl.chatnet.util.generateBottomSheetItems
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class DropInView {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val dropInViewModel = viewModel<DropInViewModel>()
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )
        val context = LocalContext.current
        val dropInState by sharedViewModel.dropInState
        val searchedValue by sharedViewModel.searchValue
        val lazyColumnState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showBigUserImageDialog by remember { mutableStateOf(false) }
        var showClearChatDialog by remember { mutableStateOf(false) }
        var changeBlockStateDialog by remember { mutableStateOf(false) }
        var modelSheetState by remember { mutableStateOf(false) }
        var changeDropInStateDialog by remember { mutableStateOf(false) }

        val completeDopInChatListState by sharedViewModel.completeDropInList.collectAsState()
        val completeDropInNearbyUserListState by sharedViewModel.completeDropInNearbyUserList.collectAsState()
        val userDataState by sharedViewModel.userData.collectAsState()
        val friendDataState by sharedViewModel.friend.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()

        val completeDropInNearbyUserList = completeDropInNearbyUserListState
        val completeDopInChatList: List<InternalChatInstance> = completeDopInChatListState
        val userData: FirebaseUser = userDataState
        val friendData: InternalChatInstance = friendDataState
        val chatData: List<FirebaseChat> = chatDataState

        val dropInNearbyUsersListWithoutUser =
            sharedViewModel.nearbyDropInUsersList.value.filter { nearbyUsers ->
                nearbyUsers.id != userData.id
            }

        val dropInUserWithLocationInformation =
            sharedViewModel.nearbyDropInUsersList.value.find { nearbyUser ->
                nearbyUser.id == userData.id
            }
        val filteredDropInChatList = dropInViewModel.filterDropInPersonsList(
            searchedValue = searchedValue, completeDopInChatList = completeDopInChatList
        )
        val filteredNearbyUsersList = dropInViewModel.filterDropInNearbyUsersList(
            searchedValue = searchedValue, dropInUsersNearbyList = dropInNearbyUsersListWithoutUser
        )
        val doesSearchValueContainUsername = dropInViewModel.filterDropInNearbyUser(
            searchedValue = searchedValue,
            dropInUserWithLocationInformation = dropInUserWithLocationInformation
        )
        val bottomSheetItems = generateBottomSheetItems(
            isChatMate = false, friendData = friendData, userData = userData
        )
        Scaffold(backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(tab = CurrentTab.DROPIN, dropInState = dropInState, onActionClicked = {
                    changeDropInStateDialog = true
                }, onUpdateSearchValue = { updatedSearchValue ->
                    sharedViewModel.updateSearchValue(newSearchValue = updatedSearchValue)
                })
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = "Users in your area",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            item {
                                if (doesSearchValueContainUsername || searchedValue.isEmpty()) {
                                    DropInUserComponent(
                                        userData = userData,
                                        userDataWithLocationInformation = dropInUserWithLocationInformation,
                                        searchedValue = searchedValue,
                                        onUserImageClicked = {
                                            sharedViewModel.updatePublicUser(
                                                newFriend = userData,
                                                onComplete = {
                                                    navController.navigate(Screens.ProfilePictureView.route)
                                                })
                                        },
                                    )
                                }
                            }
                            items(filteredNearbyUsersList) { nearbyUser ->
                                DropInNearbyPersonComponent(userData = userData,
                                    nearbyUser = nearbyUser,
                                    searchedValue = searchedValue,
                                    onPersonImageClicked = { clickedPerson ->
                                        completeDropInNearbyUserList.find { it.personList.id == clickedPerson.id }
                                            ?.let { matchingNearbyUser ->
                                                if (matchingNearbyUser.chatRoomID.isEmpty()) {
                                                    saveChatRoom(
                                                        userID = userData.id,
                                                        friendID = matchingNearbyUser.personList.id,
                                                        tab = CurrentTab.DROPIN.name.lowercase(),
                                                        onChatCreated = { newChatChatRoomID ->
                                                            sharedViewModel.updateFriend(newFriend = InternalChatInstance().copy(
                                                                personList = matchingNearbyUser.personList,
                                                                chatRoomID = newChatChatRoomID
                                                            ), onComplete = {
                                                                navController.navigate(Screens.ChatViewScreen.route)
                                                            })
                                                        },
                                                    )
                                                } else {
                                                    sharedViewModel.updateFriend(newFriend = matchingNearbyUser,
                                                        onComplete = {
                                                            navController.navigate(Screens.ChatViewScreen.route)
                                                        })
                                                }
                                            }
                                    })
                            }
                        })

                    if (completeDopInChatList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(40.dp))
                        Row {
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "Users you chatted with",
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    LazyColumn(
                        state = lazyColumnState
                    ) {
                        items(filteredDropInChatList) { userInContactWith ->
                            ChatsViewChatItem(
                                friendElement = userInContactWith,
                                userData = userData,
                                displayOnlineState = true,
                                searchedValue = searchedValue,
                            ) { context ->
                                sharedViewModel.updateFriend(newFriend = userInContactWith,
                                    onComplete = {
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
                }
                if (changeDropInStateDialog) {
                    ChangeDropInStateDialog(
                        isDropInOn = !dropInState,
                        onChangeDropInState = { stateChanged ->
                            if (stateChanged) {
                                sharedViewModel.updateDropInState(newState = !dropInState)
                            }
                            changeDropInStateDialog = false
                        })
                }
            })
        if (showBigUserImageDialog) {
            ShowBigUserImageDialog(userData = userData,
                friendData = friendData,
                onDismiss = { action ->
                    when (action) {
                        INFO -> {
                            chatData.find { it.chatRoomID == friendData.chatRoomID }?.let {
                                createMediaImageList(
                                    userID = userData.id, messages = it.messages
                                )
                            }?.let { sharedViewModel.updateImageList(it) }
                            navController.navigate(Screens.UserSheetScreen.route)
                        }

                        BLOCK -> {
                            changeBlockStateDialog = true
                        }

                        MESSAGE -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        IMAGE -> {
                            coroutineScope.launch {
                                SaveImageTask(WeakReference(context)).saveImage(friendData.personList.image)
                            }
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
        if (changeBlockStateDialog) {
            ChangeBlockStateDialog(userData = userData,
                friendData = friendData,
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
        if (modelSheetState) {
            ModalBottomSheet(windowInsets = WindowInsets(0, 0, 0, 0), onDismissRequest = {
                modelSheetState = false
            }, dragHandle = null, content = {
                TabsBottomSheetContent(
                    friendData = friendData,
                    bottomSheetItems = bottomSheetItems,
                    onItemClicked = { item ->
                        modelSheetState = false
                        when (item) {
                            BottomSheetTagState.UNREAD -> {
                                if (friendData.read > 0) {
                                    markMessagesAsRead(userData = userData, friendData = friendData)
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

                            else -> {}
                        }
                    },
                )
            })
        }
    }
}
