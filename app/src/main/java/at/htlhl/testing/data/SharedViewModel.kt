package at.htlhl.testing.data

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp

class SharedViewModel : ViewModel() {
    val friends = mutableStateOf(listOf<PersonList>())
    val user = mutableStateOf(PersonList("", "", "", "", Timestamp.now()))
}
