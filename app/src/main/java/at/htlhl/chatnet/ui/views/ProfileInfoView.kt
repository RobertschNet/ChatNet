package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.dialogs.BlockUserDialog
import at.htlhl.chatnet.ui.components.dialogs.DeleteAllMediaDialog
import at.htlhl.chatnet.ui.components.dialogs.DeleteAllMessagesDialog
import at.htlhl.chatnet.ui.components.dialogs.DeleteFriendDialog
import at.htlhl.chatnet.ui.components.mixed.TagElement
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

class ProfileInfoView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun ProfileInfoScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val lazyListState = rememberLazyListState()
        val friendState = sharedViewModel.friend.collectAsState()
        val friend: InternalChatInstance = friendState.value
        val userState = sharedViewModel.user.collectAsState()
        val user: FirebaseUser = userState.value
        val imageList = sharedViewModel.imageList.value.toMutableStateList()
        var friendsFromFriendsList by remember {
            mutableStateOf(
                listOf<FirebaseUser>()
            )
        }
        sharedViewModel.fetchFriendsFriends(friend){
            friendsFromFriendsList=it
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
                            item {
                                ProfileHeader(
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
        Spacer(modifier = Modifier.height(10.dp))
        if (imageList.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp),
                shape = RoundedCornerShape(25.dp),
                elevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.White),
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(7.5f.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis,
                                text = "Media and links",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
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
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                                overflow = TextOverflow.Ellipsis,
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )
                                    ) {
                                        append(imageList.size.toString())
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Gray,
                                            fontFamily = FontFamily.SansSerif,
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
                                            )
                                            .clip(RoundedCornerShape(16.dp)),
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
            Spacer(modifier = Modifier.height(10.dp))
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp),
            elevation = 10.dp,
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.height(7.5f.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 15.dp),
                    text = "Chat Settings",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
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
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp),
            elevation = 10.dp,
            shape = RoundedCornerShape(25.dp)
        ) {
            Column(modifier = Modifier.background(Color.White)) {
                Spacer(modifier = Modifier.height(7.5f.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 15.dp),
                    text = "Friend Settings",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
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
        if (friendsFromFriendsList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp),
                elevation = 10.dp,
                shape = RoundedCornerShape(25.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(7.5f.dp))
                        Text(
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            text = "Friends in common",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 15.dp),
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Normal,
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
                            Spacer(modifier = Modifier.height(5.dp))
                        })
                    }
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = R.drawable.logo__1_,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                colorFilter = ColorFilter.tint(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "ChatNet",
                fontSize = 24.sp,
                color = Color.LightGray,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

    }

    @Composable
    fun ProfileHeader(
        navController: NavController,
        friend: InternalChatInstance
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Box(modifier = Modifier.fillMaxWidth()) {
                SubcomposeAsyncImage(model = R.drawable.back_svgrepo_com_1_,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            navController.navigateUp()
                        }
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(30.dp))
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SubcomposeAsyncImage(
                        model = friend.personList.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(150.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = friend.personList.username["mixedcase"].toString(),
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TagElement(
                            element = "Programming",
                            color = Color(0xFF9C27B0),
                            icon = Icons.Default.Code,
                            smallSize = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TagElement(
                            element = "Sports",
                            color = Color(0xFF4CAF50),
                            icon = Icons.Default.SportsSoccer,
                            smallSize = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TagElement(
                            element = "Gaming",
                            color = Color(0xFFFFEB3B),
                            icon = Icons.Default.Mouse,
                            smallSize = false
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

    }
}