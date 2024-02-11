package at.htlhl.chatnet.ui.features.finduser.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.htlhl.chatnet.data.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FindUserViewModel: ViewModel(){

    private val searchUserText = MutableStateFlow("")
    val searchUserTextFlow = searchUserText.asStateFlow()
    private val searchedPersons = MutableStateFlow<List<FirebaseUser>>(emptyList())
    private val isSearchingForUsers = MutableStateFlow(false)
    val isSearchingFlow = isSearchingForUsers.asStateFlow()

    @OptIn(FlowPreview::class)
    var foundUsers = searchUserTextFlow.debounce(1000L)
        .combine(searchedPersons) { text, person ->
            if (text.isBlank()) {
                emptyList()
            } else {
                val initialUsers = retrieveSearchedUsers()
                if (initialUsers.isEmpty()) {
                    viewModelScope.launch {
                        delay(500)
                        isSearchingForUsers.update { false }
                    }
                }
                searchedPersons.value = initialUsers.toMutableList()
                person.filter { it.doesMatchUsername(text) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), searchedPersons.value)
        .onEach {
            viewModelScope.launch {
                delay(500)
                isSearchingForUsers.update { false }
            }
        }
    private suspend fun retrieveSearchedUsers(): List<FirebaseUser> {
        try {
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
            val snapshot = FirebaseFirestore.getInstance().collection("users")
                .orderBy("username.lowercase")
                .startAt(searchUserTextFlow.value.lowercase())
                .endAt(searchUserTextFlow.value.lowercase() + '\uf8ff')
                .get()
                .await()
            return snapshot.documents.mapNotNull { document ->
                try {
                    val userData = document.toObject(FirebaseUser::class.java)
                    if (userData?.id == currentUserUid) {
                        null
                    } else {
                        userData
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emptyList()
    }

     fun onSearchTextChanged(text: String) {
        searchedPersons.value = emptyList()
        isSearchingForUsers.value = true
        searchUserText.value = text
        if (searchUserText.value.isBlank()) {
            isSearchingForUsers.value = false
        }
    }
}