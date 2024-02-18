package at.htlhl.chatnet.ui.features.chat.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.InternalMessageInstance

class ChatViewModel : ViewModel() {
    fun createImageList(messages: List<InternalMessageInstance>): List<InternalMessageInstance> {
        val imageList = arrayListOf<InternalMessageInstance>()
        messages.forEach { message ->
            if (message.images.isNotEmpty()) {
                message.images.forEach { image ->
                    imageList.add(
                        InternalMessageInstance(
                            isFromCache = message.isFromCache,
                            id = message.id,
                            sender = message.sender,
                            images = arrayListOf(image),
                            read = message.read,
                            text = message.text,
                            timestamp = message.timestamp,
                            visible = message.visible
                        )
                    )
                }
            }
        }
        return imageList
    }
}