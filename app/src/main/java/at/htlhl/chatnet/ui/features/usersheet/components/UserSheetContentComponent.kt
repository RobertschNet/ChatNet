package at.htlhl.chatnet.ui.features.usersheet.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.ChangeBlockStateDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteAllMediaDialog
import at.htlhl.chatnet.ui.features.dialogs.DeleteAllMessagesDialog
import at.htlhl.chatnet.ui.features.dialogs.RemoveFriendDialog
import at.htlhl.chatnet.ui.features.mixed.ProfileChatNetIconSection
import at.htlhl.chatnet.ui.features.mixed.ProfileChatSettingsSection
import at.htlhl.chatnet.ui.features.mixed.ProfileFriendSettingsSection
import at.htlhl.chatnet.ui.features.mixed.ProfileFriendsFromPersonSectionComponent
import at.htlhl.chatnet.ui.features.mixed.ProfileMediaAndLinksSection
import at.htlhl.chatnet.ui.features.mixed.ProfileUserFriendStateSectionComponent
import at.htlhl.chatnet.util.firebase.changeMediaVisibility
import at.htlhl.chatnet.util.firebase.deleteChatRoom
import at.htlhl.chatnet.util.firebase.removeFriendFromFriendsList
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.viewmodels.SharedViewModel

@Composable
fun UserSheetContentComponent(
    friendsFriendState: PersonType?,
    chatData: List<FirebaseChat>,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    friendsFromPersonList: List<FirebaseUser>,
    imageList: List<InternalMessageInstance>,
    friendData: InternalChatInstance,
    userData: FirebaseUser,
    friendsFromPersonsListIsLoading: Boolean
) {
    var changeBlockStateDialog by remember { mutableStateOf(false) }
    var removeFriendDialog by remember { mutableStateOf(false) }
    var deleteAllMediaDialog by remember { mutableStateOf(false) }
    var deleteAllMessagesDialog by remember { mutableStateOf(false) }
    val currentChat = chatData.find { it.chatRoomID == friendData.chatRoomID }
    Spacer(modifier = Modifier.height(10.dp))
    if (imageList.isNotEmpty()) {
        ProfileMediaAndLinksSection(imageList = imageList, onImageClicked = { imageIndex ->
            sharedViewModel.imageStartPosition.intValue = imageIndex
            navController.navigate(Screens.ImageViewScreen.route)

        }, onOpenImageViewClicked = {
            sharedViewModel.imageStartPosition.intValue = 0
            navController.navigate(Screens.ImageViewScreen.route)
        })
        Spacer(modifier = Modifier.height(10.dp))
    }
    ProfileChatSettingsSection(isChatMateChat = currentChat?.tab == "chatmate",
        friendData = friendData,
        userData = userData,
        onDeleteAllMedia = {
            deleteAllMediaDialog = true
        },
        onDeleteAllMessages = {
            deleteAllMessagesDialog = true
        },
        onUpdateFriend = { newFriend ->
            sharedViewModel.updateFriend(newFriend = newFriend)
        })
    Spacer(modifier = Modifier.height(10.dp))
    ProfileFriendSettingsSection(isChatMateChat = currentChat?.tab == "chatmate",
        userData = userData,
        friendData = friendData,
        onBlockAction = { changeBlockStateDialog = true },
        onRemoveUserAction = {
            removeFriendDialog = true
        })
    Spacer(modifier = Modifier.height(10.dp))
    if (friendsFromPersonList.isNotEmpty() || friendsFromPersonsListIsLoading) {
        ProfileFriendsFromPersonSectionComponent(
            friendsFromPersonList = friendsFromPersonList,
            friendsFromPersonListIsLoading = friendsFromPersonsListIsLoading
        ) { clickedPerson ->
            sharedViewModel.updatePublicUser(newFriend = clickedPerson)
            navController.navigate(Screens.PublicUserSheetScreen.route)
        }
    }
    if (currentChat != null && currentChat.tab == "dropin") {
        Spacer(modifier = Modifier.height(10.dp))
        ProfileUserFriendStateSectionComponent(
            userData = userData,
            chatData = chatData,
            publicUser = friendData.personList,
            friendState = friendsFriendState,
        ) {
            removeFriendDialog = true
        }

    }
    Spacer(modifier = Modifier.height(20.dp))
    ProfileChatNetIconSection()
    if (removeFriendDialog) {
        RemoveFriendDialog { removeFriendPressed ->
            if (removeFriendPressed) {
                deleteChatRoom(friendData = friendData)
                if (currentChat?.tab != "chatmate") {
                    removeFriendFromFriendsList(
                        userData = userData,
                        friendData = friendData.personList,
                        onSuccess = {
                            sharedViewModel.sortDataChats {}
                        })
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
    if (changeBlockStateDialog) {
        ChangeBlockStateDialog(friendData = friendData,
            userData = userData,
            onChangeBlockStateClicked = { stateChanged ->
                if (stateChanged) {
                    updateBlockedUserList(
                        userData = userData,
                        friendData = friendData.personList,
                        userData.blocked.contains(
                            friendData.personList.id
                        )
                    )
                }
                changeBlockStateDialog = false
            })
    }
    if (deleteAllMediaDialog) {
        DeleteAllMediaDialog { value ->
            if (value == "me") {
                changeMediaVisibility(
                    userID = userData.id,
                    friendData = friendData,
                    userContext = true,
                    isMedia = true,
                )
            } else if (value == "everyone") {
                changeMediaVisibility(
                    userID = userData.id,
                    friendData = friendData,
                    userContext = false,
                    isMedia = true,
                )
            }
            deleteAllMediaDialog = false
        }
    }
    if (deleteAllMessagesDialog) {
        DeleteAllMessagesDialog(isChatMateChat = currentChat?.tab == "chatmate") { value ->
            if (value == "me") {
                changeMediaVisibility(
                    userID = userData.id,
                    friendData = friendData,
                    userContext = true,
                    isMedia = false
                )
            } else if (value == "everyone") {
                changeMediaVisibility(
                    userID = userData.id,
                    friendData = friendData,
                    userContext = false, isMedia = false,
                )
            }
            deleteAllMessagesDialog = false
        }
    }
}