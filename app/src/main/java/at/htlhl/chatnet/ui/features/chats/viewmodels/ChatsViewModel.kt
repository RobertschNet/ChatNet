package at.htlhl.chatnet.ui.features.chats.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChatsViewModel : ViewModel() {

    val auth: FirebaseAuth = Firebase.auth

    fun filterFriendsList(
        searchedValue: String, completeChatList: List<InternalChatInstance>
    ): List<InternalChatInstance> {
        if (searchedValue.isEmpty()) {
            return completeChatList
        }

        return completeChatList.asSequence().filter { chatInstance ->
            val usernameContainsSearch =
                chatInstance.personList.username["mixedcase"]
                    ?.contains(searchedValue, ignoreCase = true) ?: false
            val messageContainsSearch =
                chatInstance.lastMessage.text.contains(searchedValue, ignoreCase = true)
            usernameContainsSearch || messageContainsSearch
        }.toList()
    }

}