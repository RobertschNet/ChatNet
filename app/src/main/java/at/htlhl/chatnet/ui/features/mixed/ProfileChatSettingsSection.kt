package at.htlhl.chatnet.ui.features.mixed

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import at.htlhl.chatnet.util.firebase.updateMuteFriendStatus
import at.htlhl.chatnet.util.firebase.updatePinChatStatus
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileChatSettingsSection(
    isChatMateChat: Boolean,
    friendData: InternalChatInstance,
    userData: FirebaseUser,
    onDeleteAllMedia: () -> Unit,
    onDeleteAllMessages: () -> Unit,
    onUpdateFriend: (InternalChatInstance) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        elevation = 10.dp,
        shape = RoundedCornerShape(25.dp),
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Column {
            Spacer(modifier = Modifier.height(7.5f.dp))
            Text(
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 15.dp),
                text = "Chat Settings",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (!isChatMateChat) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            if (userData.muted.contains(friendData.personList.id)) {
                                updateMuteFriendStatus(
                                    userData = userData,
                                    friendData = friendData.personList,
                                    isAlreadyMuted = true)
                                onUpdateFriend(friendData)
                            } else {
                                updateMuteFriendStatus(
                                    userData = userData,
                                    friendData = friendData.personList,
                                    isAlreadyMuted = false)
                                onUpdateFriend(friendData)
                            }
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        model = if (userData.muted.contains(friendData.personList.id)) R.drawable.speaker_svgrepo_com else R.drawable.speaker_none_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif,
                        text = if (userData.muted.contains(friendData.personList.id)) "Unmute ${friendData.personList.username["mixedcase"]}" else "Mute ${friendData.personList.username["mixedcase"]}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable {
                        if (friendData.pinChat) {
                            updatePinChatStatus(
                                userData = userData,
                                friend = friendData,
                                isAlreadyPinned = true
                            )
                            onUpdateFriend(
                                InternalChatInstance(
                                    personList = friendData.personList,
                                    timestampMessage = friendData.timestampMessage,
                                    lastMessage = friendData.lastMessage,
                                    pinChat = false,
                                    read = friendData.read,
                                    markedAsUnread = friendData.markedAsUnread,
                                    chatRoomID = friendData.chatRoomID
                                )
                            )

                        } else {
                            updatePinChatStatus(
                                userData = userData,
                                friend = friendData,
                                isAlreadyPinned = false
                            )
                            onUpdateFriend(
                                InternalChatInstance(
                                    friendData.personList,
                                    timestampMessage = friendData.timestampMessage,
                                    lastMessage = friendData.lastMessage,
                                    pinChat = true,
                                    read = friendData.read,
                                    markedAsUnread = friendData.markedAsUnread,
                                    chatRoomID = friendData.chatRoomID
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    model = if (friendData.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                    modifier = Modifier.size(30.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    text = if (friendData.pinChat) "Unpin Chat" else "Pin Chat",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.primary,
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.primary,
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
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
        }
    }
}
