package at.htlhl.testing.views

import android.annotation.SuppressLint
import android.os.Build
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
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.testing.data.Friend
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DropIn : ViewModel() {
    private var auth: FirebaseAuth
    private val _friendListData = MutableStateFlow<List<PersonList>>(emptyList())
    private val friendListData: StateFlow<List<PersonList>> get() = _friendListData


    init {
        auth = Firebase.auth
    }

    private var friendListDataListener: ListenerRegistration? = null
    private fun startListeningForSubCollectionData() {
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        val subCollectionRef = documentRef.collection("/friends")
            .whereEqualTo("status", "accepted")

        friendListDataListener?.remove()
        friendListDataListener =
            subCollectionRef.addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                val personListData = mutableListOf<PersonList>()

                querySnapshot?.let { snapshot ->
                    val subCollectionData = snapshot.toObjects(Friend::class.java)

                    for (friend in subCollectionData) {
                        FirebaseFirestore.getInstance().collection("user")
                            .document(friend.userID)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                val data = documentSnapshot.toObject(PersonList::class.java)
                                data?.let { personListData.add(it) }

                                _friendListData.value = personListData
                            }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        friendListDataListener?.remove()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun DropInScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val viewModel = viewModel<DropIn>()
        auth = Firebase.auth
        val lazyListState = rememberLazyListState()
        val friendListDataState =
            viewModel.friendListData.collectAsState(initial = emptyList())
        val friendListData: List<PersonList> = friendListDataState.value
        LaunchedEffect(key1 = Unit) {
            viewModel.startListeningForSubCollectionData()
        }
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
                            message.image,
                            message.lastMessage,
                            message.timestamp,
                        ), navController, sharedViewModel
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
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val formattedTime = person.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault())
            .toLocalTime().format(formatter)
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
                        text = formattedTime,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = person.lastMessage,
                    maxLines = 1,
                    fontSize = 15.sp,
                    color = Color.LightGray
                )
            }
        }
        Divider(thickness = 0.25f.dp, color = Color.LightGray)
    }
}