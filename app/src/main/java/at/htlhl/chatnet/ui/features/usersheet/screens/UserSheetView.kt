package at.htlhl.chatnet.ui.features.usersheet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.mixed.UserSheetUserHeaderComponent
import at.htlhl.chatnet.ui.features.usersheet.components.UserSheetContentComponent
import at.htlhl.chatnet.ui.features.usersheet.viewmodels.UserSheetViewModel
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class UserSheetView {
    @Composable
    fun UserSheetScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val userSheetViewModel = viewModel<UserSheetViewModel>()

        val lazyColumnState = rememberLazyListState()
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )

        val friendDataState by sharedViewModel.friend.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()
        val friendListDataState by sharedViewModel.friendListData.collectAsState()
        val userDataState by sharedViewModel.userData.collectAsState()

        val friendData: InternalChatInstance = friendDataState
        val chatData: List<FirebaseChat> = chatDataState
        val friendListData: List<FirebaseUser> = friendListDataState
        val userData: FirebaseUser = userDataState

        val chatImagesList = sharedViewModel.imageList.value.toMutableStateList()

        var friendsFromPersonListLoading by remember { mutableStateOf(true) }
        var friendsFromPersonList by remember { mutableStateOf(listOf<FirebaseUser>()) }

        val friendsFriendState =
            friendListData.find { it.id == friendData.personList.id }?.statusFriend
        userSheetViewModel.fetchFriendsFromPerson(
            friend = friendData.personList,
            onSuccess = { fetchedPersons ->
                friendsFromPersonListLoading = false
                friendsFromPersonList = fetchedPersons
            })
        Scaffold(backgroundColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                LazyColumn(modifier = Modifier
                    .background(MaterialTheme.colorScheme.onBackground)
                    .padding(top = paddingValues.calculateTopPadding())
                    .fillMaxSize(),
                    state = lazyColumnState,
                    content = {
                        item {
                            UserSheetUserHeaderComponent(friendData = friendData.personList,
                                userData = userData,
                                onNavigateToChatClicked = {
                                    navController.navigateUp()
                                },
                                onImageClick = {
                                    sharedViewModel.updatePublicUser(
                                        newFriend = friendData.personList,
                                        onComplete = {
                                            navController.navigate(Screens.ProfilePictureView.route)
                                        })
                                })
                        }
                        item {
                            UserSheetContentComponent(
                                friendsFriendState = friendsFriendState,
                                chatData = chatData,
                                sharedViewModel = sharedViewModel,
                                navController = navController,
                                friendsFromPersonList = friendsFromPersonList,
                                imageList = chatImagesList,
                                friendData = friendData,
                                userData = userData,
                                friendsFromPersonsListIsLoading = friendsFromPersonListLoading
                            )
                        }
                    })

            })
    }
}