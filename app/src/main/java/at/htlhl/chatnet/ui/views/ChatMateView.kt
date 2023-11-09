package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.ChatsViewBottomSheetContent
import at.htlhl.chatnet.ui.components.ChatsViewChatItem
import at.htlhl.chatnet.ui.components.ClearChatDialog
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

class ChatMateView {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun ChatMateTopBar(
        sharedViewModel: SharedViewModel,
        onClick: () -> Unit,
    ) {
        val isSearchMode = remember { mutableStateOf(false) }
        TopAppBar(
            backgroundColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isSearchMode.value) {
                Text(
                    text = "ChatMate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    fontFamily = FontFamily.Cursive,
                    modifier = Modifier.padding(start = 10.dp)
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                IconButton(
                    onClick = { isSearchMode.value = true },
                    modifier = Modifier.padding(start = 108.dp, top = 5.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = R.drawable.search_svgrepo_com_1_,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                }
                IconButton(
                    onClick = { onClick.invoke() },
                    modifier = Modifier.padding(top = 5.dp, end = 10.dp)
                ) {
                    Box(modifier = Modifier.size(50.dp)) {
                        SubcomposeAsyncImage(
                            model = R.drawable.add_circle_svgrepo_com,
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.Center),
                        )
                    }
                }
            } else {
                val (text, setText) = remember { mutableStateOf("") }
                val keyboardController = LocalSoftwareKeyboardController.current
                val interactionSource = remember { MutableInteractionSource() }
                val focusRequester = remember { FocusRequester() }
                val textFieldModifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(0.3f.dp, Color.DarkGray, RoundedCornerShape(36.dp))
                    .background(Color.White, RoundedCornerShape(36.dp))
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = {
                            setText(it)
                            sharedViewModel.searchValue.value = it
                        },
                        interactionSource = interactionSource,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        singleLine = true,
                        modifier = textFieldModifier.focusRequester(focusRequester),
                        textStyle = MaterialTheme.typography.body1.copy(color = Color.Black),
                        cursorBrush = SolidColor(if (isSystemInDarkTheme()) Color.White else Color.Black),
                        decorationBox = { innerTextField: @Composable () -> Unit ->
                            Text(
                                text = if (sharedViewModel.searchValue.value != "") "" else "Search...",
                                modifier = Modifier.padding(top = 9.dp, start = 50.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isSearchMode.value = false
                                        sharedViewModel.searchValue.value = ""
                                    },
                                ) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.back_svgrepo_com_1_,
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, end = 8.dp, start = 0.dp)
                                        .height(30.dp)
                                ) {
                                    innerTextField()
                                }
                            }
                        }
                    )
                }
                DisposableEffect(Unit) {
                    if (isSearchMode.value) {
                        keyboardController?.show()
                        focusRequester.requestFocus()
                    }
                    onDispose { }
                }
            }
        }
        Divider(
            thickness = 1.dp,
            color = if (isSystemInDarkTheme()) Color.DarkGray else Color.Transparent
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun ChatMateScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        Log.println(Log.INFO, "ChatMateView", "ChatMateScreen")
        val lazyListState = rememberLazyListState()
        val modelSheetState = remember { mutableStateOf(false) }
        val messageChatRoomDataState = sharedViewModel.completeChatMateList.collectAsState()
        val messageChatRoomData: List<InternalChatInstance> = messageChatRoomDataState.value
        var showClearChatPrompt by remember { mutableStateOf(false) }
        Log.println(Log.INFO, "ChatMateView", "messageChatRoomData: $messageChatRoomData")
        val bottomSheetItems = listOf(
            BottomSheetItem(
                title = if (sharedViewModel.friend.value.markedAsUnread || sharedViewModel.friend.value.read > 0) "Mark as Read" else "Mark as Unread",
                icon = if (sharedViewModel.friend.value.markedAsUnread || sharedViewModel.friend.value.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com,
                tag = "unread"
            ),
            BottomSheetItem(
                title = "Clear Chat", icon = R.drawable.comment_delete_svgrepo_com, tag = "clear"
            ),
            BottomSheetItem(
                title = if (sharedViewModel.friend.value.pinChat) "Unpin Chat" else "Pin Chat",
                icon = if (sharedViewModel.friend.value.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com,
                tag = "pin"
            ),
            BottomSheetItem(
                title = "Delete Chat", icon = R.drawable.garbage_bin_recycle_bin_svgrepo_com, tag = "delete"
            ),
        )
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { ChatMateTopBar(sharedViewModel) {sharedViewModel.createChatMateChat()} },
            content = {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
                    state = lazyListState
                ) {
                    items(messageChatRoomData) { message ->
                        ChatsViewChatItem(
                            chat = message,
                            displayOnlineState=false,
                            sharedViewModel = sharedViewModel,
                            navController = navController,
                        ) { context ->
                            when (context) {
                                "image" -> {
                                }

                                "message" -> {
                                    modelSheetState.value = true
                                }

                                "navigate" -> {
                                    navController.navigate(Screens.ChatViewScreen.route)
                                }
                            }
                            sharedViewModel.friend.value = message
                        }
                    }
                }
            },
        )
        if (showClearChatPrompt) {
            ClearChatDialog(onDismiss = { clear ->
                if (clear == "clear") {
                    sharedViewModel.deleteMessagesForUser()
                }
                showClearChatPrompt = false
            })
        }
        if (modelSheetState.value) {
            ModalBottomSheet(
                windowInsets = WindowInsets(0, 0, 0, 0),
                onDismissRequest = {
                    modelSheetState.value = false
                }, dragHandle = null, content = {
                    ChatsViewBottomSheetContent(
                        bottomSheetItems,
                        onItemClicked = { item ->
                            modelSheetState.value = false
                            when (item.tag) {
                                "unread" -> {
                                    if (sharedViewModel.friend.value.read > 0) {
                                        sharedViewModel.markMessagesAsRead(sharedViewModel.friend.value)
                                    } else if (sharedViewModel.friend.value.markedAsUnread && sharedViewModel.friend.value.read == 0) {
                                        sharedViewModel.updateMarkAsReadStatus(true)
                                    } else {
                                        sharedViewModel.updateMarkAsReadStatus(false)
                                    }
                                }

                                "clear" -> {
                                    showClearChatPrompt = true
                                }

                                "delete" -> {
                                    sharedViewModel.deleteChatRoom()
                                }

                                "pin" -> {
                                    if (sharedViewModel.friend.value.pinChat) {
                                        sharedViewModel.updatePinChatStatus(true)
                                    } else {
                                        sharedViewModel.updatePinChatStatus(false)
                                    }
                                }
                            }
                        },
                        friend = sharedViewModel.friend.value,
                    )
                }
            )
        }
    }
}