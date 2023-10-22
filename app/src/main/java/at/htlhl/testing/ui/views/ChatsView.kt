package at.htlhl.testing.ui.views

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.testing.R
import at.htlhl.testing.data.BottomSheetItems
import at.htlhl.testing.data.FirebaseChats
import at.htlhl.testing.data.FirebaseMessages
import at.htlhl.testing.data.FirebaseUsers
import at.htlhl.testing.data.InternalChatInstances
import at.htlhl.testing.navigation.Screens
import at.htlhl.testing.ui.components.BottomSheetContent
import at.htlhl.testing.ui.components.BottomSheetTopBar
import at.htlhl.testing.ui.components.EmptyChatContent
import at.htlhl.testing.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class Chats : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ChatsScreen(
        navController: NavController, sharedViewModel: SharedViewModel
    ) {
        sharedViewModel.bottomBarState.value = true
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showUserIconPrompt by remember { mutableStateOf(false) }
        var showClearChatPrompt by remember { mutableStateOf(false) }
        var largeUserIconUrl by remember { mutableStateOf("") }
        var largeUserIconName by remember { mutableStateOf("") }
        val friendListDataState = sharedViewModel.friendListData.collectAsState()
        val friendListData: List<FirebaseUsers> = friendListDataState.value
        val messageChatRoomDataState = sharedViewModel.chatData.collectAsState()
        val messageChatRoomData: List<FirebaseChats> = messageChatRoomDataState.value
        val updatedPersonList: List<InternalChatInstances> = friendListData.map { person ->
            val matchingChat = messageChatRoomData.find { chat ->
                chat.members.contains(person.id) && chat.tab == "chats"
            }
            val lastVisibleMessage = matchingChat?.messages?.lastOrNull { message ->
                sharedViewModel.auth.currentUser?.uid.toString() in message.visible
            }
            val updatedStatus = lastVisibleMessage ?: FirebaseMessages()
            if (matchingChat?.messages?.lastOrNull()?.sender != person.id && updatedStatus != FirebaseMessages()) {
                InternalChatInstances(personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    pinChat = matchingChat?.pinned?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                        ?: 0)
            } else {
                InternalChatInstances(personList = person,
                    timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                        ?: Timestamp.now(),
                    lastMessage = updatedStatus,
                    markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    pinChat = matchingChat?.pinned?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                    read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                        ?: 0)
            }
        }
        val finalPersonList =
            updatedPersonList.filter { person -> person.personList.statusFriend == "accepted" }
        val sortedPersonList =
            finalPersonList.sortedWith(compareByDescending<InternalChatInstances> { it.pinChat }.thenByDescending { it.timestampMessage })
        val completePersonList =
            if (sharedViewModel.searchtext.value != "") sortedPersonList.filter {
                it.personList.username["mixedcase"]?.contains(
                    sharedViewModel.searchtext.value, ignoreCase = true
                ) ?: false
            } else sortedPersonList
        val bottomSheetScaffoldState = remember {
            BottomSheetScaffoldState(
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                drawerState = DrawerState(DrawerValue.Closed),
                snackbarHostState = SnackbarHostState()

            )
        }
        Log.println(Log.INFO, "SortedPersonList", sortedPersonList.toString())
        val bottomSheetItems = listOf(
            BottomSheetItems(
                title = if (sharedViewModel.friend.value.markedAsUnread || sharedViewModel.friend.value.read > 0) "Mark as Read" else "Mark as Unread",
                icon = if (sharedViewModel.friend.value.markedAsUnread || sharedViewModel.friend.value.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com,
                tag = "unread"
            ),
            BottomSheetItems(
                title = "Clear Chat", icon = R.drawable.comment_delete_svgrepo_com, tag = "clear"
            ),
            BottomSheetItems(
                title = if (sharedViewModel.friend.value.personList.mutedFriend) "Unmute User" else "Mute User",
                icon = if (sharedViewModel.friend.value.personList.mutedFriend) R.drawable.speaker_none_svgrepo_com else R.drawable.speaker_svgrepo_com,
                tag = "mute"
            ),
            BottomSheetItems(
                title = if (sharedViewModel.friend.value.pinChat) "Unpin Chat" else "Pin Chat",
                icon = if (sharedViewModel.friend.value.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                tag = "pin"
            ),
        )

        if (showUserIconPrompt) {
            CustomUserDialog(imageUrl = largeUserIconUrl,
                userName = largeUserIconName,
                navController = navController,
                sharedViewModel = sharedViewModel,
                onDismiss = { showUserIconPrompt = false })
        }
        if (showClearChatPrompt) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    sharedViewModel.deleteMessagesForUser()
                }
                showClearChatPrompt = false
            })
        }
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetGesturesEnabled = true,
            drawerGesturesEnabled = false,
            sheetContent = {
                BottomSheetContent(
                    bottomSheetItems,
                    onItemClicked = { item ->
                        when (item.tag) {
                            "unread" -> {
                                if (sharedViewModel.friend.value.read > 0) {
                                    sharedViewModel.markMessagesAsRead(sharedViewModel.friend.value)
                                } else if (sharedViewModel.friend.value.markedAsUnread && sharedViewModel.friend.value.read == 0) {
                                    sharedViewModel.updateMarkAsReadStatus(true)
                                } else {
                                    sharedViewModel.updateMarkAsReadStatus(false)
                                }
                            }

                            "clear" -> {
                                showClearChatPrompt = true
                            }

                            "mute" -> {
                                if (sharedViewModel.friend.value.personList.mutedFriend) {
                                    sharedViewModel.updateMuteFriendStatus(true)
                                } else {
                                    sharedViewModel.updateMuteFriendStatus(false)
                                }
                            }

                            "pin" -> {
                                if (sharedViewModel.friend.value.pinChat) {
                                    sharedViewModel.updatePinChatStatus(true)
                                } else {
                                    sharedViewModel.updatePinChatStatus(false)
                                }
                            }
                        }
                        coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() }

                    },
                    friend = sharedViewModel.friend.value,
                )
            },
            sheetPeekHeight = 0.dp,
            topBar = {
                BottomSheetTopBar(
                    navController, bottomSheetScaffoldState, coroutineScope, sharedViewModel
                )
            },
        ) {
            if (completePersonList.isEmpty()) {
                EmptyChatContent(onClicked = {
                    navController.navigate(Screens.SearchViewScreen.route)
                })
            }
            Box(modifier = if (bottomSheetScaffoldState.bottomSheetState.isExpanded || bottomSheetScaffoldState.bottomSheetState.isAnimationRunning) {
                Modifier
                    .fillMaxSize()
                    .clickable { coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() } }
            } else {
                Modifier
            }) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
                    state = lazyListState
                ) {
                    items(completePersonList) { message ->
                        ChatItem(
                            person = message,
                            sharedViewModel = sharedViewModel,
                            navController = navController,
                            onUserIconClicked = { imageUrl, userName ->
                                sharedViewModel.friend.value = message
                                showUserIconPrompt = true
                                largeUserIconUrl = imageUrl
                                largeUserIconName = userName
                            },
                            onItemLongClicked = { person ->
                                sharedViewModel.friend.value = person
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.expand()
                                }
                            },
                            bottomSheetState = bottomSheetScaffoldState.bottomSheetState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClearChatDialog(onDismiss: (String) -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss.invoke("closed") },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(20.dp))
                .width(250.dp)
                .height(200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Clear Chat?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "All messages/images from both users will be deleted.",
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    top = 10.dp, bottom = 20.dp, start = 10.dp, end = 10.dp
                )
            )
            Divider(
                thickness = 0.3f.dp,
                color = Color.LightGray,
            )
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss.invoke("clear") }) {
                Text(
                    text = "Clear",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
            Divider(
                thickness = 0.3f.dp,
                color = Color.LightGray,
            )
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss.invoke("closed") }) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
        }
    }
}

