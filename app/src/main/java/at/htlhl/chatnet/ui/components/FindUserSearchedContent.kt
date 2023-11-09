package at.htlhl.chatnet.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.viewmodels.SharedViewModel


fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), label = ""
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFDAD7D7),
                Color(0xFFB8B5B5),
                Color(0xFFDAD7D7),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
        .onGloballyPositioned {
            size = it.size
        }
}

@Composable
fun LoadingElement() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(Color.White)
            .padding(top = 10.dp, bottom = 10.dp, start = 15.dp, end = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(), verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .shimmerEffect()
                    .size(80.dp, 18.dp)
            )
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .shimmerEffect()
                    .size(120.dp, 18.dp)
            )
        }
        Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(75.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(25))
                    .shimmerEffect(),
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FindUserSearchedContent(
    friendList: List<FirebaseUsers>,
    chatData: List<FirebaseChat>,
    persons: List<FirebaseUsers>,
    isSearching: Boolean,
    searchText: String,
    sharedViewModel: SharedViewModel
) {
    Divider(thickness = 0.25f.dp, color = Color.LightGray)
    if (isSearching) {
        LazyColumn(content = {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            items(10) {
                LoadingElement()
            }
        })
    }
    if (searchText.isNotEmpty() && persons.isEmpty() && !isSearching) {
        Text(
            text = "No results found for \"$searchText\"",
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp)
                .fillMaxWidth()
        )
    } else if (searchText.isNotEmpty()) {
        Text(
            text = "Results for \"$searchText\"",
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp)
                .fillMaxWidth()

        )
    }
    persons.forEach { person ->
        if (person.doesMatchUsername(searchText) && searchText.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize())
            {
                items(persons) { person ->
                    val specificUser = friendList.find { it.id == person.id }
                    FindUserPersonElement(
                        person = person,
                        deleteAble = false,
                        sharedViewModel = sharedViewModel,
                        searchedUser = if (specificUser == null) "searchedUser" else if (specificUser.statusFriend == "pending") "pending" else "accepted",
                    ) { clickedPerson, add ->
                        if (add) {
                            val filteredChats = chatData.filter { chat ->
                                chat.members.contains(clickedPerson.id) && chat.members
                                    .contains(sharedViewModel.auth.currentUser?.uid)
                            }
                            if (filteredChats.isEmpty()) {
                                sharedViewModel.saveChatRoom(
                                    person = clickedPerson.id,
                                    tab = "chats"
                                )
                            } else {
                                sharedViewModel.updateChatRoom(
                                    tab = "chats",
                                    chatRoomId = filteredChats[0].chatRoomID
                                )
                            }
                            sharedViewModel.saveFriendForFriend(
                                person = clickedPerson,
                                status = "accepted"
                            )
                            sharedViewModel.saveFriendForUser(
                                person = clickedPerson,
                                status = "accepted"
                            )
                        } else {
                            sharedViewModel.saveFriendForFriend(
                                person = clickedPerson,
                                status = "pending"
                            )
                            sharedViewModel.saveFriendForUser(
                                person = clickedPerson,
                                status = "requested"
                            )
                        }
                    }
                }
            }
        }
    }
}