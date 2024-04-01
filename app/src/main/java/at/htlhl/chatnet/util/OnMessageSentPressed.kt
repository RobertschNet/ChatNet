package at.htlhl.chatnet.util

import android.content.Context
import android.widget.Toast
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.getImageUploadList
import at.htlhl.chatnet.data.removeImageUploadList
import at.htlhl.chatnet.util.firebase.createImageToUpload
import kotlinx.coroutines.CoroutineScope


fun onMessageSentPressed(
    coroutineScope: CoroutineScope,
    chatRoomID: String,
    userData:FirebaseUser,
    friendData: InternalChatInstance,
    chatMateChat: Boolean,
    context: Context,
    text: String,
    onUpdateChatMateResponseState: (ChatMateResponseState) -> Unit,
    onSentWhileBlocked: () -> Unit,
    onIsLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {
    if (!userData.blocked.contains(friendData.personList.id)) {
        if (text.isNotEmpty() || getImageUploadList(friendData.chatRoomID).isNotEmpty()) {
            if (getImageUploadList(friendData.chatRoomID).isEmpty()) {
                onSuccess()
                uploadSentMessage(
                    userData = userData,
                    chatRoomID = chatRoomID,
                    friendData = friendData,
                    coroutineScope = coroutineScope,
                    chatMateChat = chatMateChat,
                    text = text, onUpdateChatMateResponseState = { chatMateResponseState ->
                       onUpdateChatMateResponseState(chatMateResponseState)
                    },
                )
            } else {
                onIsLoading(true)
                createImageToUpload(
                    context = context,
                    selectedImageUris = getImageUploadList(id = friendData.chatRoomID),
                    onUploadSuccess = { success ->
                        removeImageUploadList(id = friendData.chatRoomID)
                        uploadSentMessage(
                            userData = userData,
                            chatRoomID = chatRoomID,
                            friendData = friendData,
                            coroutineScope = coroutineScope,
                            chatMateChat = chatMateChat,
                            text = text,
                            images = success,
                            onUpdateChatMateResponseState = { chatMateResponseState ->
                                onUpdateChatMateResponseState(chatMateResponseState)
                            },
                        ) {uploaded->
                            onIsLoading(false)
                            if (uploaded) {
                                removeImageUploadList(id = friendData.chatRoomID)
                                onSuccess()
                            } else {
                                Toast.makeText(context, "Error sending message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onUploadError = {
                        onIsLoading(false)
                        Toast.makeText(context, "Error uploading image", Toast.LENGTH_SHORT).show()
                    })
            }
        }
    } else {
        onSentWhileBlocked()
    }

}