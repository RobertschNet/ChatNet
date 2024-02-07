package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.dialogs.DeleteFriendDialog
import at.htlhl.chatnet.ui.components.mixed.ProfileChatNetIconSection
import at.htlhl.chatnet.ui.components.mixed.ProfileFriendsFromFriendsSection
import at.htlhl.chatnet.ui.components.mixed.ProfileInfoUserHeader
import at.htlhl.chatnet.ui.components.mixed.ProfileUserFriendStateSection
import at.htlhl.chatnet.viewmodels.SharedViewModel

class PublicProfileView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun PublicProfileScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        var friendsFromFriendsListLoading by remember { mutableStateOf(true) }
        val lazyListState = rememberLazyListState()
        val publicUserState = sharedViewModel.publicUserFlow.collectAsState()
        val publicUser: FirebaseUser = publicUserState.value
        val friendListState = sharedViewModel.friendListData.collectAsState()
        val friendList = friendListState.value
        val chatDataState = sharedViewModel.chatData.collectAsState()
        val chatData = chatDataState.value
        val friendState = friendList.find { it.id == publicUser.id }?.statusFriend
        var friendsFromFriendsList by remember { mutableStateOf(listOf<FirebaseUser>()) }
        sharedViewModel.fetchFriendsFriends(publicUser) {
            friendsFromFriendsListLoading = false
            friendsFromFriendsList = it
        }
        Scaffold(
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.BottomCenter),
                        state = lazyListState,
                        content = {
                            item {
                                ProfileInfoUserHeader(
                                    navController = navController,
                                    friend = publicUser
                                )
                            }
                            item {
                                PublicProfileScreenContent(
                                    publicUser = publicUser,
                                    friendsFromFriendsList = friendsFromFriendsList,
                                    friendState = friendState,
                                    chatData = chatData,
                                    sharedViewModel = sharedViewModel,
                                    navController = navController,
                                    friendsFromFriendsListLoading = friendsFromFriendsListLoading
                                )
                            }
                        }
                    )
                }
            }
        )
    }

    @Composable
    fun PublicProfileScreenContent(
        publicUser: FirebaseUser,
        friendsFromFriendsList: List<FirebaseUser>,
        friendState: String?,
        chatData: List<FirebaseChat>,
        sharedViewModel: SharedViewModel,
        navController: NavController,
        friendsFromFriendsListLoading: Boolean
    ) {
        var removeFriendDialog by remember { mutableStateOf(false) }
        Spacer(modifier = Modifier.height(10.dp))

        if (friendsFromFriendsList.isNotEmpty()||friendsFromFriendsListLoading) {
            Spacer(modifier = Modifier.height(10.dp))
            ProfileFriendsFromFriendsSection(friendsFromFriendsList = friendsFromFriendsList, friendsFromFriendsListIsLoading = friendsFromFriendsListLoading) {
                sharedViewModel.updatePublicUser(it)
                navController.popBackStack()
                navController.navigate(Screens.PublicProfileScreen.route)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ProfileUserFriendStateSection(
            publicUser = publicUser,
            chatData = chatData,
            friendState = friendState,
            sharedViewModel = sharedViewModel
        ) {
            removeFriendDialog = true
        }
        Spacer(modifier = Modifier.height(20.dp))
        ProfileChatNetIconSection()
        if (removeFriendDialog) {
            DeleteFriendDialog { value ->
                if (value == "deleted") {
                    sharedViewModel.deleteChatRoom(isPublic = true)
                    sharedViewModel.deleteFriendFromFriendList(publicUser)
                    navController.navigate(Screens.ChatsViewScreen.route) {
                        popUpTo(Screens.PublicProfileScreen.route) {
                            inclusive = true
                        }
                    }
                }
                removeFriendDialog = false
            }
        }
    }
}