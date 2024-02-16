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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.mixed.UserSheetUserHeaderComponent
import at.htlhl.chatnet.ui.features.usersheet.components.PublicUserSheetContentComponent
import at.htlhl.chatnet.ui.features.usersheet.viewmodels.UserSheetViewModel
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class PublicUserSheetView {

    @Composable
    fun PublicUserSheetScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val userSheetViewModel = viewModel<UserSheetViewModel>()

        val lazyColumnState = rememberLazyListState()
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )

        val publicUserState by sharedViewModel.publicUserData.collectAsState()
        val friendListState by sharedViewModel.friendListData.collectAsState()
        val userDataState by sharedViewModel.user.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()

        val userData: FirebaseUser = userDataState
        val friendList: List<FirebaseUser> = friendListState
        val publicUser: FirebaseUser = publicUserState
        val chatData: List<FirebaseChat> = chatDataState

        var friendsFromPersonListLoading by remember { mutableStateOf(true) }
        var friendsFromPersonList by remember { mutableStateOf(listOf<FirebaseUser>()) }

        val friendState = friendList.find { it.id == publicUser.id }?.statusFriend

        userSheetViewModel.fetchFriendsFromPerson(friend = publicUser,
            onSuccess = { fetchedPersons ->
                friendsFromPersonListLoading = false
                friendsFromPersonList = fetchedPersons
            })
        Scaffold(backgroundColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                LazyColumn(modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.onBackground)
                    .fillMaxSize(),
                    state = lazyColumnState,
                    content = {
                        item {
                            UserSheetUserHeaderComponent(friend = publicUser,
                                onNavigateToChatClicked = {
                                    navController.navigateUp()
                                },
                                onImageClick = {
                                    sharedViewModel.updatePublicUser(newFriend = publicUser,
                                        onComplete = {
                                            navController.navigate(Screens.ProfilePictureView.route)
                                        })
                                })
                        }
                        item {
                            PublicUserSheetContentComponent(
                                userData = userData,
                                publicUser = publicUser,
                                friendsFromPersonList = friendsFromPersonList,
                                friendState = friendState,
                                chatData = chatData,
                                sharedViewModel = sharedViewModel,
                                navController = navController,
                                friendsFromPersonListLoading = friendsFromPersonListLoading
                            )
                        }
                    })
            })
    }
}