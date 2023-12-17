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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ProfileInfoView {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ProfileInfoScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        var progress by remember { mutableFloatStateOf(1f) }
        val totalHeight = remember { mutableFloatStateOf(0f) }
        val lazyListState = rememberLazyListState()
        val chat: FirebaseChat =
            sharedViewModel.chatData.value.find { it.chatRoomID == sharedViewModel.friend.value.chatRoomID }!!
        val imageList = chat.messages.filter { message ->
            message.image != "" && message.visible.contains(sharedViewModel.auth.currentUser!!.uid)
        }
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
                                    sharedViewModel = sharedViewModel,
                                    navController = navController
                                )
                            }
                            item {
                                ProfileInfoContent(sharedViewModel, navController, imageList)
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
        imageList: List<InternalMessageInstance>
    ) {
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
                        .shadow(10.dp)
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
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Tags",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .border(
                                width = 0.2f.dp,
                                color = Color.Red.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .background(Color.Red.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBike,
                                contentDescription = "Biking",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Text(
                                text = "Biking",
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .border(
                                width = 0.2f.dp,
                                color = Color.Yellow.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .background(Color.Yellow.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = "Eating",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Text(
                                text = "Eating",
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .border(
                                width = 0.2f.dp,
                                color = Color.Green.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .background(Color.Green.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Programming",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Text(
                                text = "Programming",
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .border(
                                width = 0.2f.dp,
                                color = Color.Blue.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .background(Color.Blue.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsFootball,
                                contentDescription = "Football",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Text(
                                text = "Football",
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                            )
                        }
                    }
                }
                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row {
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
                                    sharedViewModel.imagePosition.value = 0
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
                                    append("69 ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                ) {
                                    append(">")
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
                                            sharedViewModel.imagePosition.value = it
                                            navController.navigate(Screens.ImageViewScreen.route)
                                        }
                                        .height(100.dp)
                                        .width(100.dp)
                                        .padding(5.dp)
                                        .border(
                                            width = 2.dp,
                                            color = Color.White,
                                        ),
                                    model = imageList[it].image,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                            item {
                                IconButton(onClick = {
                                    sharedViewModel.imagePosition.value = 0
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
                        })
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp) {
            Column(modifier = Modifier.background(Color.White)) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Black),
                        model = R.drawable.speaker_none_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Mute Notifications",
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .height(50.dp)
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
                        text = "Media Visibility",
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
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.Red),
                        model = R.drawable.person_block_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Block Tobias Brandl",
                        fontSize = 16.sp,
                        color = Color.Red,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
                Row(
                    modifier = Modifier
                        .height(50.dp)
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
                        text = "Remove Friend Tobias Brandl",
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
                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            text = "Friends in common",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.5f.dp))
                    Column(content = {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    ),
                                model = "https://picsum.photos/200/300",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "Tobias Brandl",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                )
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "1 mutual friend",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    ),
                                model = "https://picsum.photos/200/300",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "Tobias Brandl",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                )
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "1 mutual friend",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    ),
                                model = "https://picsum.photos/200/300",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "Tobias Brandl",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                )
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "1 mutual friend",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(10.dp))
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = Color.Black,
                                        shape = CircleShape
                                    ),
                                model = "https://picsum.photos/200/300",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "Tobias Brandl",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                )
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = "1 mutual friend",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}


@OptIn(ExperimentalMotionApi::class)
@Composable
fun ProfileHeader(progress: State<Float>, sharedViewModel: SharedViewModel,navController: NavController) {
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
                .clickable { navController.navigate(Screens.ChatsViewScreen.route) }
                .clip(CircleShape)
                .layoutId("back_arrow")
        )
        SubcomposeAsyncImage(
            model = sharedViewModel.friend.value.personList.image,
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .layoutId("profile_pic")
                .shimmerEffect()
        )
        Text(
            text =sharedViewModel.friend.value.personList.username["mixedcase"].toString(),
            overflow = TextOverflow.Ellipsis,
            fontSize = 26.sp,
            modifier = Modifier.layoutId("username"),
            color = profilePicProperties.value.color("background")
        )

    }
}