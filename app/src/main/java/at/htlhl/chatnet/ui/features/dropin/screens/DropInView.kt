package at.htlhl.chatnet.ui.features.dropin.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.BigUserImageDismissState.BLOCK
import at.htlhl.chatnet.data.BigUserImageDismissState.IMAGE
import at.htlhl.chatnet.data.BigUserImageDismissState.INFO
import at.htlhl.chatnet.data.BigUserImageDismissState.MESSAGE
import at.htlhl.chatnet.data.BottomSheetTagState
import at.htlhl.chatnet.data.ChatsChatItemClickState
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.data.LocationUserInstance
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.SaveImageTask
import at.htlhl.chatnet.ui.features.dialogs.ClearChatDialog
import at.htlhl.chatnet.ui.features.dialogs.DisableDropInDialog
import at.htlhl.chatnet.ui.features.dialogs.ShowBigUserImageDialog
import at.htlhl.chatnet.ui.features.dropin.viewmodels.DropInViewModel
import at.htlhl.chatnet.ui.features.mixed.ChatsViewChatItem
import at.htlhl.chatnet.ui.features.mixed.TabsBottomSheetContent
import at.htlhl.chatnet.ui.features.mixed.TabsTopBar
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.util.firebase.deleteAllChatMessages
import at.htlhl.chatnet.util.firebase.markMessagesAsRead
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.util.firebase.updateMarkAsUnreadStatus
import at.htlhl.chatnet.util.firebase.updateMuteFriendStatus
import at.htlhl.chatnet.util.firebase.updatePinChatStatus
import at.htlhl.chatnet.util.generateBottomSheetItems
import at.htlhl.chatnet.util.highlightSearchedText
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class DropInView {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val dropInViewModel = viewModel<DropInViewModel>()
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = MaterialTheme.colorScheme.background, darkIcons = !isSystemInDarkTheme()
        )
        val context = LocalContext.current
        val dropInState by sharedViewModel.dropInState
        val searchedValue by sharedViewModel.searchValue
        val lazyColumnState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var showBigUserImageDialog by remember { mutableStateOf(false) }
        var showClearChatDialog by remember { mutableStateOf(false) }
        var showBlockUserDialog by remember { mutableStateOf(false) }
        var modelSheetState by remember { mutableStateOf(false) }
        var disableDropInDialog by remember { mutableStateOf(false) }

        val completeDopInChatListState by sharedViewModel.completeDropInList.collectAsState()
        val chatDataState by sharedViewModel.chatData.collectAsState()
        val userDataState by sharedViewModel.user.collectAsState()
        val friendDataState by sharedViewModel.friend.collectAsState()


        val chatData: List<FirebaseChat> = chatDataState
        val completeDopInChatList: List<InternalChatInstance> = completeDopInChatListState
        val userData: FirebaseUser = userDataState
        val friendData: InternalChatInstance = friendDataState

        val dropInUsersNearbyList = sharedViewModel.localChatUserList.value.filter { nearbyUsers ->
            nearbyUsers.id != userData.id
        }
        val filteredDropInChatList = dropInViewModel.filterDropInPersonsList(
            searchedValue = searchedValue, completeDopInChatList = completeDopInChatList
        )
        val filteredNearbyUsersList = dropInViewModel.filterDropInNearbyUsersList(
            searchedValue = searchedValue, dropInUsersNearbyList = dropInUsersNearbyList
        )



        val localChatUser = sharedViewModel.localChatUserList.value.find { localUser ->
            localUser.id == sharedViewModel.auth.currentUser?.uid
        }
        val userInSearchValue = java.lang.String("You").contains(
            sharedViewModel.searchValue.value, ignoreCase = true
        ) || localChatUser!!.location.contains(sharedViewModel.searchValue.value, ignoreCase = true)

        val updatedLocalChatUsers: List<InternalChatInstance> = dropInUsersNearbyList.map { person ->
            val matchingChat = chatData.find { chat ->
                chat.members.contains(person.id)
            }
            InternalChatInstance(personList = FirebaseUser(
                blocked = person.blocked,
                email = "",
                pinned = listOf(),
                color = "",
                connected = false,
                muted = person.muted,
                statusFriend = PersonType.EMPTY_PERSON,
                image = person.image,
                username = person.username,
                online = person.online,
                id = person.id,
                tags = listOf()
            ),
                timestampMessage = matchingChat?.messages?.lastOrNull()?.timestamp
                    ?: Timestamp.now(),
                lastMessage = matchingChat?.messages?.lastOrNull() ?: InternalMessageInstance(),
                markedAsUnread = matchingChat?.unread?.contains(sharedViewModel.auth.currentUser?.uid.toString()) == true,
                pinChat = false,
                chatRoomID = matchingChat?.chatRoomID ?: "",
                read = matchingChat?.messages?.count { it.sender != sharedViewModel.auth.currentUser?.uid.toString() && !it.read }
                    ?: 0)
        }
        val bottomSheetItems = generateBottomSheetItems(
            isChatMate = false, friendData = friendData, userData = userData
        )
        Scaffold(backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(tab = CurrentTab.DROPIN, dropInState = dropInState, onActionClicked = {
                    disableDropInDialog = true
                }, onUpdateSearchValue = {
                    sharedViewModel.searchValue.value = it
                })
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
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
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            item {
                                if (userInSearchValue || sharedViewModel.searchValue.value.isEmpty()) {
                                    UserListItem(
                                        user = userData,
                                        localChatUser = localChatUser,
                                        sharedViewModel = sharedViewModel,
                                        onClick = {
                                            sharedViewModel.updatePublicUser(newFriend = userData, onComplete = {
                                                navController.navigate(Screens.ProfilePictureView.route)
                                            })
                                        }
                                    )
                                }
                            }
                            items(dropInUsersNearbyList) { chat ->
                                GPSChatList(
                                    chat = chat, sharedViewModel = sharedViewModel
                                ) { locationUserInstance ->
                                    updatedLocalChatUsers.find { it.personList.id == locationUserInstance.id }
                                        ?.let {
                                            if (it.chatRoomID.isEmpty()) {
                                                sharedViewModel.saveChatRoom(
                                                    person = it.personList.id, tab = "dropin"
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
                        })
                    if (completeDopInChatList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(40.dp))
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
                    }
                    LazyColumn(
                        Modifier.fillMaxSize(), state = lazyColumnState
                    ) {
                        items(filteredDropInChatList) { chat ->
                            ChatsViewChatItem(
                                friendElement = chat,
                                userData = userData,
                                displayOnlineState = true,
                                searchedValue = searchedValue,
                            ) { context ->
                                sharedViewModel.updateFriend(chat) {
                                    when (context) {
                                        ChatsChatItemClickState.IMAGE -> {
                                            showBigUserImageDialog = true
                                        }

                                        ChatsChatItemClickState.CONTEXT_MENU -> {
                                            modelSheetState = true
                                        }

                                        ChatsChatItemClickState.MESSAGE -> {
                                            navController.navigate(Screens.ChatViewScreen.route)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (disableDropInDialog) {
                    DisableDropInDialog(
                        dropInOn = !sharedViewModel.dropInState.value,
                        onClose = { action ->
                            if (action == "changed") {
                                sharedViewModel.dropInState.value =
                                    !sharedViewModel.dropInState.value
                            }
                            disableDropInDialog = false
                        })
                }
            })
        if (showBigUserImageDialog) {
            ShowBigUserImageDialog(userData = userData,
                friendData = friendData,
                onDismiss = { action ->
                    when (action) {
                        INFO -> {
                            navController.navigate(Screens.ProfileInfoScreen.route)
                        }

                        BLOCK -> {
                            updateBlockedUserList(
                                userData = userData,
                                friendData = friendData.personList,
                                isAlreadyBlocked = userData.blocked.contains(
                                    friendData.personList.id
                                )
                            )
                        }

                        MESSAGE -> {
                            navController.navigate(Screens.ChatViewScreen.route)
                        }

                        IMAGE -> {
                            coroutineScope.launch {
                                SaveImageTask(WeakReference(context)).saveImage(sharedViewModel.friend.value.personList.image)
                            }
                        }

                        else -> {
                        }

                    }
                    showBigUserImageDialog = false
                })
        }
        if (showClearChatDialog) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    deleteAllChatMessages(
                        userData = userData, friendData = friendData
                    )
                }
                showClearChatDialog = false
            })
        }
        if (modelSheetState) {
            ModalBottomSheet(windowInsets = WindowInsets(0, 0, 0, 0), onDismissRequest = {
                modelSheetState = false
            }, dragHandle = null, content = {
                TabsBottomSheetContent(
                    friendData = friendData,
                    bottomSheetItems = bottomSheetItems,
                    onItemClicked = { item ->
                        modelSheetState = false
                        when (item) {
                            BottomSheetTagState.UNREAD -> {
                                if (friendData.read > 0) {
                                    markMessagesAsRead(userData = userData, friendData = friendData)

                                } else if (sharedViewModel.friend.value.markedAsUnread && sharedViewModel.friend.value.read == 0) {
                                    updateMarkAsUnreadStatus(
                                        userData = userData, friendData = friendData,
                                        isAlreadyUnread = true
                                    )

                                } else {
                                    updateMarkAsUnreadStatus(
                                        userData = userData, friendData = friendData,
                                        isAlreadyUnread = false
                                    )

                                }
                            }

                            BottomSheetTagState.CLEAR -> {
                                showClearChatDialog = true
                            }

                            BottomSheetTagState.MUTE -> {
                                if (userData.muted.contains(friendData.personList.id)) {
                                    updateMuteFriendStatus(
                                        userData = userData,
                                        friendData = friendData.personList,
                                        isAlreadyMuted = true
                                    )
                                } else {
                                    updateMuteFriendStatus(
                                        userData = userData,
                                        friendData = friendData.personList,
                                        isAlreadyMuted = false
                                    )
                                }
                            }

                            BottomSheetTagState.PIN -> {
                                if (friendData.pinChat) {
                                    updatePinChatStatus(
                                        userData = userData,
                                        friend = friendData,
                                        isAlreadyPinned = true
                                    )
                                } else {
                                    updatePinChatStatus(
                                        userData = userData,
                                        friend = friendData,
                                        isAlreadyPinned = false
                                    )
                                }
                            }

                            else -> {}
                        }
                    },
                )
            })
        }
    }

    @Composable
    fun GPSChatList(
        chat: LocationUserInstance,
        sharedViewModel: SharedViewModel,
        onItemClicked: (LocationUserInstance) -> Unit
    ) {
        val isOnline = chat.online
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.width(80.dp)
            ) {
                SubcomposeAsyncImage(contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clickable { onItemClicked.invoke(chat) }
                        .align(Alignment.Center)
                        .size(80.dp)
                        .clip(CircleShape)
                        .shimmerEffect(),
                    model = chat.image,
                    contentDescription = null)
                Box(
                    modifier = Modifier
                        .offset((-5).dp)
                        .size(16.5f.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background
                                ), start = Offset(0f, 0f), end = Offset(14.dp.value, 14.dp.value)
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
            Text(
                text = highlightSearchedText(
                    chat.username["mixedcase"].toString(), sharedViewModel.searchValue.value
                ),
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp),
                fontSize = 12.sp,
                maxLines = 1,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = highlightSearchedText(
                    chat.location, sharedViewModel.searchValue.value
                ),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light
            )
        }
    }

    @Composable
    fun UserListItem(
        user: FirebaseUser,
        localChatUser: LocationUserInstance?,
        sharedViewModel: SharedViewModel,
        onClick: () -> Unit
    ) {
        val isOnline = user.online
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.width(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.width(80.dp)
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
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background
                                ), start = Offset(0f, 0f), end = Offset(14.dp.value, 14.dp.value)
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
            Text(
                text = highlightSearchedText(
                    "You", sharedViewModel.searchValue.value
                ),
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 5.dp),
                fontSize = 12.sp,
                maxLines = 1,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = highlightSearchedText(
                    localChatUser?.location ?: "Unknown", sharedViewModel.searchValue.value
                ),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light
            )
        }
    }
}
