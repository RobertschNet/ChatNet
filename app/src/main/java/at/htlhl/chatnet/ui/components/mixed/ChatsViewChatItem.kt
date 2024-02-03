package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsViewChatItem(
    chatFriend: InternalChatInstance,
    chatUser: FirebaseUser,
    displayOnlineState: Boolean,
    sharedViewModel: SharedViewModel,
    onClick: (String) -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(chatFriend.timestampMessage.toDate())
    if (chatFriend.read > 0) {
        sharedViewModel.updateMarkAsReadStatus(true)
    }
    Row(
        modifier =
        Modifier
            .combinedClickable(
                onClick = {
                    onClick.invoke("navigate")
                },
                onLongClick = {
                    onClick.invoke("message")
                },
            )
            .fillMaxWidth()
            .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
            .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        val isOnline = chatFriend.personList.online
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            if (!chatFriend.personList.blocked.contains(sharedViewModel.auth.currentUser?.uid.toString())) {
                SubcomposeAsyncImage(
                    contentDescription = null,
                    model = chatFriend.personList.image,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp)
                        .shimmerEffect()
                        .clickable {
                            onClick.invoke("image")
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
                            onClick.invoke("image")
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
                                colors = if (!isSystemInDarkTheme()) listOf(
                                    Color.White, Color.White
                                ) else listOf(Color(0xF1161616), Color(0xF1161616)),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    if (isOnline) {
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
                    text = buildAnnotatedStringWithColorHighlights(
                        chatFriend.personList.username["mixedcase"].toString(),
                        sharedViewModel.searchValue.value
                    ),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 17.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
                Text(
                    text = if (chatUser.blocked.contains(chatFriend.personList.id)) "" else formattedTime,
                    fontWeight = FontWeight.Light,
                    fontSize = if (chatFriend.read > 0) 13.sp else 12.sp,
                    color = if (chatFriend.read > 0) Color(0xFF00A0E8) else Color.Black
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (chatUser.blocked.contains(chatFriend.personList.id)) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 10.dp)
                            .weight(1f),
                        text = "You blocked this user",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 15.sp,
                        color = Color.LightGray,
                    )
                } else {
                    val messageContent =
                        if (chatFriend.lastMessage.text.isEmpty() && chatFriend.lastMessage.images.isNotEmpty()) "Image" else chatFriend.lastMessage.text
                    val senderPrefix =
                        if (chatFriend.lastMessage.sender != sharedViewModel.auth.currentUser?.uid.toString()) "" else "Me: "
                    if (messageContent.isNotEmpty()) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 10.dp),
                            text = senderPrefix,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            fontSize = 15.sp,
                            color = if (chatFriend.read > 0 && !chatFriend.lastMessage.read && chatFriend.lastMessage.sender != sharedViewModel.auth.currentUser?.uid || chatFriend.markedAsUnread) Color(
                                0xFF00A0E8
                            ) else Color.LightGray,
                        )

                        if (chatFriend.lastMessage.images.isNotEmpty()) {
                            Image(
                                imageVector = Icons.Default.Image,
                                colorFilter = ColorFilter.tint(
                                    if (chatFriend.read > 0 && !chatFriend.lastMessage.read && chatFriend.lastMessage.sender != sharedViewModel.auth.currentUser?.uid || chatFriend.markedAsUnread) Color(
                                        0xFF00A0E8
                                    ) else Color.Gray
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
                        text = buildAnnotatedStringWithColorHighlights(
                            messageContent,
                            sharedViewModel.searchValue.value
                        ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontSize = 15.sp,
                        color = if (chatFriend.read > 0 && !chatFriend.lastMessage.read && chatFriend.lastMessage.sender != sharedViewModel.auth.currentUser?.uid || chatFriend.markedAsUnread) Color(
                            0xFF00A0E8
                        ) else Color.LightGray,
                    )
                }
                if (chatUser.muted.contains(chatFriend.personList.id)) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        model = R.drawable.speaker_none_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray),
                    )
                }
                if (chatFriend.pinChat) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(20.dp),
                        model = R.drawable.pin_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray),
                    )
                }
                if (chatFriend.read > 0 || chatFriend.markedAsUnread) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Box(
                        modifier = Modifier
                            .size(if (chatFriend.read > 99) 24.dp else if (chatFriend.read > 9) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterVertically)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00A0E8), Color(0xFF00A0E8)),
                                )
                            )
                    ) {
                        Text(
                            text = if (chatFriend.markedAsUnread) "" else if (chatFriend.read < 100) chatFriend.read.toString() else "99+",
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun buildAnnotatedStringWithColorHighlights(content: String, text: String): AnnotatedString {
    val lowercase = text.lowercase(Locale.getDefault())
    val occurrences = if (text.isNotEmpty()) {
        findAllOccurrences(content.lowercase(Locale.getDefault()), lowercase)
    } else {
        emptyList()
    }

    return buildAnnotatedString {
        var lastIndex = 0
        occurrences.forEach { ottoIndex ->
            append(content.substring(lastIndex, ottoIndex))
            if (text.isNotEmpty()) {
                withStyle(style = SpanStyle(background = Color.Yellow, color = Color.Black)) {
                    val ottoLength = text.length
                    append(content.substring(ottoIndex, ottoIndex + ottoLength))
                }
            }
            lastIndex = ottoIndex + text.length
        }
        if (lastIndex < content.length) {
            append(content.substring(lastIndex))
        }
    }
}