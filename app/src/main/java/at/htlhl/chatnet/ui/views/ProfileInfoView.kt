package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.BlockUserDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteAllMediaDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteAllMessagesDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteFriendDialog
import at.htlhl.chatnet.ui.features.mixed.ProfileChatNetIconSection
import at.htlhl.chatnet.ui.features.mixed.ProfileChatSettingsSection
import at.htlhl.chatnet.ui.features.mixed.ProfileFriendSettingsSection
import at.htlhl.chatnet.ui.features.mixed.ProfileFriendsFromFriendsSection
import at.htlhl.chatnet.ui.features.mixed.ProfileInfoUserHeader
import at.htlhl.chatnet.ui.features.mixed.ProfileMediaAndLinksSection
import at.htlhl.chatnet.ui.features.mixed.ProfileUserFriendStateSection
import at.htlhl.chatnet.util.firebase.deleteChatRoom
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ProfileInfoView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ProfileInfoScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme())
        var friendsFromFriendsListLoading by remember { mutableStateOf(true) }
        val lazyListState = rememberLazyListState()
        val friendState = sharedViewModel.friend.collectAsState()
        val friend: InternalChatInstance = friendState.value
        val friendListDataState = sharedViewModel.friendListData.collectAsState()
        val friendListData = friendListDataState.value
        val userState = sharedViewModel.user.collectAsState()
        val user: FirebaseUser = userState.value
        val chatDataState = sharedViewModel.chatData.collectAsState()
        val chatData = chatDataState.value
        val imageList = sharedViewModel.imageList.value.toMutableStateList()
        val friendsFriendState = friendListData.find { it.id == friend.personList.id }?.statusFriend
        var friendsFromFriendsList by remember { mutableStateOf(listOf<FirebaseUser>()) }
        sharedViewModel.fetchFriendsFriends(friend.personList) {
            friendsFromFriendsListLoading = false
            friendsFromFriendsList = it
        }
        Scaffold(
            backgroundColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize(),
            content = {
                LazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onBackground)
                        .fillMaxSize(),
                    state = lazyListState,
                    content = {
                        item {
                            ProfileInfoUserHeader(
                                navController = navController,
                                friend = friend.personList
                            )
                        }
                        item {
                            ProfileInfoContent(
                                friendsFriendState,
                                chatData,
                                sharedViewModel,
                                navController,
                                friendsFromFriendsList,
                                imageList,
                                friend,
                                user,
                                friendsFromFriendsListLoading
                            )
                        }
                    }
                )

            }
        )
    }

    @Composable
    fun ProfileInfoContent(
        friendsFriendState: PersonType?,
        chatData: List<FirebaseChat>,
        sharedViewModel: SharedViewModel,
        navController: NavController,
        friendsFromFriendsList: List<FirebaseUser>,
        imageList: List<InternalMessageInstance>,
        friend: InternalChatInstance,
        userData: FirebaseUser,
        friendsFromFriendsListIsLoading: Boolean
    ) {
        var blockDialog by remember { mutableStateOf(false) }
        var removeFriendDialog by remember { mutableStateOf(false) }
        var deleteAllMediaDialog by remember { mutableStateOf(false) }
        var deleteAllMessagesDialog by remember { mutableStateOf(false) }
        val currentChat = chatData.find { it.chatRoomID == friend.chatRoomID }
        Spacer(modifier = Modifier.height(10.dp))
        if (imageList.isNotEmpty()) {
            ProfileMediaAndLinksSection(
                imageList = imageList,
                navController = navController,
                sharedViewModel = sharedViewModel
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        ProfileChatSettingsSection(
            isChatMateChat = currentChat?.tab == "chatmate",
            sharedViewModel = sharedViewModel,
            friend = friend,
            user = userData,
            onDeleteAllMedia = {
                deleteAllMediaDialog = true
            },
            onDeleteAllMessages = {
                deleteAllMessagesDialog = true
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ProfileFriendSettingsSection(
            isChatMateChat = currentChat?.tab == "chatmate",
            user = userData,
            friend = friend,
            onBlockAction = { blockDialog = true },
            onRemoveUserAction = {
                removeFriendDialog = true
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (friendsFromFriendsList.isNotEmpty() || friendsFromFriendsListIsLoading) {
            ProfileFriendsFromFriendsSection(
                friendsFromFriendsList = friendsFromFriendsList,
                friendsFromFriendsListIsLoading = friendsFromFriendsListIsLoading
            ) {
                sharedViewModel.updatePublicUser(it)
                navController.navigate(Screens.PublicProfileScreen.route)
            }
        }
        if (currentChat != null && currentChat.tab == "dropin") {
            Spacer(modifier = Modifier.height(10.dp))
            ProfileUserFriendStateSection(
                chatData = chatData,
                publicUser = friend.personList,
                friendState = friendsFriendState,
                sharedViewModel = sharedViewModel
            ) {
                removeFriendDialog = true
            }

        }
        Spacer(modifier = Modifier.height(20.dp))
        ProfileChatNetIconSection()
        if (removeFriendDialog) {
            DeleteFriendDialog { value ->
                if (value == "deleted") {
                    deleteChatRoom(friendData = friend)
                    if (currentChat?.tab != "chatmate") {
                        sharedViewModel.deleteFriendFromFriendList(friend.personList)
                    }
                    if (navController.previousBackStackEntry?.destination?.route != Screens.ChatViewScreen.route) {
                        navController.navigateUp()
                    } else {
                        navController.navigate(Screens.ChatsViewScreen.route)
                    }
                }
                removeFriendDialog = false
            }
        }
        if (blockDialog) {
            BlockUserDialog(
                friendData = friend,
                userData = userData
            ) { value ->
                if (value == "blocked") {
                    updateBlockedUserList(
                        userData = userData,
                        friendData = friend.personList,
                        userData.blocked.contains(
                            friend.personList.id
                        )
                    )
                }
                blockDialog = false
            }
        }
        if (deleteAllMediaDialog) {
            DeleteAllMediaDialog { value ->
                if (value == "me") {
                    sharedViewModel.changeMediaVisibility(userContext = true, isMedia = true)
                } else if (value == "everyone") {
                    sharedViewModel.changeMediaVisibility(userContext = false, isMedia = true)
                }
                deleteAllMediaDialog = false
            }
        }
        if (deleteAllMessagesDialog) {
            DeleteAllMessagesDialog(isChatMateChat = currentChat?.tab == "chatmate") { value ->
                if (value == "me") {
                    sharedViewModel.changeMediaVisibility(userContext = true, isMedia = false)
                } else if (value == "everyone") {
                    sharedViewModel.changeMediaVisibility(userContext = false, isMedia = false)
                }
                deleteAllMessagesDialog = false
            }
        }
    }
}