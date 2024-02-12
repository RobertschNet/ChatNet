package at.htlhl.chatnet.ui.features.chats.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChatsViewModel : ViewModel() {

    val auth: FirebaseAuth = Firebase.auth

    fun createImageList(
        messages: List<InternalMessageInstance>,
    ): List<InternalMessageInstance> {
        val imageList = arrayListOf<InternalMessageInstance>()
        messages.forEach {
            if (it.images.isNotEmpty()) {
                it.images.forEach { image ->
                    if (it.visible.contains(auth.currentUser!!.uid)) {
                        imageList.add(
                            InternalMessageInstance().copy(images = arrayListOf(image))
                        )
                    }
                }
            }
        }
        return imageList
    }

    fun filterFriendsList(
        searchedValue: String,
        completeChatList: List<InternalChatInstance>
    ): List<InternalChatInstance> {
        return if (searchedValue != "") completeChatList.filter {
            it.personList.username["mixedcase"]?.contains(
                searchedValue, ignoreCase = true
            ) ?: false || it.lastMessage.text.contains(
                searchedValue, ignoreCase = true
            )
        } else completeChatList
    }
}