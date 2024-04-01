package at.htlhl.chatnet.ui.features.chat.components

import android.content.Context
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
import androidx.navigation.NavController
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.chat.viewmodels.ChatViewModel
import at.htlhl.chatnet.util.isBlockSeparatorNeeded
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun ChatViewContentComponent(
    chatViewModel: ChatViewModel,
    sharedViewModel: SharedViewModel,
    userData: FirebaseUser,
    context: Context,
    friendData: InternalChatInstance,
    paddingValues: PaddingValues,
    imageList: List<InternalMessageInstance>,
    navController: NavController,
    isChatMateChat: Boolean,
    messages: List<InternalMessageInstance>,
    lazyColumnState: LazyListState,
    chatRoomId: String,
    chatMateResponseState: ChatMateResponseState,
    onChatMateResponseReceived: (String) -> Unit
) {
    var animatedText by remember { mutableStateOf("ChatMate is thinking") }
    var unblockDialog by remember { mutableStateOf(false) }
    LaunchedEffect(chatMateResponseState == ChatMateResponseState.Loading) {
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
        lazyColumnState.animateScrollToItem(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        LazyColumn(
            reverseLayout = true, modifier = Modifier.padding(
                bottom = paddingValues.calculateBottomPadding(),
                top = paddingValues.calculateTopPadding()
            ), state = lazyColumnState
        ) {
            item {
                if (isBlockSeparatorNeeded(userData = userData, friendData = friendData)) {
                    BlockedUserIndicatorComponent(onClicked = {
                        unblockDialog = true
                    })
                }
            }
            items(
                key = { it.id }, items = messages
            ) { message ->
                val messageIndex = messages.indexOf(message)
                val previousMessage = messages.getOrNull(messageIndex + 1)
                val nextMessage = messages.getOrNull(messageIndex - 1)
                ChatViewMessageStructureComponent(sharedViewModel = sharedViewModel,
                    chatMateChat = isChatMateChat,
                    userData = userData,
                    context = context,
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
                    onImageClicked = { image ->
                        sharedViewModel.updateImageList(newImageList = chatViewModel.createImageList(
                            messages = messages
                        ), onComplete = {
                            sharedViewModel.updateImageStartPosition(newImageStartPosition = imageList.find { it.images[0] == image }
                                ?.let { imageList.indexOf(it) } ?: 0, onComplete = {
                                navController.navigate(Screens.ImageViewScreen.route)
                            })
                        })
                    },
                    onChatMateResponseReceived = { responseText ->
                        onChatMateResponseReceived(responseText)
                    })
            }
        }
    }
}