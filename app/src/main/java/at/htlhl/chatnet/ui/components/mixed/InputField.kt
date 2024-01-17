package at.htlhl.chatnet.ui.components.mixed

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream

@Composable
fun InputField(
    onChatMateResponse: String,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    chatMateChat: Boolean,
    onSentWhileBlocked: () -> Unit
) {
    Log.println(Log.INFO, "InputField", chatMateChat.toString())
    var badgeCount by remember { mutableIntStateOf(0) }
    var isLoading by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    var text by rememberSaveable {
        mutableStateOf("")
    }
    if (onChatMateResponse.isNotEmpty()) {
        text = onChatMateResponse
    }
    var chatMateResponseText by remember { mutableStateOf("ChatMate is thinking") }
    val chatMatePadding =
        if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) 10.dp else 0.dp
    val multiplePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                sharedViewModel.galleryImageList.value = uris
            })
    BottomAppBar(
        elevation = 0.dp,
        modifier = if (sharedViewModel.galleryImageList.value.isEmpty()) Modifier.height(70.dp + badgeCount.dp + chatMatePadding) else Modifier.height(
            160.dp + badgeCount.dp + chatMatePadding
        ),
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (sharedViewModel.galleryImageList.value.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                ) {
                    items(sharedViewModel.galleryImageList.value.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .padding(5.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = sharedViewModel.galleryImageList.value[index],
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
                                    sharedViewModel.galleryImageList.value =
                                        sharedViewModel.galleryImageList.value.filterIndexed { i, _ -> i != index }
                                }
                                .border(
                                    width = Dp.Hairline,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .align(Alignment.TopEnd)) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.close_svgrepo_com,
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
            if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                isLoading = true
                LaunchedEffect(sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                    while (true) {
                        delay(750)
                        chatMateResponseText = "ChatMate is thinking."
                        delay(750)
                        chatMateResponseText = "ChatMate is thinking.."
                        delay(750)
                        chatMateResponseText = "ChatMate is thinking..."
                    }
                }
                Text(
                    text = chatMateResponseText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    color = Color.Black,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(start = 30.dp)
                )
            } else {
                isLoading = false
            }
            BasicTextField(
                // TODO  enabled = sharedViewModel.isConnected.value,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (!isLoading) {
                        if (!sharedViewModel.user.value.blocked.contains(sharedViewModel.friend.value.personList.id)) {
                            if (text.isNotEmpty() || sharedViewModel.galleryImageList.value.isNotEmpty()) {
                                isLoading = true
                                if (sharedViewModel.galleryImageList.value.isEmpty()) {
                                    uploadMessage(chatMateChat, sharedViewModel, text) {
                                        isLoading = false
                                        if (it) {
                                            text = ""
                                        } else {
                                            //TODO Show error message
                                        }
                                    }
                                    text = ""
                                } else {
                                    uploadImage(
                                        context,
                                        sharedViewModel.galleryImageList.value,
                                        { success ->
                                            sharedViewModel.galleryImageList.value = emptyList()
                                            uploadMessage(
                                                chatMateChat,
                                                sharedViewModel,
                                                text,
                                                success
                                            ) {
                                                isLoading = false
                                                if (it) {
                                                    sharedViewModel.galleryImageList.value =
                                                        emptyList()
                                                    text = ""
                                                } else {
                                                    //TODO Show error message
                                                }
                                            }
                                        },
                                        {
                                            isLoading = false
                                            //TODO Show error message
                                        })
                                    text = ""
                                }
                            }
                        } else{
                            onSentWhileBlocked.invoke()
                        }
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
                cursorBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00A0E8), Color(0xFF00A0E8), Color(
                            0xFF0CB0FA
                        ), Color.White
                    ), Offset.Zero, Offset.Infinite, TileMode.Clamp
                ),
                onValueChange = { text = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp + badgeCount.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .background(
                        MaterialTheme.colorScheme.background, RoundedCornerShape(26.dp)
                    )
                    .border(
                        width = Dp.Hairline,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(26.dp),
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
                                    navController.navigate(Screens.CameraViewScreen.route)
                                }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.camera_svgrepo_com_5_,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = if (chatMateChat) 0.7f else 1f)),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(onClick = {
                                    if (chatMateChat) {
                                        createToast(false, context)
                                    } else if (!isLoading) {
                                        multiplePhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.gallery_svgrepo_com,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = if (chatMateChat) 0.7f else 1f)),
                                    )
                                }
                            }
                        }
                        Box(Modifier.padding(start = 10.dp, end = 70.dp)) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Message...",
                                    textAlign = TextAlign.Start,
                                    fontSize = 18.sp,
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
                            .padding(if (text.isEmpty() && sharedViewModel.galleryImageList.value.isEmpty()) 6.dp else 12.dp),
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
                            if (text.isNotEmpty() || sharedViewModel.galleryImageList.value.isNotEmpty()) {
                                Text(text = "Send",
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFF00A0E8),
                                    fontWeight = Bold,
                                    modifier = Modifier.clickable {
                                        if (!sharedViewModel.user.value.blocked.contains(sharedViewModel.friend.value.personList.id)) {
                                            if (text.isNotEmpty() || sharedViewModel.galleryImageList.value.isNotEmpty()) {
                                                isLoading = true
                                                if (sharedViewModel.galleryImageList.value.isEmpty()) {
                                                    uploadMessage(
                                                        chatMateChat,
                                                        sharedViewModel,
                                                        text
                                                    ) {
                                                        isLoading = false
                                                        if (it) {
                                                        } else {
                                                            //TODO Show error message
                                                        }
                                                    }
                                                    text = ""
                                                } else {
                                                    uploadImage(
                                                        context,
                                                        sharedViewModel.galleryImageList.value,
                                                        { success ->
                                                            sharedViewModel.galleryImageList.value =
                                                                emptyList()
                                                            uploadMessage(
                                                                chatMateChat,
                                                                sharedViewModel,
                                                                text,
                                                                success
                                                            ) {
                                                                isLoading = false
                                                                if (it) {
                                                                    sharedViewModel.galleryImageList.value =
                                                                        emptyList()
                                                                    text = ""
                                                                } else {
                                                                    //TODO Show error message
                                                                }
                                                            }
                                                        },
                                                        {
                                                            isLoading = false
                                                            //TODO Show error message
                                                        })
                                                    text = ""
                                                }
                                            }
                                        } else{
                                            onSentWhileBlocked.invoke()
                                        }
                                    }
                                )
                            } else {
                                IconButton(onClick = {
                                    if (chatMateChat) createToast(
                                        false,
                                        context
                                    ) else multiplePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.gallery_svgrepo_com,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = if (chatMateChat) 0.7f else 1f
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
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

fun createToast(camera: Boolean, context: Context) {
    Log.println(Log.INFO, "Camera", "Permission denied")
    Toast.makeText(
        context,
        if (camera) "Camera not available" else "Gallery not available",
        Toast.LENGTH_SHORT
    ).show()
}

fun uploadImage(
    context: Context,
    selectedImageUris: List<Uri>,
    onUploadSuccess: (List<String>) -> Unit,
    onUploadError: () -> Unit
) {
    runBlocking {
        selectedImageUris.forEach { imageUri ->
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val webpByteArray = convertBitmapToWebP(bitmap)
            uploadWebPImage(webpByteArray, onUploadSuccess, onUploadError)
        }
    }
}

private fun convertBitmapToWebP(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
    return outputStream.toByteArray()
}

private fun uploadWebPImage(
    webpByteArray: ByteArray,
    onUploadSuccess: (List<String>) -> Unit,
    onUploadError: () -> Unit
) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val webpImageRef = storageRef.child("images/${webpByteArray.hashCode()}\"")

    webpImageRef.putBytes(webpByteArray)
        .addOnSuccessListener {
            webpImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onUploadSuccess.invoke(listOf(downloadUrl.toString()))
            }.addOnFailureListener {
                onUploadError.invoke()
            }
        }
        .addOnFailureListener {
            onUploadError.invoke()
        }
}

fun uploadMessage(
    chatMateChat: Boolean,
    sharedViewModel: SharedViewModel,
    text: String,
    images: List<String> = arrayListOf(),
    onSuccess: (Boolean) -> Unit
) {
    runBlocking {
        sharedViewModel.saveMessages(
            documentId = sharedViewModel.friend.value.chatRoomID,
            message = FirebaseMessage(
                sender = sharedViewModel.auth.currentUser?.uid.toString(),
                text = text,
                timestamp = Timestamp.now(),
                read = false,
                images = images,
                visible =
                listOf(
                    sharedViewModel.friend.value.personList.id,
                    sharedViewModel.auth.currentUser?.uid.toString(),
                )
            ),
            {
                onSuccess.invoke(true)
                Log.println(Log.INFO, "Message", "Message sent")
            },
            {
                onSuccess.invoke(false)
                Log.println(Log.INFO, "Message", "Message not sent")
            }
        )
    }
    //TODO: ChatMate loading when offline
    if (chatMateChat) {
        sharedViewModel.chatMateResponseState.value = ChatMateResponseState.Loading
        sharedViewModel.sendDataToServer(text){
            runBlocking {
                sharedViewModel.saveMessages(
                    documentId = sharedViewModel.friend.value.chatRoomID,
                    message = FirebaseMessage(
                        sender = "chatmate",
                        text = it,
                        timestamp = Timestamp.now(),
                        read = false,
                        images = arrayListOf(),
                        visible =
                        listOf(
                            sharedViewModel.auth.currentUser?.uid.toString(),
                        )
                    ),
                    {
                        onSuccess.invoke(true)
                    },
                    {
                        onSuccess.invoke(false)
                    }
                )
            }
        }
    }
}