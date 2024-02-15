package at.htlhl.chatnet.ui.features.randchat.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.FirebaseUser

class RandChatViewModel : ViewModel() {

    fun filterPreviousRandChatUsersList(
        previousRandChatUsersList: List<FirebaseUser>,
        searchedValue: String,
    ): List<FirebaseUser> {
        return if (searchedValue != "") previousRandChatUsersList.filter {
            it.username["mixedcase"]?.contains(
                searchedValue, ignoreCase = true
            ) ?: false
        } else previousRandChatUsersList
    }
}