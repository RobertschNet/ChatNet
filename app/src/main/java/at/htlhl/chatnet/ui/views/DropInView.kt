package at.htlhl.chatnet.ui.views

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GpsNotFixed
import androidx.compose.material.icons.outlined.GpsOff
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontFamily.Companion.Cursive
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.ChatsViewChatItem
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class DropIn : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val lazyListState = rememberLazyListState()
        var test by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val documentIdState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val documentationId: List<FirebaseChat> = documentIdState.value
        val friendIdState = sharedViewModel.personData.collectAsState(initial = emptyList())
        val friendListData: List<FirebaseUsers> = friendIdState.value
        val localChatUsers = sharedViewModel.localChatUserList.value.filter { localUser ->
            localUser.id != sharedViewModel.auth.currentUser?.uid
        }
        val updatedPersonList: List<InternalChatInstance> = friendListData.map { person ->
            val matchingChat = documentationId.find { chat ->
                chat.members.contains(person.id) && chat.tab == "dropIn"
            }
            val lastVisibleMessage = matchingChat?.messages?.lastOrNull { message ->
                sharedViewModel.auth.currentUser?.uid.toString() in message.visible
            }
            val updatedStatus = lastVisibleMessage ?: InternalMessageInstance()
            if (matchingChat?.messages?.lastOrNull()?.sender != person.id && updatedStatus != InternalMessageInstance()) {
                InternalChatInstance(
                    personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    pinChat = person.blocked.contains(matchingChat?.chatRoomID),
                    chatRoomID = matchingChat?.chatRoomID ?: "",
                    read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                        ?: 0
                )
            } else {
                InternalChatInstance(
                    personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    pinChat = person.blocked.contains(matchingChat?.chatRoomID),
                    chatRoomID = matchingChat?.chatRoomID ?: "",
                    markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                        ?: 0
                )
            }
        }

        val updatedLocalChatUsers: List<InternalChatInstance> = localChatUsers.map { person ->
            val matchingChat = documentationId.find { chat ->
                chat.members.contains(person.id)
            }
            InternalChatInstance(
                personList = person,
                timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                    ?: Timestamp.now(),
                lastMessage = matchingChat?.messages?.lastOrNull() ?: InternalMessageInstance(),
                markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                pinChat = person.blocked.contains(matchingChat?.chatRoomID),
                chatRoomID = matchingChat?.chatRoomID ?: "",
                read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                    ?: 0
            )
        }
        val sortedPersonList = updatedPersonList.sortedByDescending { it.timestampMessage }
        val usersWithEmptyChatRooms = documentationId.filter { user ->
            user.messages.isEmpty()
        }
        val bottomSheetItems = listOf(
            BottomSheetItem(title = "Delete", icon = 2, tag = "delete"),
            BottomSheetItem(title = "Mute Messages", icon = 2, tag = "mute"),
            BottomSheetItem(title = "Pin Chat", icon = 2, tag = "pin"),
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
                            SubcomposeAsyncImage(
                                contentDescription = null,
                                model = sharedViewModel.friend.value.personList.image,
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
                                            if (bottomSheetItems[it].title == "Delete") {
                                                sharedViewModel.deleteFriendFromFriendList()
                                                sharedViewModel.deleteChatRoom()
                                            }
                                        },
                                ) {
                                    SubcomposeAsyncImage(
                                        model = bottomSheetItems[it].icon,
                                        bottomSheetItems[it].title,
                                        modifier = Modifier.padding(top = 14.dp, bottom = 14.dp),
                                        loading = {
                                            CircularProgressIndicator()
                                        }
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
                    backgroundColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .height(70.dp)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                alpha = if (
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
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 36.sp,
                            fontFamily = Cursive
                        )
                        IconButton(
                            onClick = {
                                sharedViewModel.gpsState.value = !sharedViewModel.gpsState.value
                                usersWithEmptyChatRooms.forEach { user ->
                                    sharedViewModel.deleteChatRoomForLocal(user.chatRoomID)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp, top = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (!sharedViewModel.gpsState.value) Icons.Outlined.GpsNotFixed else Icons.Outlined.GpsOff,
                                contentDescription = "GPS",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            },
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(if (isSystemInDarkTheme()) Color.Black else Color.White)
                    .graphicsLayer(
                        alpha = if (
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
                if (!sharedViewModel.gpsState.value) {
                    items(updatedLocalChatUsers) { message ->
                        ChatItemForDropIn(
                            person = message,
                            documentationId = documentationId,
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
                items(sortedPersonList) { message ->
                    ChatsViewChatItem(
                        chat = message,
                        displayOnlineState = true,
                        sharedViewModel = sharedViewModel,
                        navController = navController,
                    ) { context ->
                        if (context == "image") {
                            //TODO: Show Big Image
                        } else if (context == "message") {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        }
                        sharedViewModel.friend.value = message

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatItemForDropIn(
    person: InternalChatInstance,
    bottomSheetState: Boolean,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    documentationId: List<FirebaseChat>,
    onItemClicked: () -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(person.timestampMessage.toDate())
    Divider(thickness = 0.25f.dp, color = Color.LightGray)
    Row(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    if (bottomSheetState) {
                        onItemClicked()
                    } else {
                        sharedViewModel.friend.value = person
                        Log.println(Log.INFO, "Current", person.toString())
                        val filteredChats = documentationId.filter { chat ->
                            chat.members.contains(person.personList.id) && chat.members
                                .contains(sharedViewModel.auth.currentUser?.uid)
                        }
                        if (filteredChats.isEmpty()) {
                            sharedViewModel.saveChatRoom(
                                person = person.personList.id,
                                tab = "dropIn"
                            )
                        }
                        navController.navigate(Screens.ChatViewScreen.route)
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
            SubcomposeAsyncImage(
                contentDescription = null,
                model = person.personList.image,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(50.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                loading = {
                    CircularProgressIndicator()
                }
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
                    fontSize = 12.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
            }
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = person.personList.statusFriend,
                maxLines = 1,
                fontSize = 15.sp,
                color = if (person.lastMessage.content >= "User is 400 meters away") Color.Yellow else if (person.lastMessage.content >= "User is 450 meters away") Color.Red else Color.LightGray
            )
        }
    }
    Divider(thickness = 0.25f.dp, color = Color.LightGray)
}

