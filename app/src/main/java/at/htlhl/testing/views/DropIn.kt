package at.htlhl.testing.views

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily.Companion.Cursive
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.testing.data.Friend
import at.htlhl.testing.data.Message
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import java.time.format.DateTimeFormatter

class DropIn : ViewModel() {


    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint(
        "UnusedMaterialScaffoldPaddingParameter",
        "UnusedMaterial3ScaffoldPaddingParameter"
    )
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val lazyListState = rememberLazyListState()
        val user= sharedViewModel.auth.currentUser?.uid

        val friendListDataState = sharedViewModel.friendListData.collectAsState()
        val friendListData: List<PersonList> = friendListDataState.value
        val messageListDataState = sharedViewModel.messageData1.collectAsState()
        val messageData: List<Message> = messageListDataState.value
        println("S$friendListData")
        println("M$messageData")
        Log.d(TAG, "Ausgabe: $friendListData")
        Scaffold {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
                lazyListState
            ) {
                item {
                    TopAppBar(
                        Modifier
                            .height(70.dp)
                            .fillMaxWidth(),
                        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "ChatNet",
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 20.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                fontFamily = Cursive
                            )
                            Icon(
                                imageVector = Icons.Outlined.PersonAddAlt1,
                                contentDescription = "AddFriend",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 20.dp, top = 5.dp)
                                    .size(30.dp)
                                    .clickable {
                                        navController.navigate(Screens.SearchViewScreen.Route)
                                    }
                            )
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 80.dp, top = 5.dp)
                                    .size(30.dp)
                            )
                        }
                    }
                    Divider(
                        thickness = 1.dp,
                        color = if (isSystemInDarkTheme()) Color.DarkGray else Color.Transparent
                    )
                }
                items(friendListData) { message ->
                    ChatItem(
                        PersonList(
                            message.userID,
                            message.name,
                            message.status,
                            message.image,
                        ),
                        navController,
                        sharedViewModel
                    )
                }



            }
        }
    }

        @RequiresApi(Build.VERSION_CODES.O)
        @Composable
        fun ChatItem(
            person: PersonList,
            navController: NavController,
            sharedViewModel: SharedViewModel
        ) {

            Row(
                modifier = Modifier
                    .clickable {
                        sharedViewModel.user.value = person
                        navController.navigate(Screens.ChatScreen.Route)
                    }
                    .fillMaxWidth()
                    .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
                    .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),
            ) {
                Image(
                    contentDescription = null,
                    painter = rememberAsyncImagePainter(person.image),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(50.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                Column(Modifier.padding(horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = person.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                        Text(
                            text = "1984-09-11",
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    }
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = person.status,
                        maxLines = 1,
                        fontSize = 15.sp,
                        color = Color.LightGray
                    )
                }
            }
            Divider(thickness = 0.25f.dp, color = Color.LightGray)
        }
}