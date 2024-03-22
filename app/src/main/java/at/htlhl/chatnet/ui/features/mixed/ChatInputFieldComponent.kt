package at.htlhl.chatnet.ui.features.mixed

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.addImageUploadList
import at.htlhl.chatnet.data.getImageUploadList
import at.htlhl.chatnet.data.removeItemFromUploadList
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.util.checkAndRequestPermission
import at.htlhl.chatnet.util.createDisabledToastForInputField
import at.htlhl.chatnet.util.firebase.changeTypingStatus
import at.htlhl.chatnet.util.onMessageSentPressed
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatInputFieldComponent(
    coroutineScope: CoroutineScope,
    context: Context,
    userData: FirebaseUser,
    friendData: InternalChatInstance,
    chatMateResponseText: String,
    chatMateResponseState: ChatMateResponseState,
    navController: NavController,
    isChatMateChat: Boolean,
    onSentWhileBlocked: () -> Unit,
    onUpdateChatMateResponseState: (ChatMateResponseState) -> Unit,
) {
    var badgeCount by remember { mutableIntStateOf(0) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(chatMateResponseText) { text = chatMateResponseText }
    var chatMateLoadingText by remember { mutableStateOf("ChatMate is thinking") }
    val chatMatePadding =
        if (chatMateResponseState == ChatMateResponseState.Loading) 10.dp else 0.dp
    val multiplePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    addImageUploadList(friendData.chatRoomID, uris.map { uri -> uri })
                }
            })

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.saveState()
            navController.navigate(Screens.CameraFlow.route)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            multiplePhotoPickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        } else {
            Toast.makeText(context, "Gallery permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    BottomAppBar(
        elevation = 0.dp,
        modifier = if (getImageUploadList(id = friendData.chatRoomID).isEmpty()) Modifier.height(
            70.dp + badgeCount.dp + chatMatePadding
        ) else Modifier.height(
            160.dp + badgeCount.dp + chatMatePadding
        ),
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom
        ) {
            if (getImageUploadList(id = friendData.chatRoomID).isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                ) {
                    items(getImageUploadList(id = friendData.chatRoomID).size) { index ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .padding(5.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = getImageUploadList(id = friendData.chatRoomID)[index],
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                            )
                            Box(modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    val currentItem =
                                        getImageUploadList(id = friendData.chatRoomID)[index]
                                    removeItemFromUploadList(
                                        friendData.chatRoomID, currentItem
                                    )
                                }
                                .align(Alignment.TopEnd)) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.close_md_svgrepo_com,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.Center),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }
                    }
                }
            }
            if (!interactionSource.collectIsFocusedAsState().value && userData.typing.isNotEmpty()) {
                changeTypingStatus(
                    userId = userData.id, isTyping = false, chatRoomId = friendData.chatRoomID
                )
            }
            if (chatMateResponseState == ChatMateResponseState.Loading) {
                isLoading = true
                LaunchedEffect(chatMateResponseState) {
                    while (true) {
                        delay(750)
                        chatMateLoadingText = "ChatMate is thinking."
                        delay(750)
                        chatMateLoadingText = "ChatMate is thinking.."
                        delay(750)
                        chatMateLoadingText = "ChatMate is thinking..."
                    }
                }
                Text(
                    text = chatMateLoadingText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(start = 30.dp)
                )
            } else {
                isLoading = false
            }
            Card(
                elevation = 2.dp,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = if (isSystemInDarkTheme()) Color(0xFF141419) else Color(0xFFF3F4FA)
            ) {
                BasicTextField(
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (!isLoading) {
                            onMessageSentPressed(coroutineScope = coroutineScope,
                                userData = userData,
                                friendData = friendData,
                                chatMateChat = isChatMateChat,
                                context = context,
                                text = text,
                                onSentWhileBlocked = { onSentWhileBlocked.invoke() },
                                onIsLoading = { loadingState ->
                                    isLoading = loadingState
                                },
                                onSuccess = { text = "" },
                                onUpdateChatMateResponseState = { chatMateResponseState ->
                                    onUpdateChatMateResponseState(chatMateResponseState)
                                })
                        }
                    }),
                    value = text,
                    onTextLayout = { textLayoutResult ->
                        when {
                            textLayoutResult.lineCount >= 4 -> {
                                badgeCount = 36
                            }

                            textLayoutResult.lineCount == 3 -> {
                                badgeCount = 24
                            }

                            textLayoutResult.lineCount == 2 -> {
                                badgeCount = 12
                            }

                            textLayoutResult.lineCount == 1 -> {
                                badgeCount = 0
                            }
                        }
                    },
                    maxLines = 4,
                    interactionSource = interactionSource,
                    cursorBrush = Brush.linearGradient(
                        listOf(
                            Color(0xFF00A0E8), Color(0xFF00A0E8), Color(
                                0xFF0CB0FA
                            )
                        ), Offset.Zero, Offset.Infinite, TileMode.Repeated
                    ),
                    onValueChange = {
                        text = it
                        changeTypingStatus(
                            userId = userData.id,
                            isTyping = true,
                            chatRoomId = friendData.chatRoomID
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp + badgeCount.dp)
                        .background(
                            if (isSystemInDarkTheme()) Color(0xFF141419) else Color(0xFFF3F4FA),
                            RoundedCornerShape(26.dp)
                        ),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF00A0E8), Color(0xFF00A0E8), Color(
                                                    0xFF0CB0FA
                                                ), Color.White
                                            )
                                        ), RoundedCornerShape(24.dp)
                                    )
                            ) {
                                if (text.isEmpty()) {
                                    IconButton(onClick = {
                                        if (isChatMateChat) createDisabledToastForInputField(
                                            isCameraPressed = true, context = context
                                        ) else {
                                            if (checkAndRequestPermission(
                                                    context = context,
                                                    permission = android.Manifest.permission.CAMERA,
                                                    launcher = cameraLauncher
                                                )
                                            ) {
                                                navController.saveState()
                                                navController.navigate(Screens.CameraFlow.route)
                                            }
                                        }
                                    }) {
                                        SubcomposeAsyncImage(
                                            model = R.drawable.camera_svgrepo_com_5_,
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(Color.White.copy(alpha = if (isChatMateChat) 0.7f else 1f)),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    IconButton(onClick = {
                                        if (isChatMateChat) {
                                            createDisabledToastForInputField(
                                                isCameraPressed = false, context = context
                                            )
                                        } else if (!isLoading) {
                                            if (checkAndRequestPermission(
                                                    context = context,
                                                    permission = android.Manifest.permission.READ_MEDIA_IMAGES,
                                                    launcher = mediaLauncher
                                                )
                                            ) {
                                                multiplePhotoPickerLauncher.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        }
                                    }) {
                                        SubcomposeAsyncImage(
                                            model = R.drawable.gallery_svgrepo_com,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            colorFilter = ColorFilter.tint(Color.White.copy(alpha = if (isChatMateChat) 0.7f else 1f)),
                                        )
                                    }
                                }
                            }
                            Box(
                                Modifier.padding(start = 10.dp, end = 70.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        text = "Message...",
                                        textAlign = TextAlign.Start,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Normal,
                                    )
                                }
                                innerTextField()
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (text.isEmpty() && getImageUploadList(id = friendData.chatRoomID).isEmpty()) 6.dp else 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF00A0E8),
                                    strokeWidth = 4.dp,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(end = 2.dp)
                                )
                            } else {
                                if (text.isNotEmpty() || getImageUploadList(id = friendData.chatRoomID).isNotEmpty()) {
                                    Text(text = "Send",
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF00A0E8),
                                        fontWeight = Bold,
                                        modifier = Modifier.clickable {
                                            onMessageSentPressed(coroutineScope = coroutineScope,
                                                userData = userData,
                                                friendData = friendData,
                                                chatMateChat = isChatMateChat,
                                                context = context,
                                                text = text,
                                                onSentWhileBlocked = { onSentWhileBlocked.invoke() },
                                                onIsLoading = { loadingState ->
                                                    isLoading = loadingState
                                                },
                                                onSuccess = { text = "" },
                                                onUpdateChatMateResponseState = { chatMateResponseState ->
                                                    onUpdateChatMateResponseState(
                                                        chatMateResponseState
                                                    )
                                                })
                                        })
                                } else {
                                    IconButton(onClick = {
                                        if (isChatMateChat) {
                                            createDisabledToastForInputField(
                                                isCameraPressed = false, context = context
                                            )
                                        } else {
                                            if (checkAndRequestPermission(
                                                    context = context,
                                                    permission = android.Manifest.permission.READ_MEDIA_IMAGES,
                                                    launcher = mediaLauncher
                                                )
                                            ) {
                                                multiplePhotoPickerLauncher.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                        }
                                    }) {
                                        SubcomposeAsyncImage(
                                            model = R.drawable.gallery_svgrepo_com,
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(
                                                MaterialTheme.colorScheme.primary.copy(
                                                    alpha = if (isChatMateChat) 0.7f else 1f
                                                )
                                            ),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}