@Composable
fun CustomUserDialog(
    imageUrl: String,
    userName: String,
    navController: NavController,
    sharedViewModel: SharedViewModel,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SubcomposeAsyncImage(model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(shape = RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator()
                    })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = {
                        navController.navigate(Screens.ChatViewScreen.route)
                    }) {
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


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatItem(
    person: InternalChatInstances,
    bottomSheetState: BottomSheetState,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onUserIconClicked: (String, String) -> Unit,
    onItemLongClicked: (InternalChatInstances) -> Unit
) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime: String = formatter.format(person.timestampMessage.toDate())
    if (person.read > 0) {
        sharedViewModel.updateMarkAsReadStatus(true)
    }
    Row(
        modifier = if (bottomSheetState.isAnimationRunning || bottomSheetState.isExpanded) {
            Modifier
                .fillMaxWidth()
                .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
                .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        } else {
            Modifier
                .combinedClickable(
                    onClick = {
                        sharedViewModel.friend.value = person
                        navController.navigate(Screens.ChatViewScreen.route)
                    },
                    onLongClick = {
                        onItemLongClicked.invoke(person)
                    },
                )
                .fillMaxWidth()
                .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
                .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
        }
    ) {
        val isOnline = person.personList.status
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            SubcomposeAsyncImage(contentDescription = null,
                model = person.personList.image,
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
                alignment = Alignment.Center,
                loading = {
                    CircularProgressIndicator()
                })
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
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .weight(1f),
                    text = if (person.lastMessage.sender != sharedViewModel.auth.currentUser?.uid.toString()) {
                        if (person.lastMessage.content.length > 24) "${
                            person.lastMessage.content.substring(
                                0, 24
                            )
                        }..." else person.lastMessage.content
                    } else {
                        if (person.lastMessage.content.length > 24) "Me: ${
                            person.lastMessage.content.substring(
                                0, 24
                            )
                        }..." else "Me: ${person.lastMessage.content}"
                    },
                    maxLines = 1,
                    fontSize = 15.sp,
                    color = if (person.read > 0 && !person.lastMessage.read && person.lastMessage.sender != sharedViewModel.auth.currentUser?.uid) Color(
                        0xFF00A0E8
                    ) else Color.LightGray,
                )
                if (person.personList.mutedFriend) {
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
                if (person.pinChat) {
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
                if (person.read > 0 || person.markedAsUnread) {
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
                            text = if (person.markedAsUnread) "" else if (person.read < 100) person.read.toString() else "99+",
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