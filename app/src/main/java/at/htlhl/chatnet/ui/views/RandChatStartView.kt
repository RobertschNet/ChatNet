package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.finduser.FindUserPersonElement
import at.htlhl.chatnet.ui.components.mixed.TabsTopBar
import at.htlhl.chatnet.ui.components.mixed.TagElement
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

class RandChatStartView {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun RandChatStartScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val friendListDataState =
            sharedViewModel.friendListData.collectAsState(initial = arrayListOf(FirebaseUser()))
        val friendListData: List<FirebaseUser> = friendListDataState.value
        val userDataState = sharedViewModel.user.collectAsState(initial = FirebaseUser())
        val userData: FirebaseUser = userDataState.value
        val chatDataState = sharedViewModel.chatData.collectAsState()
        val chatData: List<FirebaseChat> = chatDataState.value
        val previousRandChatUsers= sharedViewModel.previousRandChatUser.value
        val completePreviousRandChatUsers=
            if (sharedViewModel.searchValue.value != "") previousRandChatUsers.filter {
                it.username["mixedcase"]?.contains(sharedViewModel.searchValue.value, ignoreCase = true) ?: false
                      //TODO:  Add filtering for tags
            } else previousRandChatUsers

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabsTopBar(
                    tab = "RandChat",
                    availableUsers = listOf(),
                    sharedViewModel = sharedViewModel
                )
            },
            content = {
                Column{
                    Spacer(modifier = Modifier.height(20.dp))
                    SubcomposeAsyncImage(
                        model = userData.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = userData.username["mixedcase"].toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TagElement(
                            element = "Sports",
                            color = Color(0xFF4CAF50),
                            icon = Icons.Default.SportsSoccer,
                            smallSize = false
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TagElement(
                            element = "LGBTQ",
                            color = Color(0xFFE91E63),
                            icon = Icons.Default.Flag,
                            smallSize = false
                        )

                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        shape = CircleShape,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            if (!userData.connected) {
                                sharedViewModel.getRandChat(
                                    sharedViewModel,
                                    false,
                                    navController
                                ) {}
                            }
                            navController.navigate(Screens.RandChatScreen.route)
                        }
                    ) {
                        Text(text = if (!userData.connected) "Start RandChat" else "Continue to Chat with User")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    if (completePreviousRandChatUsers.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "Recent RandChat Users",
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(completePreviousRandChatUsers) { previousUser ->
                            val savedUser = friendListData.find { previousUser.id == it.id }
                            FindUserPersonElement(
                                person = previousUser,
                                deleteAble = false,
                                sharedViewModel = sharedViewModel,
                                searchedUser = if (savedUser == null) "searchedUser" else if (savedUser.statusFriend == "pending") "pending" else "added",
                                onClick = { clickedPerson, add ->
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
                            )
                        }

                    }
                }
            }
        )
    }
}