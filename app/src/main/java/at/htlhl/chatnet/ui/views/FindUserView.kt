package at.htlhl.chatnet.ui.views

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.finduser.FindUserPersonElement
import at.htlhl.chatnet.ui.components.finduser.FindUserSearchedContent
import at.htlhl.chatnet.ui.components.finduser.FindUserTopBar
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

class FindUserView : ViewModel() {

    @OptIn(ExperimentalMaterialApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun FindUserScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        val scaffoldState = rememberBackdropScaffoldState(BackdropValue.Concealed)
        val coroutineScope = rememberCoroutineScope()
        val interactionSource = remember { MutableInteractionSource() }
        val searchedUsers by sharedViewModel.person.collectAsState(initial = emptyList())
        val searchText by sharedViewModel.searchTextFlow.collectAsState()
        val isSearching by sharedViewModel.isSearchingFlow.collectAsState()
        val chatDataState = sharedViewModel.chatData.collectAsState()
        val chatData: List<FirebaseChat> = chatDataState.value
        val friendListFriendsDataState =
            sharedViewModel.friendRandomFriendsListData.collectAsState()
        val friendListFriendsData: List<FirebaseUser> = friendListFriendsDataState.value

        val friendListDataState = sharedViewModel.friendListData.collectAsState()
        val friendListData: List<FirebaseUser> = friendListDataState.value
        val finalFriendList =
            friendListData.filter { friend -> friend.statusFriend == "pending" }
        val finalFriendListFriends = friendListFriendsData.filter { friend ->
            friendListData.none { it.id == friend.id } && friend.id != sharedViewModel.auth.currentUser?.uid.toString()
        }
        BackdropScaffold(
            scaffoldState = scaffoldState,
            frontLayerShape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
            headerHeight = 100.dp,
            stickyFrontLayer = false,
            backLayerBackgroundColor = Color.White,
            persistentAppBar = true,
            peekHeight = screenHeight/3,
            frontLayerBackgroundColor = Color.White,
            frontLayerScrimColor = Color.Transparent,
            frontLayerElevation = 10.dp,
            modifier = Modifier.fillMaxSize(),
            appBar = {
                FindUserTopBar(
                    navController, interactionSource, {
                        coroutineScope.launch {
                            scaffoldState.reveal()
                        }
                    }, { text ->
                        onSearchTextChanged(text, sharedViewModel)
                    })
            },
            frontLayerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFFFFF))
                ) {
                    Spacer(modifier = Modifier.height(15.dp))
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
                    Text(
                        text = "Users who follow you:",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 15.dp, top = 8.dp)
                    )
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                    ) {

                        if (finalFriendList.isEmpty()) {
                            item {
                                Text(
                                    text = "There are currently no users who follow you.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.padding(start = 15.dp, top = 20.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        items(finalFriendList) { person ->
                            FindUserPersonElement(
                                person = person,
                                deleteAble = true,
                                sharedViewModel = sharedViewModel,
                                searchedUser = "pending",
                                onUserClicked = {
                                    sharedViewModel.updatePublicUser(it)
                                    navController.navigate(Screens.PublicProfileScreen.route)
                                },
                                onActionClicked = { clickedPerson, _ ->
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
                                    sharedViewModel.removeDropInUser(clickedPerson.id)
                                    sharedViewModel.saveFriendForFriend(
                                        person = clickedPerson,
                                        status = "accepted"
                                    )
                                    sharedViewModel.saveFriendForUser(
                                        person = clickedPerson,
                                        status = "accepted"
                                    )
                                }
                            )
                        }
                        if (finalFriendListFriends.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Suggestions for you:",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 15.dp, top = 20.dp)
                                )
                            }
                        }
                        items(finalFriendListFriends) { person ->
                            FindUserPersonElement(
                                person = person,
                                deleteAble = false,
                                sharedViewModel = sharedViewModel,
                                searchedUser = "searchedUser",
                                onUserClicked = {
                                    sharedViewModel.updatePublicUser(it)
                                    navController.navigate(Screens.PublicProfileScreen.route)
                                },
                                onActionClicked = { clickedPerson, _ ->
                                    sharedViewModel.saveFriendForFriend(
                                        person = clickedPerson,
                                        status = "pending"
                                    )
                                    sharedViewModel.saveFriendForUser(
                                        person = clickedPerson,
                                        status = "requested"
                                    )
                                }
                            )
                        }
                    }
                }
            }, backLayerContent = {
                FindUserSearchedContent(
                    friendListData,
                    chatData,
                    searchedUsers,
                    isSearching,
                    searchText,
                    sharedViewModel,
                    navController
                )
            }
        )
    }
    private fun onSearchTextChanged(text: String, sharedViewModel: SharedViewModel) {
        Log.println(Log.INFO, "SearchView", "Text: $text")
        sharedViewModel.searchedPersons.value = emptyList()
        sharedViewModel.isSearching.value = true
        sharedViewModel.searchText.value = text
        if (sharedViewModel.searchText.value.isBlank()) {
            sharedViewModel.isSearching.value = false
        }
    }
}