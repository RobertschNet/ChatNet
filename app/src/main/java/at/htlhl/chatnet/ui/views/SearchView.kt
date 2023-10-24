package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChats
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.viewmodels.SharedViewModel
import at.htlhl.chatnet.navigation.Screens
import coil.compose.SubcomposeAsyncImage

class SearchView : ViewModel() {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun SearchViewScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val viewModel: SharedViewModel = viewModel()
        val persons by viewModel.person.collectAsState()
        val searchText by viewModel.searchText.collectAsState()
        val isSearching by viewModel.isSearching.collectAsState()
        val documentIdState = sharedViewModel.chatData.collectAsState()
        val documentationId: List<FirebaseChats> = documentIdState.value
        Scaffold(
            backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            modifier = Modifier.fillMaxSize(),
            topBar = { TopBarSearchView(navController, persons) },
            content = {
                ContentSearchView(
                    viewModel,
                    persons,
                    isSearching,
                    searchText,
                    documentationId,
                    sharedViewModel
                )
            })
    }

    @Composable
    fun TopBarSearchView(navController: NavController, persons: List<FirebaseUsers>) {
        val viewModel: SharedViewModel = viewModel()
        val searchText by viewModel.searchText.collectAsState()
        var search by rememberSaveable { mutableStateOf(true) }
        Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 20.dp, bottom = 10.dp)) {
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
                modifier = Modifier
                    .size(30.dp)
                    .clickable { navController.navigate(Screens.ChatsViewScreen.route) }
                    .align(Alignment.CenterVertically))
            BasicTextField(value = searchText,
                onValueChange = {
                    viewModel.onSearchTextChanged(it)
                    search = it.isEmpty()
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
                    .background(
                        if (isSystemInDarkTheme()) Color(0xFF1D2020) else Color.LightGray,
                        RoundedCornerShape(12.dp)
                    ),
                textStyle = MaterialTheme.typography.body1.copy(color = Color.White),
                cursorBrush = SolidColor(if (isSystemInDarkTheme()) Color.White else Color.Black),
                decorationBox = { innerTextField: @Composable () -> Unit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = if (isSystemInDarkTheme()) Color.DarkGray else Color.Gray,
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, top = 8.dp)
                                .height(30.dp)
                        ) {
                            innerTextField()
                        }
                    }
                })
        }
        if (search) {
            Text(
                text = "Search",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color.DarkGray else Color.Gray,
                modifier = Modifier.padding(start = 102.dp, top = 15f.dp)
            )
        } else {
            Icon(imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = if (isSystemInDarkTheme()) Color.DarkGray else Color.Gray,
                modifier = Modifier
                    .padding(start = 360.dp, top = 14.5f.dp)
                    .size(24.dp)
                    .clickable { viewModel.onSearchTextChanged(text = "") })
        }
    }

    @Composable
    fun ContentSearchView(
        viewModel: SharedViewModel,
        persons: List<FirebaseUsers>,
        isSearching: Boolean,
        searchText: String,
        documentId: List<FirebaseChats>,
        sharedViewModel: SharedViewModel
    ) {
        Divider(thickness = 0.25f.dp, color = Color.LightGray)
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        if (persons.isEmpty() && searchText.isNotEmpty()) {
            Text(
                text = "No results found for \"$searchText\"",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                modifier = Modifier
                    .padding(start = 20.dp, top = 10.dp)
            )
        }
        persons.forEach {
            if (it.doesMatchUsername(searchText) && searchText.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(persons) { person ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSystemInDarkTheme()) Color(0xF1161616) else Color.White)
                                .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp),
                        ) {
                            SubcomposeAsyncImage(
                                contentDescription = null,
                                model = person.image,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(50.dp),
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                loading = {
                                    CircularProgressIndicator()
                                }
                            )
                            Text(
                                text = person.username["mixedcase"].toString(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 10.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.PersonAddAlt,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 10.dp)
                                    .size(35.dp)
                                    .clickable {
                                        viewModel.getDocument { data ->
                                            val filteredChats = documentId.filter { chat ->
                                                chat.members.contains(person.id) && chat.members
                                                    .contains(sharedViewModel.auth.currentUser?.uid)
                                            }
                                            if (data != null) {
                                                viewModel.saveFriendForFriend(
                                                    person = person,
                                                    status = "pending"
                                                )
                                                viewModel.saveFriendForUser(
                                                    person = person,
                                                    status = "initiated"
                                                )
                                                if (filteredChats.isEmpty()) {
                                                    Log.println(
                                                        Log.INFO,
                                                        "Chat",
                                                        filteredChats.toString()
                                                    )
                                                    Log.println(
                                                        Log.INFO,
                                                        "Chat",
                                                        documentId.toString()
                                                    )
                                                }
                                            }
                                        }
                                    },
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}