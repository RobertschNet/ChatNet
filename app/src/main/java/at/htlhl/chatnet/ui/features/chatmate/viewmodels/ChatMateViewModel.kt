package at.htlhl.chatnet.ui.features.chatmate.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FirebaseFirestore

class ChatMateViewModel : ViewModel() {

    fun filterChatMateChatsList(
        completeChatMateChatsList: List<InternalChatInstance>,
        searchedValue: String
    ): List<InternalChatInstance> {
        return if (searchedValue != "") completeChatMateChatsList.filter {
            it.personList.username["mixedcase"]?.contains(
                searchedValue, ignoreCase = true
            ) ?: false || it.lastMessage.text.contains(
                searchedValue, ignoreCase = true
            )
        } else completeChatMateChatsList
    }

    fun createChatMateChat(
        userID: String,
        chatData: List<FirebaseChat>,
        onEmptyChatAlreadyExists: () -> Unit
    ) {
        if (chatData.any { chat ->
                chat.tab == "chatmate" && chat.members.contains(userID) && chat.messages.isEmpty()
            }) {
            onEmptyChatAlreadyExists()
            return
        }

        val membersArray = arrayListOf(userID, "ChatMate")
        val fieldUpdates = hashMapOf(
            "members" to membersArray,
            "tab" to "chatmate",
            "unread" to emptyList<String>(),
        )
        FirebaseFirestore.getInstance().collection("chats").document().set(fieldUpdates)
    }
}