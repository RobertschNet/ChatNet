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
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.RemoveFriendDialog
import at.htlhl.chatnet.ui.features.mixed.ProfileChatNetIconSection
import at.htlhl.chatnet.ui.features.mixed.ProfileFriendsFromPersonSectionComponent
import at.htlhl.chatnet.ui.features.mixed.ProfileUserFriendStateSectionComponent
import at.htlhl.chatnet.util.firebase.deleteChatRoom
import at.htlhl.chatnet.util.firebase.removeFriendFromFriendsList
import at.htlhl.chatnet.viewmodels.SharedViewModel

@Composable
fun PublicUserSheetContentComponent(
    userData: FirebaseUser,
    publicUser: FirebaseUser,
    friendsFromPersonList: List<FirebaseUser>,
    friendState: PersonType?,
    chatData: List<FirebaseChat>,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    friendsFromPersonListLoading: Boolean
) {
    var removeFriendDialog by remember { mutableStateOf(false) }
    Spacer(modifier = Modifier.height(10.dp))
    if (friendsFromPersonList.isNotEmpty() || friendsFromPersonListLoading) {
        Spacer(modifier = Modifier.height(10.dp))
        ProfileFriendsFromPersonSectionComponent(
            friendsFromPersonList = friendsFromPersonList,
            friendsFromPersonListIsLoading = friendsFromPersonListLoading
        ) { clickedPerson ->
            sharedViewModel.updatePublicUser(newFriend = clickedPerson)
            navController.popBackStack()
            navController.navigate(Screens.PublicUserSheetScreen.route)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    ProfileUserFriendStateSectionComponent(
        userData = userData,
        publicUser = publicUser,
        chatData = chatData,
        friendState = friendState,
    ) {
        removeFriendDialog = true
    }
    Spacer(modifier = Modifier.height(20.dp))
    ProfileChatNetIconSection()
    if (removeFriendDialog) {
        RemoveFriendDialog { removeFriendPressed ->
            if (removeFriendPressed) {
                deleteChatRoom(publicUser = publicUser, chatData = chatData)
                removeFriendFromFriendsList(
                    userData = userData,
                    friendData = publicUser,
                    onSuccess = {
                        sharedViewModel.sortDataChats()
                    }
                )
                navController.navigate(Screens.ChatsViewScreen.route) {
                    popUpTo(Screens.PublicUserSheetScreen.route) {
                        inclusive = true
                    }
                }
            }
            removeFriendDialog = false
        }
    }
}