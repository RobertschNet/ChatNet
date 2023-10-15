package at.htlhl.testing.views

import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily.Companion.Cursive
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import at.htlhl.testing.R
import at.htlhl.testing.data.BottomSheetItem
import at.htlhl.testing.data.Chat
import at.htlhl.testing.data.FetchedUsers
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.data.ShownUsers
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.Timestamp
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
        var showUserIconPrompt by remember { mutableStateOf(false) }
        var largeUserIconUrl by remember { mutableStateOf("") }
        var largeUserIconName by remember { mutableStateOf("") }
        val friendListDataState = sharedViewModel.friendListData.collectAsState()
        val friendListData: List<FetchedUsers> = friendListDataState.value
        val messageChatRoomDataState = sharedViewModel.chatData.collectAsState()
        val messageChatRoomData: List<Chat> = messageChatRoomDataState.value
        val updatedPersonList: List<ShownUsers> = friendListData.map { person ->
            val matchingChat = messageChatRoomData.find { chat ->
                chat.members.contains(person.id) && chat.tab == "chats"
            }
            val lastVisibleMessage = matchingChat?.messages?.lastOrNull { message ->
                sharedViewModel.auth.currentUser?.uid.toString() in message.visible
            }
            val updatedStatus = lastVisibleMessage?.content ?: ""
            if (matchingChat?.messages?.lastOrNull()?.sender != person.id && updatedStatus != "") {
                ShownUsers(
                    personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = "Me: $updatedStatus",
                    pinChat = matchingChat?.pinned?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                        ?: 0
                )
            } else {
                ShownUsers(
                    personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    pinChat = matchingChat?.pinned?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                        ?: 0
                )
            }
        }
        if (showUserIconPrompt) {
            CustomUserDialog(
                imageUrl = largeUserIconUrl,
                userName = largeUserIconName,
                onDismiss = { showUserIconPrompt = false }
            )
        }
        val finalPersonList =
            updatedPersonList.filter { person -> person.personList.statusFriend == "accepted" }
        val sortedPersonList =
            finalPersonList.sortedWith(compareByDescending<ShownUsers> { it.pinChat }.thenByDescending { it.timestampMessage })
        Log.println(Log.INFO, "SortedPersonList", sortedPersonList.toString())
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
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(data = sharedViewModel.friend.value.personList.image)
                                        .apply(block = fun ImageRequest.Builder.() {
                                            placeholder(R.drawable.user_circle_svgrepo_com)
                                        }).build()
                                ),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(40.dp),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            )
                            Text(
                                text = sharedViewModel.friend.value.personList.username["mixedcase"].toString(),
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .align(Alignment.CenterVertically)
                                    .weight(1f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.Black
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
                                            when (bottomSheetItems[it].title) {
                                                "Delete" -> {
                                                    sharedViewModel.deleteFriendFromFriendList()
                                                    sharedViewModel.deleteChatRoom()
                                                }

                                                "Pin Chat" -> {
                                                    Log.println(
                                                        Log.INFO,
                                                        "PinChat",
                                                        sharedViewModel.friend.value.toString()
                                                    )
                                                    if (sharedViewModel.friend.value.pinChat) {
                                                        sharedViewModel.updatePinChatStatus(true)
                                                    } else {
                                                        sharedViewModel.updatePinChatStatus(false)
                                                    }
                                                }

                                                "Mute Messages" -> {
                                                    if (sharedViewModel.friend.value.personList.mutedFriend) {
                                                        sharedViewModel.updateMuteFriendStatus(true)
                                                    } else {
                                                        sharedViewModel.updateMuteFriendStatus(false)
                                                    }
                                                }
                                            }
                                        },
                                ) {
                                    Icon(
                                        bottomSheetItems[it].icon,
                                        bottomSheetItems[it].title,
                                        tint = Color.Black,
                                        modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
                                    )
                                    Text(
                                        text = bottomSheetItems[it].title,
                                        color = Color.Black,
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
                            color = Color.White,
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
                                    navController.navigate(Screens.SearchViewScreen.route)
                                })
                        IconButton(
                            onClick = {
                                navController.navigate(Screens.InboxScreen.route)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 60.dp, top = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
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
                        bottomSheetState = test,
                        onUserIconClicked = { imageUrl, userName ->
                            showUserIconPrompt = true
                            largeUserIconUrl = imageUrl
                            largeUserIconName = userName
                        }

                    )
                }
            }
        }
    }
}

@Composable
fun CustomUserDialog(
    imageUrl: String,
    userName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .height(295.dp)
                .width(250.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .background(Color.Gray.copy(alpha = 0.4f))
            ) {
                Text(
                    text = userName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier
                        .zIndex(1f)
                        .padding(start = 5.dp, top = 5.dp, bottom = 5.dp),
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display the user's image
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = imageUrl)
                            .apply(block = fun ImageRequest.Builder.() {
                                placeholder(R.drawable.user_circle_svgrepo_com)
                            }).build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(shape = RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { /* Handle icon click */ }) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Message",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { /* Handle icon click */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = "Block",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { /* Handle icon click */ }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { /* Handle icon click */ }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatItem(
    person: ShownUsers,
    bottomSheetState: Boolean,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onUserIconClicked: (String, String) -> Unit,
    onItemClicked: () -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(person.timestampMessage.toDate())
    Row(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    if (bottomSheetState) {
                        onItemClicked()
                    } else {
                        sharedViewModel.friend.value = person
                        Log.println(Log.INFO, "Current", person.toString())
                        navController.navigate(Screens.ChatScreen.route)
                    }
                },
                onLongClick = {
                    sharedViewModel.friend.value = person
                    onItemClicked()
                },
            )
            .fillMaxWidth()
            .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
            .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        val isOnline = person.personList.status
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            Image(
                contentDescription = null,

                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = person.personList.image)
                        .apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.user_circle_svgrepo_com)
                        }).build()
                ),
                modifier = Modifier
                    .clip(CircleShape)

                    .size(50.dp)
                    .clickable {
                        onUserIconClicked.invoke(
                            person.personList.image,
                            person.personList.username["mixedcase"].toString()
                        )
                    },
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
        Column(Modifier.padding(horizontal = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = person.personList.username["mixedcase"].toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
                Text(
                    text = formattedTime,
                    fontWeight = FontWeight.Light,
                    fontSize = if (person.read > 0) 13.sp else 12.sp,
                    color = if (person.read > 0) Color(0xFF00A0E8) else Color.Black
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    text = if (person.lastMessage.length > 24) "${person.lastMessage.substring(
                        0,
                        24
                    )}..." else person.lastMessage,
                    maxLines = 1,
                    fontSize = 15.sp,
                    color = if (person.read > 0) Color(0xFF00A0E8) else Color.LightGray,
                )
                if (person.personList.mutedFriend) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = null,
                        tint = Color.LightGray,
                    )
                }
                if (person.pinChat) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(20.dp),
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = Color.LightGray,
                    )
                }
                if (person.read > 0) {
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Box(
                        modifier = Modifier
                            .size(if (person.read > 99) 24.dp else if (person.read > 9) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterVertically)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF00A0E8), Color(0xFF00A0E8)),
                                )
                            )
                    ) {
                        Text(
                            text = if (person.read < 100) person.read.toString() else "99+",
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