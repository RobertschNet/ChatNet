package at.htlhl.testing.views


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily.Companion.Cursive
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.testing.data.BottomSheetItem
import at.htlhl.testing.data.Chat
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class Chats {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ChatsScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        sharedViewModel.bottomBarState.value = true
        val lazyListState = rememberLazyListState()
        var test by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val friendListDataState = sharedViewModel.friendListData.collectAsState()
        val friendListData: List<PersonList> = friendListDataState.value
        val messageChatRoomDataState = sharedViewModel.chatData.collectAsState()
        val messageChatRoomData: List<Chat> = messageChatRoomDataState.value
        println("Friends: $friendListData")
        println("Chats: $messageChatRoomData")
        val updatedPersonList = friendListData.map { person ->
            val matchingChat = messageChatRoomData.find { chat ->
                chat.participants.contains(person.userID)
            }
            val updatedStatus = matchingChat?.messages?.lastOrNull()?.content ?: person.status
            val updatedTimestamp =
                matchingChat?.messages?.lastOrNull()?.timestamp ?: person.timestamp

            if (matchingChat?.messages?.lastOrNull()?.sender != person.userID && updatedStatus != "") {
                person.copy(status = "Me: $updatedStatus", timestamp = updatedTimestamp)
            } else {
                person.copy(status = updatedStatus, timestamp = updatedTimestamp)
            }
        }
        val sortedPersonList = updatedPersonList.sortedByDescending { it.timestamp }
        val bottomSheetItems = listOf(
            BottomSheetItem(title = "Delete", icon = Icons.Default.Delete),
            BottomSheetItem(title = "Mute Messages", icon = Icons.Default.VolumeMute),
            BottomSheetItem(title = "Pin Chat", icon = Icons.Default.PushPin),
        )
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
        )
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetGesturesEnabled = true,
            drawerGesturesEnabled = false,
            sheetContent = {
                Column(
                    content = {
                        Canvas(
                            modifier = Modifier
                                .width(50.dp)
                                .height(10.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            drawRoundRect(
                                color = Color.LightGray,
                                size = size.copy(height = 2.dp.toPx()),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                style = Stroke(2.dp.toPx())
                            )
                        }
                        Spacer(modifier = Modifier.padding(6.dp))
                        Row {
                            Image(
                                contentDescription = null,
                                painter = rememberAsyncImagePainter(sharedViewModel.user.value.image),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(40.dp),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )
                            Text(
                                text = sharedViewModel.user.value.name,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterVertically)
                                    .weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.padding(6.dp))
                        Divider(
                            thickness = 0.25f.dp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        LazyColumn(userScrollEnabled = false) {
                            items(bottomSheetItems.size, itemContent = {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (bottomSheetItems[it].title == "Delete") {
                                                sharedViewModel.deleteFriendFromFriendList()
                                                sharedViewModel.deleteChatRoom()
                                            }
                                        },
                                ) {
                                    Icon(
                                        bottomSheetItems[it].icon,
                                        bottomSheetItems[it].title,
                                        tint = Color.White,
                                        modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
                                    )
                                    Text(
                                        text = bottomSheetItems[it].title,
                                        color = Color.White,
                                        modifier = Modifier.padding(
                                            start = 12.dp,
                                            top = 14.dp,
                                            bottom = 14.dp
                                        ),
                                    )
                                }

                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(
                            color = Color(0xFF252525),
                        )
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
            },
            sheetPeekHeight = 0.dp,
            topBar = {
                TopAppBar(
                    backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                alpha = if (bottomSheetScaffoldState.bottomSheetState.isAnimationRunning ||
                                    bottomSheetScaffoldState.bottomSheetState.isExpanded
                                ) 0.5f else 1f
                            )
                    ) {
                        Text(
                            text = "ChatNet",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 20.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            fontFamily = Cursive
                        )
                        Icon(imageVector = Icons.Outlined.PersonAddAlt1,
                            contentDescription = "AddFriend",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 20.dp, top = 5.dp)
                                .size(30.dp)
                                .clickable {
                                    navController.navigate(Screens.SearchViewScreen.Route)
                                })
                        IconButton(
                            onClick = {
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 60.dp, top = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                Divider(
                    thickness = 1.dp,
                    color = if (isSystemInDarkTheme()) Color.DarkGray else Color.Transparent
                )
            },
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
                    .graphicsLayer(
                        alpha = if (bottomSheetScaffoldState.bottomSheetState.isAnimationRunning ||
                            bottomSheetScaffoldState.bottomSheetState.isExpanded
                        ) 0.5f else 1f
                    )
                    .clickable(enabled = test) {
                        if (test) {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                                test = !test
                            }
                        }
                    },
                state = lazyListState
            ) {
                items(sortedPersonList) { message ->
                    ChatItem(
                        person = message,
                        sharedViewModel = sharedViewModel,
                        navController = navController,
                        onItemClicked = {
                            test = !test
                            coroutineScope.launch {
                                if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                } else {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                            }
                        },
                        bottomSheetState = test
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatItem(
    person: PersonList,
    bottomSheetState: Boolean,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onItemClicked: () -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(person.timestamp.toDate())
    Divider(thickness = 0.25f.dp, color = Color.LightGray)

    Row(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    if (bottomSheetState) {
                        onItemClicked()
                    } else {
                        sharedViewModel.user.value = person
                        if (person.local) {
                            sharedViewModel.saveChatRoom(person.userID)
                        }
                        navController.navigate(Screens.ChatScreen.Route)
                    }

                },
                onLongClick = {
                    sharedViewModel.user.value = person
                    onItemClicked()
                },
            )
            .fillMaxWidth()
            .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
            .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
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
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = person.status,
                maxLines = 1,
                fontSize = 15.sp,
                color = Color.LightGray
            )
        }
    }
    Divider(thickness = 0.25f.dp, color = Color.LightGray)
}