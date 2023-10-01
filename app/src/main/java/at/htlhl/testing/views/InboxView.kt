package at.htlhl.testing.views

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Locale

class InboxView {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun Inbox(sharedViewModel: SharedViewModel) {
        val friendListDataState = sharedViewModel.friendListPending.collectAsState()
        val friendListData: List<PersonList> = friendListDataState.value
        Log.println(Log.INFO, "InboxView", "friendListData: $friendListData")

        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
        ) {
            items(friendListData) { message ->
                ChatItem(
                    person = message,
                    sharedViewModel = sharedViewModel,
                )
            }
        }

    }

    @OptIn(ExperimentalFoundationApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ChatItem(
        person: PersonList,
        sharedViewModel: SharedViewModel,
    ) {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime: String = formatter.format(person.timestamp.toDate())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
                .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            val isOnline = person.online
            Box(
                modifier = Modifier.size(50.dp)
            ) {
                Image(
                    contentDescription = null,
                    painter = rememberAsyncImagePainter(person.image),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                Box(
                    modifier = Modifier
                        .size(16.5f.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (!isSystemInDarkTheme()) listOf(
                                    Color.White,
                                    Color.White
                                ) else listOf(Color(0xF1161616), Color(0xF1161616)),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    when (isOnline) {
                        "Online" -> {
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

                        "Offline" -> {
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

                        "Idle" -> {
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
            Column(Modifier.padding(horizontal = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = person.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                    Text(
                        text = formattedTime,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.Add,
                    tint = Color.Green,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        sharedViewModel.saveFriendForFriend(
                            person = person.userID,
                            local = false,
                            status = "accepted"
                        )
                        sharedViewModel.saveFriendForUser(
                            person = person.userID,
                            local = false,
                            status = "accepted"
                        )
                    })
            }

        }
    }
}