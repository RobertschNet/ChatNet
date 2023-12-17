package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.components.chats.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.components.mixed.ChatsViewBottomSheetContent
import at.htlhl.chatnet.ui.components.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.components.mixed.ClearChatDialog
import at.htlhl.chatnet.ui.components.mixed.TabsTopBar
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale


class DropIn : ViewModel() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        Log.println(Log.INFO, "Chats", "ChatsScreen")
        val context = LocalContext.current
        val lazyListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showUserIconPrompt by remember { mutableStateOf(false) }
        var showClearChatPrompt by remember { mutableStateOf(false) }
        val modelSheetState = remember { mutableStateOf(false) }
        val documentIdState = sharedViewModel.chatData.collectAsState()
        val documentationId: List<FirebaseChat> = documentIdState.value
        val friendIdState = sharedViewModel.personData.collectAsState(initial = emptyList())
        val friendListData: List<FirebaseUsers> = friendIdState.value
        val localChatUsers = sharedViewModel.localChatUserList.value.filter { localUser ->
            localUser.id != sharedViewModel.auth.currentUser?.uid
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
        val usersWithEmptyChatRooms = documentationId.filter { user ->
            user.messages.isEmpty()
        }
        val bottomSheetItems = listOf(
            BottomSheetItem(
                title = if (sharedViewModel.friend.value.markedAsUnread || sharedViewModel.friend.value.read > 0) "Mark as Read" else "Mark as Unread",
                icon = if (sharedViewModel.friend.value.markedAsUnread || sharedViewModel.friend.value.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com,
                tag = "unread"
            ),
            BottomSheetItem(
                title = "Clear Chat", icon = R.drawable.comment_delete_svgrepo_com, tag = "clear"
            ),
            BottomSheetItem(
                title = if (sharedViewModel.friend.value.personList.mutedFriend) "Unmute User" else "Mute User",
                icon = if (sharedViewModel.friend.value.personList.mutedFriend) R.drawable.speaker_none_svgrepo_com else R.drawable.speaker_svgrepo_com,
                tag = "mute"
            ),
            BottomSheetItem(
                title = if (sharedViewModel.friend.value.pinChat) "Unpin Chat" else "Pin Chat",
                icon = if (sharedViewModel.friend.value.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                tag = "pin"
            ),
        )
        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TabsTopBar(
                    tab = "DropIn",
                    availableUsers = listOf(FirebaseUsers()),
                    sharedViewModel = sharedViewModel,
                ) {
                    sharedViewModel.gpsState.value = !sharedViewModel.gpsState.value
                }
            },
            content = {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.tertiary),
                    state = lazyListState
                ) {
                    items(updatedLocalChatUsers) { message ->
                        ChatsViewChatItem(
                            chat = message,
                            displayOnlineState = true,
                            sharedViewModel = sharedViewModel,
                            navController = navController,
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
                            sharedViewModel.friend.value = message
                        }
                    }
                }
            }
        )
        if (showUserIconPrompt) {
            ShowBigUserImageDialog(
                sharedViewModel = sharedViewModel,
                userData = sharedViewModel.friend.value,
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
                            //TODO: Info
                        }
                    }
                    showUserIconPrompt = false
                })
        }
        if (showClearChatPrompt) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    sharedViewModel.deleteMessagesForUser()
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
                        },
                        friend = sharedViewModel.friend.value,
                    )
                }
            )
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

