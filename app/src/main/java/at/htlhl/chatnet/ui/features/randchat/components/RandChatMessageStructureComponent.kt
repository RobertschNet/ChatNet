package at.htlhl.chatnet.ui.features.randchat.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.ui.features.dialogs.DeleteMessageDialog
import at.htlhl.chatnet.ui.features.dialogs.OptionsDialog
import at.htlhl.chatnet.ui.features.mixed.ChatViewMessageComponent
import at.htlhl.chatnet.util.activateVibrationEffect
import at.htlhl.chatnet.util.cloudfunctions.requestChatMateResponse
import at.htlhl.chatnet.util.copyToClipboard
import at.htlhl.chatnet.util.firebase.changeMessageVisibility
import at.htlhl.chatnet.util.firebase.deleteMessage
import at.htlhl.chatnet.viewmodels.SharedViewModel

@Composable
fun RandChatMessageStructureComponent(
    sharedViewModel: SharedViewModel,
    userData: FirebaseUser,
    chatMateChat: Boolean,
    message: InternalMessageInstance,
    previousMessage: InternalMessageInstance?,
    nextMessage: InternalMessageInstance?,
    chatRoomId: String,
    onChatMateResponseReceived: (String) -> Unit
) {
    val context = LocalContext.current
    var menuDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    val anchorPosition = remember { mutableStateOf<Offset?>(null) }
    val isFromUser = message.sender == userData.id
    val searchedValue = sharedViewModel.searchValue.value
    ChatViewMessageComponent(userID = userData.id,
        chatMateChat = chatMateChat,
        previousMessage = previousMessage,
        nextMessage = nextMessage,
        message = message,
        onImageClicked = {},
        searchedValue = searchedValue,
        onOpenActionMenuClicked = {
            if (isFromUser) {
                anchorPosition.value = Offset(200f, 0f)
            } else {
                anchorPosition.value = Offset(20f, 0f)
            }
            activateVibrationEffect(context = context)
            menuDialog = true
        })
    if (deleteDialog) {
        DeleteMessageDialog(isUser = isFromUser) { value ->
            if (value == "delete") {
                deleteMessage(chatRoomID = chatRoomId, messageID = message.id)
            } else if (value == "change") {
                changeMessageVisibility(
                    userID = userData.id, chatRoomID = chatRoomId, messageId = message.id
                )
            }
            deleteDialog = false
        }
    }
    if (menuDialog) {
        OptionsDialog(offset = anchorPosition.value) { value ->
            when (value) {
                "delete" -> {
                    deleteDialog = true
                }

                "copy" -> {
                    copyToClipboard(context = context, label = "Message", text = message.text)
                }

                "generate" -> {
                    sharedViewModel.updateChatMateResponseState(newChatMateResponseState = ChatMateResponseState.Loading)
                    requestChatMateResponse(data = message.text, onSuccess = { responseText ->
                        sharedViewModel.updateChatMateResponseState(newChatMateResponseState = ChatMateResponseState.Success)
                        onChatMateResponseReceived(responseText)

                    })

                }
            }
            menuDialog = false
        }
    }
}