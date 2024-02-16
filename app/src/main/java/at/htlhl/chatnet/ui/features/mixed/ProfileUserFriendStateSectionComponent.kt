package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.util.firebase.cancelFriendRequest
import at.htlhl.chatnet.util.firebase.changeFriendStateForPerson
import at.htlhl.chatnet.util.firebase.changeFriendStateForUser
import at.htlhl.chatnet.util.firebase.saveChatRoom
import at.htlhl.chatnet.util.firebase.updateChatRoomTab

@Composable
fun ProfileUserFriendStateSectionComponent(
    userData: FirebaseUser,
    chatData: List<FirebaseChat>,
    publicUser: FirebaseUser,
    friendState: PersonType?,
    onRemoveFriendAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        elevation = 10.dp,
        shape = RoundedCornerShape(25.dp),
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Spacer(modifier = Modifier.height(7.5f.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    text = "Friend State",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 15.dp),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(2.5f.dp))
                Column(content = {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00A0E8), contentColor = Color.White
                            ),
                            onClick = {
                                when (friendState) {
                                    PersonType.PENDING_PERSON -> {
                                        val filteredChats = chatData.filter { chat ->
                                            chat.members.contains(publicUser.id) && chat.members.contains(
                                                    userData.id
                                                )
                                        }
                                        if (filteredChats.isEmpty()) {
                                            saveChatRoom(
                                                userID = userData.id,
                                                friendID = publicUser.id,
                                                tab = CurrentTab.CHATS
                                            )
                                        } else {
                                            updateChatRoomTab(
                                                newTab = CurrentTab.CHATS,
                                                chatRoomId = filteredChats[0].chatRoomID
                                            )
                                        }
                                        changeFriendStateForPerson(
                                            userID = userData.id,
                                            personID = publicUser.id,
                                            status = "accepted"
                                        )
                                        changeFriendStateForUser(
                                            userID = userData.id,
                                            personID = publicUser.id,
                                            status = "accepted"
                                        )
                                    }

                                    PersonType.ACCEPTED_PERSON -> {
                                        onRemoveFriendAction()
                                    }

                                    PersonType.REQUESTED_PERSON -> {
                                        cancelFriendRequest(
                                            userID = userData.id, person = publicUser
                                        )
                                    }

                                    else -> {
                                        changeFriendStateForPerson(
                                            userID = userData.id,
                                            personID = publicUser.id,
                                            status = "pending"
                                        )
                                        changeFriendStateForUser(
                                            userID = userData.id,
                                            personID = publicUser.id,
                                            status = "requested"
                                        )
                                    }
                                }
                            }) {
                            Text(
                                text = if (friendState == PersonType.PENDING_PERSON) "Accept Request" else if (friendState == PersonType.ACCEPTED_PERSON) "Remove Friend" else if (friendState == PersonType.REQUESTED_PERSON) "Cancel Request" else "Add Friend",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (friendState == PersonType.PENDING_PERSON) "This User wants to follow you, click the Button to become Friends with him." else if (friendState == PersonType.ACCEPTED_PERSON) "You are already Friends with this User, click the button to remove him as Friend." else if (friendState == PersonType.REQUESTED_PERSON) "You have sent this User a Friend-request, click the Button to cancel it." else "You can send this User a Friend-request to start chatting with him, click the Button to send it.",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                        )

                    }
                    Spacer(modifier = Modifier.height(5.dp))
                })
            }
        }
    }
}