package at.htlhl.chatnet.ui.features.randchat.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.finduser.components.FindUserPersonComponent
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.ui.features.randchat.components.RandChatUserOverviewComponent
import at.htlhl.chatnet.ui.features.randchat.viewmodels.RandChatViewModel
import at.htlhl.chatnet.util.cloudfunctions.requestRandChatPairingPartner
import at.htlhl.chatnet.util.firebase.changeFriendStateForPerson
import at.htlhl.chatnet.util.firebase.changeFriendStateForUser
import at.htlhl.chatnet.util.firebase.removeFriendFromFriendsList
import at.htlhl.chatnet.util.firebase.saveChatRoom
import at.htlhl.chatnet.util.firebase.updateChatRoomTab
import at.htlhl.chatnet.util.getPersonTagsList
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class RandChatStartView {

    @Composable
    fun RandChatStartScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val randChatViewModel = viewModel<RandChatViewModel>()

        val systemUiController = rememberSystemUiController()

        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )
        val searchedValue by sharedViewModel.searchValue
        val dropInState by sharedViewModel.dropInState

        val friendListDataState by sharedViewModel.friendListData.collectAsState()
        val userDataState by sharedViewModel.userData.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()

        val friendListData: List<FirebaseUser> = friendListDataState
        val userData: FirebaseUser = userDataState
        val chatData: List<FirebaseChat> = chatDataState

        val previousRandChatUsersList = sharedViewModel.previousRandChatUsersList.value

        val filteredPreviousRandChatUsersList = randChatViewModel.filterPreviousRandChatUsersList(
            previousRandChatUsersList = previousRandChatUsersList, searchedValue = searchedValue
        )
        val filteredUserTags = getPersonTagsList(personData = userData)
        Scaffold(backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(tab = CurrentTab.RANDCHAT,
                    dropInState = dropInState,
                    onActionClicked = {},
                    onUpdateSearchValue = { updatedSearchValue ->
                        sharedViewModel.updateSearchValue(newSearchValue = updatedSearchValue)
                    })
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                ) {
                    RandChatUserOverviewComponent(userData = userData,
                        filteredUserTags = filteredUserTags,
                        onUserProfilePictureClicked = {
                            sharedViewModel.updatePublicUser(newFriend = userData, onComplete = {
                                navController.navigate(Screens.ProfilePictureView.route)
                            })
                        },
                        onStartRandChatPressed = {
                            if (!userData.connected) {
                                requestRandChatPairingPartner(userID = userData.id,
                                    requestState = false,
                                    navController = navController,
                                    sharedViewModel = sharedViewModel,
                                   )
                            }
                            navController.navigate(Screens.RandChatScreen.route)
                        })
                    if (filteredPreviousRandChatUsersList.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "Recent RandChat Users",
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(filteredPreviousRandChatUsersList) { previousRandChatUser ->
                            val previousUserExistsInFriendList =
                                friendListData.find { previousRandChatUser.id == it.id }
                            FindUserPersonComponent(isFrontLayer = false,
                                person = previousRandChatUser,
                                deleteAble = false,
                                searchedText = searchedValue,
                                personType = if (previousUserExistsInFriendList == null) PersonType.SEARCHED_PERSON else if (previousUserExistsInFriendList.statusFriend == PersonType.PENDING_PERSON) PersonType.PENDING_PERSON else PersonType.ACCEPTED_PERSON,
                                onPersonClicked = { clickedPerson ->
                                    sharedViewModel.updatePublicUser(
                                        newFriend = clickedPerson,
                                        onComplete = {
                                            navController.navigate(Screens.PublicUserSheetScreen.route)
                                        })
                                },
                                onFriendActionClicked = { clickedPerson, add ->
                                    if (add) {
                                        val filteredChats = chatData.filter { chat ->
                                            chat.members.contains(clickedPerson.id) && chat.members.contains(
                                                userData.id
                                            )
                                        }
                                        if (filteredChats.isEmpty()) {
                                            saveChatRoom(
                                                userID = userData.id,
                                                friendID = clickedPerson.id,
                                                tab = CurrentTab.CHATS.name.lowercase(),
                                            )
                                        } else {
                                            updateChatRoomTab(
                                                newTab = CurrentTab.CHATS.name.lowercase(),
                                                chatRoomId = filteredChats[0].chatRoomID
                                            )
                                        }
                                        changeFriendStateForPerson(
                                            userID = userData.id,
                                            personID = clickedPerson.id,
                                            status = "accepted"
                                        )
                                        changeFriendStateForUser(
                                            userID = userData.id,
                                            personID = clickedPerson.id,
                                            status = "accepted"
                                        )
                                    } else {
                                        changeFriendStateForPerson(
                                            userID = userData.id,
                                            personID = clickedPerson.id,
                                            status = "pending"
                                        )
                                        changeFriendStateForUser(
                                            userID = userData.id,
                                            personID = clickedPerson.id,
                                            status = "requested"
                                        )
                                    }
                                },
                                onDenyFriendRequestClicked = { clickedPerson ->
                                    removeFriendFromFriendsList(userData = userData,
                                        friendData = clickedPerson,
                                        onSuccess = {
                                            sharedViewModel.sortDataChats()
                                        })
                                })
                        }
                    }
                }
            })
    }
}