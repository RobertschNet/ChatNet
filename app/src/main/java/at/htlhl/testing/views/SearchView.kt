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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class SearchView : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth


    private val _searchText = MutableStateFlow("")
    private val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    private val isSearching = _isSearching.asStateFlow()

    private val _person = MutableStateFlow<List<Person>>(emptyList())

    @OptIn(FlowPreview::class)
    val person = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_person) { text, person ->
            if (text.isBlank()) {
                person
            } else {
                val initialMessages = retrieveMessages()
                _person.value = initialMessages.toMutableList()
                person.filter { it.doesMatch(text) }
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _person.value)

    private fun onSearchTextChanged(text: String) {
        _searchText.value = text
    }

    private suspend fun retrieveMessages(): List<Person> {
        val snapshot = if (searchText.value.isBlank()) {
            db.collection("user")
                .orderBy("name")
                .get()
                .await()
        } else {
            db.collection("user")
                .orderBy("name")
                .startAt(searchText.value)
                .endAt(searchText.value + '\uf8ff')
                .get()
                .await()
        }
        return snapshot.documents.mapNotNull { document ->
            val firstname = document.getString("name")
            val image = document.getString("image")
            val lastMessage = document.getString("lastMessage")
            val userID = document.getString("userID")
            val timestamp = document.getTimestamp("timestamp")
            if (firstname != null && image != null && lastMessage != null && userID != null && timestamp != null) {
                Person(
                    image = image,
                    name = firstname,
                    lastMessage = lastMessage,
                    userID = userID,
                    timestamp = timestamp
                )
            } else {
                null
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun SearchViewScreen(navController: NavController) {
        Scaffold(
            containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            modifier = Modifier.fillMaxSize(),
            topBar = { TopBarSearchView(navController) },
            content = { ContentSearchView() })
    }

    @Composable
    fun TopBarSearchView(navController: NavController) {
        val viewModel = viewModel<SearchView>()
        val searchText by viewModel.searchText.collectAsState()
        val persons by viewModel.person.collectAsState()
        val isSearching by viewModel.isSearching.collectAsState()
        var search by rememberSaveable { mutableStateOf(true) }
        auth = Firebase.auth
        Row(modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 20.dp)) {
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .clickable { navController.navigate(Screens.DropInScreen.Route) }
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
                    .background(Color(0xFF1B1B1B), RoundedCornerShape(12.dp)),
                textStyle = MaterialTheme.typography.body1.copy(color = Color.White),
                cursorBrush = SolidColor(Color.Black),
                decorationBox = { innerTextField: @Composable () -> Unit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Color.DarkGray,
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
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 102.dp, top = 14.5f.dp)
            )
        } else {
            Icon(imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.DarkGray,
                modifier = Modifier
                    .padding(start = 360.dp, top = 14.5f.dp)
                    .size(24.dp)
                    .clickable { })
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else if (searchText.isNotBlank()) {
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
                            }
                            Text(
                                modifier = Modifier.padding(start = 10.dp),
                                text = person.lastMessage,
                                maxLines = 1,
                                fontSize = 15.sp,
                                color = Color.LightGray
                            )
                            Icon(
                                imageVector = Icons.Default.Check,
                                tint = Color.Green,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(20.dp)
                                    .clickable {
                                        getDocument { data ->
                                            if (data != null) {
                                                saveFriend(person=person, user = data)
                                            }
                                        }
                                        saveSubscribed(person)
                                    },
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ContentSearchView() {
    }

    private fun getDocument(onSuccess: (PersonList?) -> Unit) {
        val documentRef = db.collection("user").document(auth.currentUser!!.uid)
        documentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val personList = documentSnapshot.toObject(PersonList::class.java)
                    println(personList)
                    onSuccess(personList)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener {
                onSuccess(null)
            }
    }

    private fun saveSubscribed(person: Person) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${auth.currentUser!!.uid}/friends")
        val documentRef = collectionRef.document(person.userID)
        documentRef.set(person)
            .addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }

    private fun saveFriend(person: Person, user: PersonList) {
        val collectionRef =
            FirebaseFirestore.getInstance().collection("user/${person.userID}/friends")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        documentRef.set(user)
            .addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }
}

data class Person(
    val userID: String,
    val name: String,
    val image: String,
    val lastMessage: String,
    val timestamp: Timestamp,
) {
    fun doesMatch(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            "${name.first()}",
        )
        return matchingCombinations.any { it.contains(query, ignoreCase = true) }
    }
}
