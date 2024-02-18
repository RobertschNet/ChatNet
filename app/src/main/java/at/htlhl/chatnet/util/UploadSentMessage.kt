package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.util.cloudfunctions.requestChatMateResponse
import at.htlhl.chatnet.util.cloudfunctions.sendPushNotificationToPartner
import at.htlhl.chatnet.util.firebase.saveMessage
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


fun uploadSentMessage(
    userData: FirebaseUser,
    friendData: InternalChatInstance,
    coroutineScope: CoroutineScope,
    chatMateChat: Boolean,
    text: String,
    images: List<String> = arrayListOf(),
    onUpdateChatMateResponseState: (ChatMateResponseState) -> Unit,
    onSuccess: (Boolean) -> Unit
) {
    val message = FirebaseMessage(
        sender = userData.id,
        text = text,
        timestamp = Timestamp.now(),
        read = false,
        images = images,
        visible = listOf(
            friendData.personList.id,
            userData.id,
        )
    )
    coroutineScope.launch {
        saveMessage(chatRoomID = friendData.chatRoomID, message = message, {
            onSuccess(true)
            sendPushNotificationToPartner(
                userID = userData.id, friendID = friendData.personList.id, message = message
            )
        }, {
            onSuccess(false)
        })
    }
    if (chatMateChat) {
        onUpdateChatMateResponseState(ChatMateResponseState.Loading)
        requestChatMateResponse(data = text, onSuccess = { responseText ->
            onUpdateChatMateResponseState(ChatMateResponseState.Success)
            val messageElement = FirebaseMessage(
                sender = "chatmate",
                text = responseText,
                timestamp = Timestamp.now(),
                read = false,
                images = arrayListOf(),
                visible = listOf(
                    userData.id,
                )
            )
            coroutineScope.launch {
                saveMessage(chatRoomID = friendData.chatRoomID, message = messageElement, {
                    sendPushNotificationToPartner(
                        userID = userData.id,
                        friendID = friendData.personList.id,
                        message = messageElement
                    )
                    onSuccess(true)
                }, {
                    onSuccess(false)
                })
            }
        })
    }
}