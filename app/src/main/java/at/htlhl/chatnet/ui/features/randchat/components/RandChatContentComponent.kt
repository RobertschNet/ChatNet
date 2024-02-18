package at.htlhl.chatnet.ui.features.randchat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun RandChatContentComponent(
    paddingValues: PaddingValues,
    sharedViewModel: SharedViewModel,
    userData: FirebaseUser,
    chatMateChat: Boolean,
    messages: List<InternalMessageInstance>,
    lazyListState: LazyListState,
    chatRoomId: String,
    onChatMateResponseReceived: (String) -> Unit
) {
    var animatedText by remember { mutableStateOf("ChatMate is thinking") }
    LaunchedEffect(sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
        while (true) {
            delay(750)
            animatedText = "ChatMate is thinking."
            delay(750)
            animatedText = "ChatMate is thinking.."
            delay(750)
            animatedText = "ChatMate is thinking..."
            delay(750)
            animatedText = "ChatMate is thinking..."
        }
    }
    LaunchedEffect(messages.size) {
        lazyListState.animateScrollToItem(0)
    }
    Column(
        modifier = Modifier
            .padding(top=paddingValues.calculateTopPadding())
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            modifier = Modifier.padding(bottom = 70.dp),
            reverseLayout = true,
            state = lazyListState
        ) {
            items(messages) { message ->
                val messageIndex = messages.indexOf(message)
                val previousMessage =
                    messages.getOrNull(messageIndex + 1)
                val nextMessage =
                    messages.getOrNull(messageIndex - 1)
                RandChatMessageStructureComponent(
                    sharedViewModel = sharedViewModel,
                    chatMateChat = chatMateChat,
                    userData = userData,
                    previousMessage = previousMessage,
                    nextMessage = nextMessage,
                    message = InternalMessageInstance(
                        isFromCache = message.isFromCache,
                        id = message.id,
                        sender = message.sender,
                        images = message.images,
                        read = message.read,
                        text = message.text,
                        timestamp = message.timestamp,
                        visible = message.visible,
                    ),
                    chatRoomId = chatRoomId,
                    onChatMateResponseReceived = { chatMateResponse ->
                        onChatMateResponseReceived(chatMateResponse)
                    }
                )

            }
        }

    }
}