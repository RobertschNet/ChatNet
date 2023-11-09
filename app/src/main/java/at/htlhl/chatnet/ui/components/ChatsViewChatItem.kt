package at.htlhl.chatnet.ui.components

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
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsViewChatItem(
    chat: InternalChatInstance,
    displayOnlineState: Boolean,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onClick: (String) -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(chat.timestampMessage.toDate())
    if (chat.read > 0) {
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
        val isOnline = chat.personList.status
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            SubcomposeAsyncImage(
                contentDescription = null,
                model = chat.personList.image,
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
                    when (isOnline) {
                        "online" -> {
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
                        }

                        "offline" -> {
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

                        "idle" -> {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFFFC107), Color(0xFFFFC107)),
                                            start = Offset(0f, 0f),
                                            end = Offset(14.dp.value, 14.dp.value)
                                        )
                                    )
                                    .align(Alignment.Center)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.2f.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .align(Alignment.TopStart)
                                )
                            }
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
                    text = chat.personList.username["mixedcase"].toString(),
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
                    text = formattedTime,
                    fontWeight = FontWeight.Light,
                    fontSize = if (chat.read > 0) 13.sp else 12.sp,
                    color = if (chat.read > 0) Color(0xFF00A0E8) else Color.Black
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val messageContent = if(chat.lastMessage.content.isEmpty()&&chat.lastMessage.image.isNotEmpty()) "Image" else chat.lastMessage.content
                val senderPrefix = if (chat.lastMessage.sender != sharedViewModel.auth.currentUser?.uid.toString()) "" else "Me:"
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 10.dp),
                    text = if (messageContent == "Image") senderPrefix else "$senderPrefix ",
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 15.sp,
                    color = if (chat.read > 0 && !chat.lastMessage.read && chat.lastMessage.sender != sharedViewModel.auth.currentUser?.uid) Color(0xFF00A0E8) else Color.LightGray,
                )
                if (messageContent == "Image") {
                    Image(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically),
                        colorFilter = ColorFilter.tint(Color.Gray)
                    )
                }
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    text = messageContent,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 15.sp,
                    color = if (chat.read > 0 && !chat.lastMessage.read && chat.lastMessage.sender != sharedViewModel.auth.currentUser?.uid) Color(
                        0xFF00A0E8
                    ) else Color.LightGray,
                )
                if (chat.personList.mutedFriend) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        model = R.drawable.speaker_none_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray),
                        loading = {
                            CircularProgressIndicator()
                        },
                    )
                }
                if (chat.pinChat) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(20.dp),
                        model = R.drawable.pin_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.Gray),
                        loading = {
                            CircularProgressIndicator()
                        },
                    )
                }
                if (chat.read > 0 || chat.markedAsUnread) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Box(
                        modifier = Modifier
                            .size(if (chat.read > 99) 24.dp else if (chat.read > 9) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterVertically)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00A0E8), Color(0xFF00A0E8)),
                                )
                            )
                    ) {
                        Text(
                            text = if (chat.markedAsUnread) "" else if (chat.read < 100) chat.read.toString() else "99+",
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