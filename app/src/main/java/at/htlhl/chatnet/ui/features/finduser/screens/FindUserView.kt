package at.htlhl.chatnet.ui.features.finduser.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.finduser.components.FindUserBackLayerContent
import at.htlhl.chatnet.ui.features.finduser.components.FindUserFrontLayerContent
import at.htlhl.chatnet.ui.features.finduser.components.FindUserTopSearchBar
import at.htlhl.chatnet.ui.features.finduser.viewmodels.FindUserViewModel
import at.htlhl.chatnet.util.firebase.changeFriendStateForPerson
import at.htlhl.chatnet.util.firebase.changeFriendStateForUser
import at.htlhl.chatnet.util.firebase.removeFriendFromFriendsList
import at.htlhl.chatnet.util.firebase.saveChatRoom
import at.htlhl.chatnet.util.firebase.updateChatRoomTab
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

class FindUserView {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun FindUserScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val findUserViewModel = viewModel<FindUserViewModel>()
        val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
        val coroutineScope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() }
        val searchedText by sharedViewModel.searchValue

        val searchedUsers by findUserViewModel.foundUsers.collectAsState(initial = emptyList())
        val searchUserText by findUserViewModel.searchUserTextFlow.collectAsState()
        val isSearching by findUserViewModel.isSearchingFlow.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()
        val suggestedFriendsListState by sharedViewModel.friendRandomFriendsListData.collectAsState()
        val friendListDataState by sharedViewModel.friendListData.collectAsState()
        val userDataState by sharedViewModel.userData.collectAsState()

        val chatData: List<FirebaseChat> = chatDataState
        val suggestedFriendsList: List<FirebaseUser> = suggestedFriendsListState
        val friendListData: List<FirebaseUser> = friendListDataState
        val userData: FirebaseUser = userDataState

        val pendingFriendsList =
            friendListData.filter { friend -> friend.statusFriend == PersonType.PENDING_PERSON }

        val filteredSuggestedFriendList = suggestedFriendsList.filter { friend ->
            friendListData.none { it.id == friend.id } && friend.id != sharedViewModel.auth.currentUser?.uid.toString()
        }

        BackdropScaffold(
            scaffoldState = scaffoldState,
            frontLayerShape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
            headerHeight = 100.dp,
            stickyFrontLayer = false,
            backLayerBackgroundColor = MaterialTheme.colorScheme.background,
            persistentAppBar = true,
            peekHeight = LocalConfiguration.current.screenHeightDp.dp / 3,
            frontLayerBackgroundColor = MaterialTheme.colorScheme.onBackground,
            frontLayerScrimColor = Color.Transparent,
            frontLayerElevation = 10.dp,
            modifier = Modifier.fillMaxSize(),
            appBar = {
                FindUserTopSearchBar(
                    interactionSource = interactionSource,
                    onClicked = {
                        coroutineScope.launch {
                            scaffoldState.reveal()
                        }
                    },
                    onTextChanged = { text ->
                        findUserViewModel.onSearchTextChanged(text)
                    },
                    onNavigate = {
                        navController.navigate(Screens.ChatsViewScreen.route) {
                            popUpTo(Screens.FindUserScreen.route) { inclusive = true }

                        }
                    }
                )
            },
            frontLayerContent = {
                FindUserFrontLayerContent(
                    pendingFriendsList = pendingFriendsList,
                    filteredSuggestedFriendList = filteredSuggestedFriendList,
                    searchedText = searchedText,
                    onPersonClicked = { clickedPerson ->
                        sharedViewModel.updatePublicUser(clickedPerson)
                        navController.navigate(Screens.PublicUserSheetScreen.route)
                    },
                    onDenyFriendRequestClicked = { clickedPerson ->
                        removeFriendFromFriendsList(
                            userData = userData,
                            friendData = clickedPerson,
                            onSuccess = {
                                sharedViewModel.sortDataChats()
                            }
                        )
                    },
                    onFriendActionClicked = { clickedPerson, addFriend ->
                        if (addFriend) {
                            val filteredChats = chatData.filter { chat ->
                                chat.members.contains(clickedPerson.id) && chat.members
                                    .contains(sharedViewModel.auth.currentUser?.uid)
                            }
                            if (filteredChats.isEmpty()) {
                                saveChatRoom(
                                    userID = userData.id,
                                    friendID = clickedPerson.id,
                                    tab = CurrentTab.CHATS.name.lowercase()
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
                    }
                )

            }, backLayerContent = {
                FindUserBackLayerContent(
                    friendList = friendListData,
                    searchedPersons = searchedUsers,
                    isSearching = isSearching,
                    searchUserText = searchUserText,
                    searchedText = searchedText,
                    onPersonClicked = { clickedPerson ->
                        sharedViewModel.updatePublicUser(clickedPerson)
                        navController.navigate(Screens.PublicUserSheetScreen.route)
                    },
                    onFriendActionClicked = { clickedPerson, addFriend ->
                        if (addFriend) {
                            val filteredChats = chatData.filter { chat ->
                                chat.members.contains(clickedPerson.id) && chat.members
                                    .contains(sharedViewModel.auth.currentUser?.uid)
                            }
                            if (filteredChats.isEmpty()) {
                                saveChatRoom(
                                    userID = userData.id,
                                    friendID = clickedPerson.id,
                                    tab = CurrentTab.CHATS.name.lowercase()
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
                        removeFriendFromFriendsList(
                            userData = userData,
                            friendData = clickedPerson,
                            onSuccess = {
                                sharedViewModel.sortDataChats()
                            }
                        )
                    },
                )
            }
        )
    }
}