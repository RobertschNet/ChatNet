package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.LocationUserInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.components.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.components.dialogs.DisableDropInDialog
import at.htlhl.chatnet.ui.components.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.components.mixed.ChatsViewBottomSheetContent
import at.htlhl.chatnet.ui.components.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.components.mixed.TabsTopBar
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class DropInView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val context = LocalContext.current
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showUserIconPrompt by remember { mutableStateOf(false) }
        var showClearChatPrompt by remember { mutableStateOf(false) }
        val modelSheetState = remember { mutableStateOf(false) }
        var disableDropInDialog by remember {
            mutableStateOf(
                false
            )
        }
        val chatDataState = sharedViewModel.chatData.collectAsState()
        val chatData: List<FirebaseChat> = chatDataState.value
        val dropInDataState = sharedViewModel.completeDropInList.collectAsState()
        val dropInData: List<InternalChatInstance> = dropInDataState.value
        val friendDataState = sharedViewModel.friend.collectAsState()
        val userDataState = sharedViewModel.user.collectAsState()
        val userData: FirebaseUser = userDataState.value
        val friend: InternalChatInstance = friendDataState.value
        val localChatUsers = sharedViewModel.localChatUserList.value.filter { localUser ->
            localUser.id != sharedViewModel.auth.currentUser?.uid
        }
        val localChatUser= sharedViewModel.localChatUserList.value.find { localUser ->
            localUser.id == sharedViewModel.auth.currentUser?.uid
        }
        val updatedLocalChatUsers: List<InternalChatInstance> = localChatUsers.map { person ->
            val matchingChat = chatData.find { chat ->
                chat.members.contains(person.id)
            }
            InternalChatInstance(
                personList = FirebaseUser(
                    blocked = person.blocked,
                    email = "",
                    pinned = listOf(),
                    color = "",
                    connected = false,
                    mutedFriend = false,
                    statusFriend = "",
                    image = person.image,
                    username = person.username,
                    status = person.status,
                    id = person.id
                ),
                timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                    ?: Timestamp.now(),
                lastMessage = matchingChat?.messages?.lastOrNull() ?: InternalMessageInstance(),
                markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                pinChat = false,
                chatRoomID = matchingChat?.chatRoomID ?: "",
                read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                    ?: 0
            )
        }
        val bottomSheetItems = listOf(
            BottomSheetItem(
                title = if (friend.markedAsUnread || friend.read > 0) "Mark as Read" else "Mark as Unread",
                icon = if (friend.markedAsUnread || friend.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com,
                tag = "unread"
            ),
            BottomSheetItem(
                title = "Clear Chat", icon = R.drawable.comment_delete_svgrepo_com, tag = "clear"
            ),
            BottomSheetItem(
                title = if (friend.personList.mutedFriend) "Unmute User" else "Mute User",
                icon = if (friend.personList.mutedFriend) R.drawable.speaker_none_svgrepo_com else R.drawable.speaker_svgrepo_com,
                tag = "mute"
            ),
            BottomSheetItem(
                title = if (friend.pinChat) "Unpin Chat" else "Pin Chat",
                icon = if (friend.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                tag = "pin"
            ),
        )
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TabsTopBar(
                    tab = "DropIn",
                    availableUsers = listOf(FirebaseUser()),
                    sharedViewModel = sharedViewModel,
                ) {
                   disableDropInDialog = true
                }
            },
            content = {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = "Users in your area",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            item {
                                UserListItem(user = userData,localChatUser) {
                                    //TODO: Navigate to your public profile
                                }
                            }
                            items(localChatUsers) { chat ->
                                GPSChatList(
                                    chat = chat
                                ) { locationUserInstance ->
                                    updatedLocalChatUsers.find { it.personList.id == locationUserInstance.id }
                                        ?.let {
                                            if (it.chatRoomID.isEmpty()) {
                                                sharedViewModel.saveChatRoom(
                                                    person = it.personList.id,
                                                    tab = "dropIn"
                                                ) { newChat ->
                                                    sharedViewModel.updateFriend(
                                                        InternalChatInstance(
                                                            personList = it.personList,
                                                            timestampMessage = Timestamp.now(),
                                                            lastMessage = InternalMessageInstance(),
                                                            markedAsUnread = true,
                                                            pinChat = false,
                                                            chatRoomID = newChat,
                                                            read = 0
                                                        )
                                                    ) {
                                                        navController.navigate(Screens.ChatViewScreen.route)
                                                    }
                                                }
                                            } else {
                                                sharedViewModel.updateFriend(it)
                                                navController.navigate(Screens.ChatViewScreen.route)
                                            }
                                        }

                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            text = "Users you chatted with",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.tertiary),
                        state = lazyListState
                    ) {
                        items(dropInData) { chat ->
                            ChatsViewChatItem(
                                chatFriend = chat,
                                chatUser = userData,
                                displayOnlineState = true,
                                sharedViewModel = sharedViewModel,
                            ) { context ->
                                when (context) {
                                    "image" -> {
                                        showUserIconPrompt = true
                                    }

                                    "message" -> {
                                        modelSheetState.value = true
                                    }

                                    "navigate" -> {
                                        navController.navigate(Screens.ChatViewScreen.route)
                                    }
                                }
                                sharedViewModel.updateFriend(chat)
                            }
                        }
                    }
                }
                if(disableDropInDialog){
                    DisableDropInDialog(
                        dropInOn = !sharedViewModel.gpsState.value,
                        onClose = { action ->
                        if (action == "changed") {
                            sharedViewModel.gpsState.value = !sharedViewModel.gpsState.value
                        }
                        disableDropInDialog = false
                    })
                }
            }
        )
        if (showUserIconPrompt) {
            ShowBigUserImageDialog(
                sharedViewModel = sharedViewModel,
                userData = friend,
                onDismiss = { action ->
                    when (action) {
                        "message" -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        "block" -> {
                            sharedViewModel.updateBlockedUserList(
                                sharedViewModel.user.value.blocked.contains(
                                    sharedViewModel.friend.value.personList.id
                                )
                            )
                        }

                        "image" -> {
                            coroutineScope.launch {
                                SaveImageTask(WeakReference(context)).saveImage(sharedViewModel.friend.value.personList.image)
                            }
                        }

                        "info" -> {
                            navController.navigate(Screens.ProfileInfoScreen.route)
                        }
                    }
                    showUserIconPrompt = false
                })
        }
        if (showClearChatPrompt) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    sharedViewModel.deleteMessagesForUser(friend.chatRoomID)
                }
                showClearChatPrompt = false
            })
        }
        if (modelSheetState.value) {
            ModalBottomSheet(
                windowInsets = WindowInsets(0, 0, 0, 0),
                onDismissRequest = {
                    modelSheetState.value = false
                }, dragHandle = null, content = {
                    ChatsViewBottomSheetContent(
                        bottomSheetItems,
                        onItemClicked = { item ->
                            modelSheetState.value = false
                            when (item.tag) {
                                "unread" -> {
                                    if (friend.read > 0) {
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
                                    if (friend.personList.mutedFriend) {
                                        sharedViewModel.updateMuteFriendStatus(true)
                                    } else {
                                        sharedViewModel.updateMuteFriendStatus(false)
                                    }
                                }

                                "pin" -> {
                                    if (friend.pinChat) {
                                        sharedViewModel.updatePinChatStatus(true)
                                    } else {
                                        sharedViewModel.updatePinChatStatus(false)
                                    }
                                }
                            }
                        },
                        friend = friend,
                    )
                }
            )
        }
    }

    @Composable
    fun GPSChatList(chat: LocationUserInstance, onItemClicked: (LocationUserInstance) -> Unit) {
        val isOnline = chat.status
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(80.dp)
            ) {
                SubcomposeAsyncImage(
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clickable { onItemClicked.invoke(chat) }
                        .align(Alignment.Center)
                        .size(80.dp)
                        .clip(CircleShape),
                    model = chat.image,
                    contentDescription = null
                )
                Box(
                    modifier = Modifier
                        .offset((-5).dp)
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
            Text(
                text = chat.username["mixedcase"].toString(),
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp),
                fontSize = 14.sp,
                maxLines = 1,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = chat.location,
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light
            )

        }
    }

    @Composable
    fun UserListItem(user: FirebaseUser,localChatUser: LocationUserInstance?,onClick: () -> Unit){
        val isOnline = user.status
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.width(90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(80.dp)
            ) {
                SubcomposeAsyncImage(
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable { onClick.invoke() },
                    model = user.image,
                    contentDescription = null
                )
                Box(
                    modifier = Modifier
                        .size(16.5f.dp)
                        .offset((-5).dp)
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
            Text(
                text = "You",
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp),
                fontSize = 12.sp,
                maxLines = 1,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Text(
                text = localChatUser?.location ?: "Unknown",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light
            )
        }
    }
}