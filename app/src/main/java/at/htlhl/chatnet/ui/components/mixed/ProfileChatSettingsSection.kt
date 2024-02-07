package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileChatSettingsSection(
    isChatMateChat: Boolean,
    sharedViewModel: SharedViewModel,
    friend: InternalChatInstance,
    user: FirebaseUser,
    onDeleteAllMedia: () -> Unit,
    onDeleteAllMessages: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        elevation = 10.dp,
        shape = RoundedCornerShape(25.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            Spacer(modifier = Modifier.height(7.5f.dp))
            Text(
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 15.dp),
                text = "Chat Settings",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
            if (!isChatMateChat) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            if (user.muted.contains(friend.personList.id)) {
                                sharedViewModel.updateMuteFriendStatus(true)
                                sharedViewModel.updateFriend(friend)
                            } else {
                                sharedViewModel.updateMuteFriendStatus(false)
                                sharedViewModel.updateFriend(friend)
                            }
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = if (user.muted.contains(friend.personList.id)) R.drawable.speaker_svgrepo_com else R.drawable.speaker_none_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = if (user.muted.contains(friend.personList.id)) "Unmute ${friend.personList.username["mixedcase"]}" else "Mute ${friend.personList.username["mixedcase"]}",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable {
                        if (friend.pinChat) {
                            sharedViewModel.updatePinChatStatus(true)
                            sharedViewModel.updateFriend(
                                InternalChatInstance(
                                    personList = friend.personList,
                                    timestampMessage = friend.timestampMessage,
                                    lastMessage = friend.lastMessage,
                                    pinChat = false,
                                    read = friend.read,
                                    markedAsUnread = friend.markedAsUnread,
                                    chatRoomID = friend.chatRoomID
                                )
                            )
                        } else {
                            sharedViewModel.updatePinChatStatus(false)
                            sharedViewModel.updateFriend(
                                InternalChatInstance(
                                    friend.personList,
                                    timestampMessage = friend.timestampMessage,
                                    lastMessage = friend.lastMessage,
                                    pinChat = true,
                                    read = friend.read,
                                    markedAsUnread = friend.markedAsUnread,
                                    chatRoomID = friend.chatRoomID
                                )
                            )
                        }
                    }
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(25.dp))
                SubcomposeAsyncImage(
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.Black),
                    model = if (friend.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                    modifier = Modifier.size(30.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    text = if (friend.pinChat) "Unpin Chat" else "Pin Chat",
                    fontSize = 16.sp,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable { onDeleteAllMessages.invoke() }
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(25.dp))
                SubcomposeAsyncImage(
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.Black),
                    model = R.drawable.comment_delete_svgrepo_com,
                    modifier = Modifier.size(30.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    text = "Delete Chat Messages",
                    fontSize = 16.sp,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
            if (!isChatMateChat) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable { onDeleteAllMedia.invoke() }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = R.drawable.gallery_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Delete Shared Media",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
        }
    }
}
