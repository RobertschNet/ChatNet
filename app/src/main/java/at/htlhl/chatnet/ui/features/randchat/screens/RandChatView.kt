package at.htlhl.chatnet.ui.features.randchat.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.dialogs.UnblockToMessageDialog
import at.htlhl.chatnet.ui.features.mixed.ChatInputFieldComponent
import at.htlhl.chatnet.ui.features.mixed.ChatViewTopBar
import at.htlhl.chatnet.ui.features.randchat.components.RandChatContentComponent
import at.htlhl.chatnet.ui.features.randchat.components.RandChatLoadingViewComponent
import at.htlhl.chatnet.util.cloudfunctions.requestRandChatPairingPartner
import at.htlhl.chatnet.util.firebase.updateBlockedUserList
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

class RandChatView {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RandChatScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        var chatMateResponse by remember { mutableStateOf("") }
        var unblockDialog by remember { mutableStateOf(false) }
        val lazyColumnState = rememberLazyListState()
        val pageState = rememberPagerState { 2 }
        var currentIndex = 0

        val randState = sharedViewModel.randState.value
        val isConnected = sharedViewModel.isConnected.value
        val chatMateResponseState = sharedViewModel.chatMateResponseState.value
        val searchedValue = sharedViewModel.searchValue.value

        val friendDataState by sharedViewModel.friend.collectAsState()
        val userDataState by sharedViewModel.userData.collectAsState()
        val currentRandChatState by sharedViewModel.currentRandChat.collectAsState()

        val friendData: InternalChatInstance = friendDataState
        val userData: FirebaseUser = userDataState
        val currentRandChat: FirebaseChat = currentRandChatState

        if (pageState.currentPage == 1 && !pageState.isScrollInProgress) {
            sharedViewModel.updateRandState(newState = false)
            LaunchedEffect(pageState.currentPage) {
                coroutineScope.launch { pageState.scrollToPage(0) }
            }
            requestRandChatPairingPartner(
                userID = userData.id,
                requestState = true,
                navController = navController,
                sharedViewModel = sharedViewModel,
            )
        }
        HorizontalPager(
            state = pageState, beyondBoundsPageCount = 1, userScrollEnabled = randState
        ) { page ->
            when (page) {
                0 -> {
                    if (randState) {
                        if (!isConnected) {
                            LaunchedEffect(Unit) {
                                Toast.makeText(
                                    context,
                                    "User left\nSwipe right to search for another user",
                                    Toast.LENGTH_SHORT
                                ).show()
                                coroutineScope.launch {
                                    pageState.scrollToPage(1)
                                }
                            }
                        }
                        val chatRoomId = currentRandChat.chatRoomID
                        val messageListFromMatchingChat: List<InternalMessageInstance> =
                            currentRandChat.let { chat ->
                                chat.messages.map { message ->
                                    InternalMessageInstance(
                                        isFromCache = message.isFromCache,
                                        id = message.id,
                                        sender = message.sender,
                                        images = message.images,
                                        read = message.read,
                                        text = message.text,
                                        timestamp = message.timestamp,
                                        visible = message.visible,
                                    )
                                }
                            }
                        val filteredMessages = messageListFromMatchingChat.filter { message ->
                            message.visible.contains(userData.id)
                        }.toMutableList()
                        Scaffold(modifier = Modifier.imePadding(), topBar = {
                            ChatViewTopBar(userData = userData,
                                chatMateResponseState = chatMateResponseState,
                                friendData = friendData,
                                onTopBarUserElementClicked = { userElementClicked ->
                                    if (userElementClicked) {
                                        navController.navigate(Screens.UserSheetScreen.route)
                                    } else {
                                        navController.navigateUp()
                                    }
                                },
                                onNavigateBetweenSearchedValues = {
                                    coroutineScope.launch {
                                        val indexes =
                                            filteredMessages.mapIndexedNotNull { index, message ->
                                                if (message.text.contains(
                                                        searchedValue, ignoreCase = true
                                                    )
                                                ) {
                                                    index
                                                } else {
                                                    null
                                                }
                                            }
                                        if (indexes.isNotEmpty()) {
                                            currentIndex = if (it) {
                                                (currentIndex + 1)
                                            } else {
                                                (currentIndex - 1)
                                            }
                                            if (currentIndex >= indexes.size) {
                                                currentIndex = 0
                                            } else if (currentIndex < 0) {
                                                currentIndex = indexes.size - 1
                                            }

                                            lazyColumnState.animateScrollToItem(indexes[currentIndex])
                                        }
                                        if (indexes.isEmpty()) {
                                            Toast.makeText(
                                                context, "No results", Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (indexes.size < 2) {
                                            Toast.makeText(
                                                context, "No more results", Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                    }
                                },
                                onUpdateSearchValue = { updatedValue ->
                                    sharedViewModel.updateSearchValue(newSearchValue = updatedValue)
                                })
                        }, content = { paddingValues ->
                            RandChatContentComponent(paddingValues = paddingValues,
                                sharedViewModel = sharedViewModel,
                                chatMateChat = false,
                                messages = filteredMessages,
                                userData = userData,
                                lazyListState = lazyColumnState,
                                chatRoomId = chatRoomId,
                                onChatMateResponseReceived = { responseText ->
                                    chatMateResponse = responseText
                                }

                            )
                        }, bottomBar = {
                            ChatInputFieldComponent(userData = userData,
                                friendData = friendData,
                                chatMateResponseText = chatMateResponse,
                                chatMateResponseState = chatMateResponseState,
                                isRandChat = true,
                                chatRoomID = currentRandChat.chatRoomID,
                                coroutineScope = coroutineScope,
                                context = context,
                                navController = navController,
                                isChatMateChat = false,
                                onUpdateChatMateResponseState = { chatMateResponseState ->
                                    sharedViewModel.updateChatMateResponseState(
                                        newChatMateResponseState = chatMateResponseState
                                    )
                                },
                                onSentWhileBlocked = {
                                    unblockDialog = true

                                })
                        })
                        if (unblockDialog) {
                            UnblockToMessageDialog(
                                friendData = friendData,
                            ) { unblockPressed ->
                                if (unblockPressed) {
                                    updateBlockedUserList(
                                        userData = userData,
                                        friendData = friendData.personList,
                                        isAlreadyBlocked = true
                                    )
                                }
                                unblockDialog = false
                            }
                        }
                    } else {
                        RandChatLoadingViewComponent(navController = navController)
                    }
                }

                1 -> {
                    RandChatLoadingViewComponent(navController = navController)
                }
            }
        }
    }
}