package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import at.htlhl.chatnet.data.ChatsChatItemClickState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.util.firebase.updateMarkAsUnreadStatus
import at.htlhl.chatnet.util.highlightSearchedText
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsViewChatItem(
    friendElement: InternalChatInstance,
    userData: FirebaseUser,
    searchedValue: String,
    displayOnlineState: Boolean,
    onClick: (ChatsChatItemClickState) -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(friendElement.timestampMessage.toDate())
    if (friendElement.read > 0) {
        updateMarkAsUnreadStatus(
            userData = userData,
            friendData = friendElement,
            isAlreadyUnread = true
        )
    }
    Row(
        modifier =
        Modifier
            .combinedClickable(
                onClick = {
                    onClick.invoke(ChatsChatItemClickState.MESSAGE)
                },
                onLongClick = {
                    onClick.invoke(ChatsChatItemClickState.CONTEXT_MENU)
                },
            )
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            if (!friendElement.personList.blocked.contains(userData.id)) {
                SubcomposeAsyncImage(
                    contentDescription = null,
                    model = friendElement.personList.image,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp)
                        .shimmerEffect()
                        .clickable {
                            onClick.invoke(ChatsChatItemClickState.IMAGE)
                        },
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                )
            } else {
                SubcomposeAsyncImage(
                    model = R.drawable.default_user,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp)
                        .clickable {
                            onClick.invoke(ChatsChatItemClickState.IMAGE)
                        },
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                )
            }
            if (displayOnlineState) {
                Box(
                    modifier = Modifier
                        .size(16.5f.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    if (friendElement.personList.online) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF08C008), Color(0xFF08C008)),
                                        start = Offset(0f, 0f),
                                        end = Offset(14.dp.value, 14.dp.value)
                                    )
                                )
                                .align(Alignment.Center)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.Gray, Color(0xFF808080)),
                                        start = Offset(0f, 0f),
                                        end = Offset(14.dp.value, 14.dp.value)
                                    )
                                )
                                .align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
        Column(Modifier.padding(horizontal = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = highlightSearchedText(
                        friendElement.personList.username["mixedcase"].toString(),
                        searchedValue
                    ),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (userData.blocked.contains(friendElement.personList.id)) "" else formattedTime,
                    fontWeight = FontWeight.Light,
                    fontSize = if (friendElement.read > 0) 13.sp else 12.sp,
                    color = if (friendElement.read > 0) Color(0xFF00A0E8) else MaterialTheme.colorScheme.secondary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (userData.blocked.contains(friendElement.personList.id)) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 10.dp)
                            .weight(1f),
                        text = "You blocked this user",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                } else {
                    val messageContent =
                        if (friendElement.lastMessage.text.isEmpty() && friendElement.lastMessage.images.isNotEmpty()) "Image" else friendElement.lastMessage.text
                    val senderPrefix =
                        if (friendElement.lastMessage.sender != userData.id) "" else "Me: "
                    if (messageContent.isNotEmpty()) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 10.dp),
                            text = senderPrefix,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            fontSize = 15.sp,
                            color = if (friendElement.read > 0 && !friendElement.lastMessage.read && friendElement.lastMessage.sender != userData.id || friendElement.markedAsUnread) Color(
                                0xFF00A0E8
                            ) else MaterialTheme.colorScheme.secondary,
                        )

                        if (friendElement.lastMessage.images.isNotEmpty()) {
                            Image(
                                imageVector = Icons.Default.Image,
                                colorFilter = ColorFilter.tint(
                                    if (friendElement.read > 0 && !friendElement.lastMessage.read && friendElement.lastMessage.sender != userData.id || friendElement.markedAsUnread) Color(
                                        0xFF00A0E8
                                    ) else MaterialTheme.colorScheme.secondary
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.CenterVertically),
                            )
                        }
                    }
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        text = highlightSearchedText(
                            messageContent,
                            searchedValue
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 15.sp,
                        color = if (friendElement.read > 0 && !friendElement.lastMessage.read && friendElement.lastMessage.sender != userData.id || friendElement.markedAsUnread) Color(
                            0xFF00A0E8
                        ) else MaterialTheme.colorScheme.secondary,
                    )
                }
                if (userData.muted.contains(friendElement.personList.id)) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        model = R.drawable.speaker_none_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    )
                }
                if (friendElement.pinChat) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(20.dp),
                        model = R.drawable.pin_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                    )
                }
                if (friendElement.read > 0 || friendElement.markedAsUnread) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Box(
                        modifier = Modifier
                            .size(if (friendElement.read > 99) 24.dp else if (friendElement.read > 9) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterVertically)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00A0E8), Color(0xFF00A0E8)),
                                )
                            )
                    ) {
                        Text(
                            text = if (friendElement.markedAsUnread) "" else if (friendElement.read < 100) friendElement.read.toString() else "99+",
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

