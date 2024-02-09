package at.htlhl.chatnet.ui.components.finduser

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.LoadingUserElement
import at.htlhl.chatnet.viewmodels.SharedViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FindUserSearchedContent(
    friendList: List<FirebaseUser>,
    chatData: List<FirebaseChat>,
    persons: List<FirebaseUser>,
    isSearching: Boolean,
    searchText: String,
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    Divider(thickness = 0.25f.dp, color =MaterialTheme.colorScheme.outline)
    if (isSearching) {
        LazyColumn(content = {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            items(10) {
                LoadingUserElement(true)
            }
        })
    }
    if (searchText.isNotEmpty()) {
        Text(
            text = "Results for \"$searchText\"",
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp)
                .fillMaxWidth()

        )
    }
    persons.forEach { person ->
        if (person.doesMatchUsername(searchText) && searchText.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize())
            {
                items(persons) { person ->
                    val specificUser = friendList.find { it.id == person.id }
                    FindUserPersonElement(
                        isFrontLayer = false,
                        person = person,
                        deleteAble = false,
                        sharedViewModel = sharedViewModel,
                        searchedUser = if (specificUser == null) "searchedUser" else if (specificUser.statusFriend == "pending") "pending" else "accepted",
                        onUserClicked = {
                            sharedViewModel.updatePublicUser(it)
                            navController.navigate(Screens.PublicProfileScreen.route)
                        },
                        onActionClicked = { clickedPerson, add ->
                            if (add) {
                                val filteredChats = chatData.filter { chat ->
                                    chat.members.contains(clickedPerson.id) && chat.members
                                        .contains(sharedViewModel.auth.currentUser?.uid)
                                }
                                if (filteredChats.isEmpty()) {
                                    sharedViewModel.saveChatRoom(
                                        person = clickedPerson.id,
                                        tab = "chats"
                                    )
                                } else {
                                    sharedViewModel.updateChatRoom(
                                        tab = "chats",
                                        chatRoomId = filteredChats[0].chatRoomID
                                    )
                                }
                                sharedViewModel.saveFriendForFriend(
                                    person = clickedPerson,
                                    status = "accepted"
                                )
                                sharedViewModel.saveFriendForUser(
                                    person = clickedPerson,
                                    status = "accepted"
                                )
                            } else {
                                sharedViewModel.saveFriendForFriend(
                                    person = clickedPerson,
                                    status = "pending"
                                )
                                sharedViewModel.saveFriendForUser(
                                    person = clickedPerson,
                                    status = "requested"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}