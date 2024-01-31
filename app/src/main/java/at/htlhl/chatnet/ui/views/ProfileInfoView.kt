package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.MotionScene
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.TagElement
import at.htlhl.chatnet.ui.components.dialogs.BlockUserDialog
import at.htlhl.chatnet.ui.components.dialogs.DeleteAllMediaDialog
import at.htlhl.chatnet.ui.components.dialogs.DeleteAllMessagesDialog
import at.htlhl.chatnet.ui.components.dialogs.DeleteFriendDialog
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ProfileInfoView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ProfileInfoScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        var progress by remember { mutableFloatStateOf(1f) }
        val totalHeight = remember { mutableFloatStateOf(0f) }
        val lazyListState = rememberLazyListState()
        val friendsFromFriendsListState = sharedViewModel.friendFriendsListData.collectAsState()
        val friendsFromFriendsList: List<FirebaseUser> = friendsFromFriendsListState.value
        val friendState = sharedViewModel.friend.collectAsState()
        val friend: InternalChatInstance = friendState.value
        val userState = sharedViewModel.user.collectAsState()
        val user: FirebaseUser = userState.value
        val chatDataState = sharedViewModel.chatData.collectAsState()
        val chatData: List<FirebaseChat> = chatDataState.value
        sharedViewModel.fetchFriendsFriends(friend)

        val chat: FirebaseChat =
            chatData.find {
                it.chatRoomID ==
                        friend.chatRoomID
            }!!
        val messageListFromMatchingChat: List<InternalMessageInstance> = chat.let {
            it.messages.map { message ->
                InternalMessageInstance(
                    isFromCache = message.isFromCache,
                    id = message.id,
                    sender = message.sender,
                    images = message.images,
                    read = message.read,
                    text = message.text,
                    timestamp = message.timestamp,
                    visible = message.visible,
                )
            }
        }
        sharedViewModel.imageList.value =
            createImageList(messageListFromMatchingChat, sharedViewModel)
        val imageList = sharedViewModel.imageList.value.toMutableStateList()
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        LaunchedEffect(Unit) {
            totalHeight.floatValue = lazyListState.layoutInfo.viewportSize.height.toFloat()
        }
        Scaffold(
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.BottomCenter),
                        state = lazyListState,
                        content = {
                            stickyHeader {
                                ProfileHeader(
                                    progress = derivedStateOf { progress },
                                    navController = navController,
                                    friend = friend
                                )
                            }
                            item {
                                ProfileInfoContent(
                                    sharedViewModel,
                                    navController,
                                    friendsFromFriendsList,
                                    imageList,
                                    friend,
                                    user,
                                )
                            }
                        }
                    )
                }
            }
        )
        if (remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }.value == 0) {
            val currentScroll =
                remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset.toFloat() } }
            Log.println(Log.INFO, "currentScroll", currentScroll.toString())
            Log.println(Log.INFO, "totalHeight", totalHeight.floatValue.toString())
            val sensitivity = 0.2f
            progress =
                (1 - (currentScroll.value / (totalHeight.floatValue * sensitivity))).coerceIn(
                    0f,
                    1.0f
                )
            Log.println(Log.INFO, "progress", progress.toString())
        }


    }

    @Composable
    fun ProfileInfoContent(
        sharedViewModel: SharedViewModel,
        navController: NavController,
        friendsFromFriendsList: List<FirebaseUser>,
        imageList: List<InternalMessageInstance>,
        friend: InternalChatInstance,
        user: FirebaseUser,
    ) {
        var blockDialog by remember { mutableStateOf(false) }
        var removeFriendDialog by remember { mutableStateOf(false) }
        var deleteAllMediaDialog by remember { mutableStateOf(false) }
        var deleteAllMessagesDialog by remember { mutableStateOf(false) }
        Spacer(modifier = Modifier.height(15.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .width(10.dp)
                )
                Column {
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Hallo! Ich bin Tobias Brandl",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Text(
                        text = "10 January 2021",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth(), elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row {
                    Text(
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                        text = "Tags",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TagElement(
                        element = "Programming",
                        color = Color(0xFFE91E63),
                        icon = Icons.Default.Code,
                        smallSize = false
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    TagElement(
                        element = "Eating",
                        color = Color(0xFF4CAF50),
                        icon = Icons.Default.Fastfood,
                        smallSize = false
                    )
                }
                Row {
                    TagElement(
                        element = "Sports",
                        color = Color(0xFF9C27B0),
                        icon = Icons.Default.SportsSoccer,
                        smallSize = false
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    TagElement(
                        element = "Gaming",
                        color = Color(0xFFFFEB3B),
                        icon = Icons.Default.Mouse,
                        smallSize = false
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            text = "Media and links",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Text(
                            modifier = Modifier
                                .clickable {
                                    sharedViewModel.imagePosition.intValue = 0
                                    navController.navigate(Screens.ImageViewScreen.route)
                                },
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                ) {
                                    append(imageList.size.toString())
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                ) {
                                    append(" >")
                                }
                            },
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Spacer(modifier = Modifier.height(2.5f.dp))
                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            item {
                                Spacer(modifier = Modifier.width(5.dp))
                            }

                            items(imageList.size) {
                                SubcomposeAsyncImage(
                                    modifier = Modifier
                                        .clickable {
                                            sharedViewModel.imagePosition.intValue = it
                                            navController.navigate(Screens.ImageViewScreen.route)
                                        }
                                        .height(100.dp)
                                        .width(100.dp)
                                        .padding(5.dp)
                                        .border(
                                            width = 2.dp,
                                            color = Color.White,
                                        ),
                                    model = imageList[it].images[0],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (imageList.isNotEmpty()) {
                                item {
                                    IconButton(onClick = {
                                        sharedViewModel.imagePosition.intValue = 0
                                        navController.navigate(Screens.ImageViewScreen.route)
                                    }) {
                                        SubcomposeAsyncImage(
                                            model = R.drawable.arrow_right_svgrepo_com,
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(Color.Gray),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Column(modifier = Modifier.background(Color.White)) {
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp),
                    text = "Chat Settings",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            if (user.muted.contains(friend.personList.id)) {
                                sharedViewModel.updateMuteFriendStatus(true)
                                sharedViewModel.updateFriend(
                                    InternalChatInstance(
                                        personList = FirebaseUser(
                                            friend.personList.blocked,
                                            friend.personList.image,
                                            friend.personList.username,
                                            friend.personList.status,
                                            friend.personList.id,
                                            friend.personList.email,
                                            friend.personList.pinned,
                                            friend.personList.color,
                                            friend.personList.connected,
                                            friend.personList.muted,
                                            friend.personList.statusFriend
                                        ),
                                        timestampMessage = friend.timestampMessage,
                                        lastMessage = friend.lastMessage,
                                        pinChat = friend.pinChat,
                                        read = friend.read,
                                        markedAsUnread = friend.markedAsUnread,
                                        chatRoomID = friend.chatRoomID
                                    )
                                )
                            } else {
                                sharedViewModel.updateMuteFriendStatus(false)
                                sharedViewModel.updateFriend(
                                    InternalChatInstance(
                                        personList = FirebaseUser(
                                            friend.personList.blocked,
                                            friend.personList.image,
                                            friend.personList.username,
                                            friend.personList.status,
                                            friend.personList.id,
                                            friend.personList.email,
                                            friend.personList.pinned,
                                            friend.personList.color,
                                            friend.personList.connected,
                                            friend.personList.muted,
                                            friend.personList.statusFriend
                                        ),
                                        timestampMessage = friend.timestampMessage,
                                        lastMessage = friend.lastMessage,
                                        pinChat = friend.pinChat,
                                        read = friend.read,
                                        markedAsUnread = friend.markedAsUnread,
                                        chatRoomID = friend.chatRoomID
                                    )
                                )
                            }
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = if (user.muted.contains(friend.personList.id)) R.drawable.speaker_svgrepo_com else R.drawable.speaker_none_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = if (user.muted.contains(friend.personList.id)) "Unmute ${friend.personList.username["mixedcase"]}" else "Mute ${friend.personList.username["mixedcase"]}",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            if (friend.pinChat) {
                                sharedViewModel.updatePinChatStatus(true)
                                sharedViewModel.updateFriend(
                                    InternalChatInstance(
                                        personList = friend.personList,
                                        timestampMessage = friend.timestampMessage,
                                        lastMessage = friend.lastMessage,
                                        pinChat = false,
                                        read = friend.read,
                                        markedAsUnread = friend.markedAsUnread,
                                        chatRoomID = friend.chatRoomID
                                    )
                                )
                            } else {
                                sharedViewModel.updatePinChatStatus(false)
                                sharedViewModel.updateFriend(
                                    InternalChatInstance(
                                        friend.personList,
                                        timestampMessage = friend.timestampMessage,
                                        lastMessage = friend.lastMessage,
                                        pinChat = true,
                                        read = friend.read,
                                        markedAsUnread = friend.markedAsUnread,
                                        chatRoomID = friend.chatRoomID
                                    )
                                )
                            }
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = if (friend.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = if (friend.pinChat) "Unpin Chat" else "Pin Chat",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable { deleteAllMessagesDialog = true }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = R.drawable.comment_delete_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Delete Chat Messages",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable { deleteAllMediaDialog = true }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = R.drawable.gallery_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Delete Shared Media",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Column(modifier = Modifier.background(Color.White)) {
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp),
                    text = "Friend Settings",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            blockDialog = true
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(if (user.blocked.contains(friend.personList.id)) Color.Black else Color.Red),
                        model = R.drawable.person_block_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = if (user.blocked.contains(friend.personList.id)) "Unblock ${friend.personList.username["mixedcase"]}" else "Block ${friend.personList.username["mixedcase"]}",
                        fontSize = 16.sp,
                        color = if (user.blocked.contains(friend.personList.id)) Color.Black else Color.Red,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            removeFriendDialog = true
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Red),
                        model = R.drawable.garbage_bin_recycle_bin_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Remove ${friend.personList.username["mixedcase"]}",
                        fontSize = 16.sp,
                        color = Color.Red,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Friends in common",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp),
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.height(2.5f.dp))
                    Column(content = {
                        Spacer(modifier = Modifier.height(5.dp))
                        friendsFromFriendsList.forEach {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(10.dp))
                                SubcomposeAsyncImage(
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(50.dp)
                                        .padding(5.dp)
                                        .clip(CircleShape),
                                    model = it.image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        textAlign = TextAlign.Start,
                                        overflow = TextOverflow.Ellipsis,
                                        text = it.username["mixedcase"].toString(),
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TagElement(
                                            element = "Sports",
                                            color = Color(0xFF4CAF50),
                                            icon = Icons.Default.SportsSoccer,
                                            smallSize = true
                                        )
                                        TagElement(
                                            element = "Programming",
                                            color = Color(0xFFE91E63),
                                            icon = Icons.Default.Code,
                                            smallSize = true
                                        )
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
        if (removeFriendDialog) {
            DeleteFriendDialog(friend = friend) { value ->
                if (value == "deleted") {
                    sharedViewModel.deleteChatRoom()
                    sharedViewModel.deleteFriendFromFriendList()
                    navController.navigate(Screens.ChatsViewScreen.route)
                }
                removeFriendDialog = false
            }
        }
        if (blockDialog) {
            BlockUserDialog(
                chatPartner = friend,
                chatUser = user
            ) { value ->
                if (value == "blocked") {
                    sharedViewModel.updateBlockedUserList(
                        user.blocked.contains(
                            friend.personList.id
                        )
                    )
                }
                blockDialog = false
            }
        }
        if (deleteAllMediaDialog) {
            DeleteAllMediaDialog { value ->
                if (value == "me") {
                    sharedViewModel.changeMediaVisibility(userContext = true, isMedia = true)
                } else if (value == "everyone") {
                    sharedViewModel.changeMediaVisibility(userContext = false, isMedia = true)
                }
                deleteAllMediaDialog = false
            }
        }
        if (deleteAllMessagesDialog) {
            DeleteAllMessagesDialog { value ->
                if (value == "me") {
                    sharedViewModel.changeMediaVisibility(userContext = true, isMedia = false)
                } else if (value == "everyone") {
                    sharedViewModel.changeMediaVisibility(userContext = false, isMedia = false)
                }
                deleteAllMessagesDialog = false
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    @OptIn(ExperimentalMotionApi::class)
    @Composable
    fun ProfileHeader(
        progress: State<Float>,
        navController: NavController,
        friend: InternalChatInstance
    ) {
        Log.println(Log.INFO, "Hallo", progress.toString())
        val context = LocalContext.current
        val motionScene = remember {
            context.resources
                .openRawResource(R.raw.userinfo_motion_layout)
                .readBytes()
                .decodeToString()
        }
        MotionLayout(
            motionScene = MotionScene(content = motionScene),
            progress = progress.value,
            modifier = Modifier.fillMaxWidth()
        ) {
            val profilePicProperties = motionProperties(id = "profile_pic")
            Card(
                elevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .layoutId("box")
            ) {}
            Image(
                painter = painterResource(id = R.drawable.back_svgrepo_com_1_),
                contentDescription = null,
                modifier = Modifier
                    .clickable { navController.navigateUp() }
                    .clip(CircleShape)
                    .layoutId("back_arrow")
            )
            SubcomposeAsyncImage(
                model = friend.personList.image,
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .layoutId("profile_pic")
                    .shimmerEffect()
            )
            Text(
                text = friend.personList.username["mixedcase"].toString(),
                overflow = TextOverflow.Ellipsis,
                fontSize = 26.sp,
                modifier = Modifier.layoutId("username"),
                color = profilePicProperties.value.color("background")
            )
        }
    }

    private fun createImageList(
        messages: List<InternalMessageInstance>,
        sharedViewModel: SharedViewModel
    ): List<InternalMessageInstance> {
        val imageList = arrayListOf<InternalMessageInstance>()
        messages.forEach {
            if (it.images.isNotEmpty()) {
                it.images.forEach { image ->
                    if (it.visible.contains(sharedViewModel.auth.currentUser!!.uid)) {
                        imageList.add(
                            InternalMessageInstance(
                                isFromCache = it.isFromCache,
                                id = it.id,
                                sender = it.sender,
                                images = arrayListOf(image),
                                read = it.read,
                                text = it.text,
                                timestamp = it.timestamp,
                                visible = it.visible
                            )
                        )
                    }
                }
            }
        }
        return imageList
    }
}