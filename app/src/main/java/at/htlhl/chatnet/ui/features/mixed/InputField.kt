package at.htlhl.chatnet.ui.features.mixed

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.addImageUploadList
import at.htlhl.chatnet.data.getImageUploadList
import at.htlhl.chatnet.data.removeImageUploadList
import at.htlhl.chatnet.data.removeItemFromUploadList
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
    chatPartner: InternalChatInstance,
    onChatMateResponse: String,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    chatMateChat: Boolean,
    onSentWhileBlocked: () -> Unit
) {
    var badgeCount by remember { mutableIntStateOf(0) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var text by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(onChatMateResponse) { text = onChatMateResponse }
    var chatMateResponseText by remember { mutableStateOf("ChatMate is thinking") }
    val chatMatePadding =
        if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) 10.dp else 0.dp
    val multiplePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                if (uris.isNotEmpty()) {
                    addImageUploadList(
                        sharedViewModel.friend.value.chatRoomID,
                        uris.map { uri -> uri })
                }
            }
        )

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navController.saveState()
            navController.navigate("CameraFlow")
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
        modifier = if (getImageUploadList(chatPartner.chatRoomID).isEmpty()) Modifier.height(
            70.dp + badgeCount.dp + chatMatePadding
        ) else Modifier.height(
            160.dp + badgeCount.dp + chatMatePadding
        ),
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (getImageUploadList(chatPartner.chatRoomID).isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, end = 5.dp)
                ) {
                    items(getImageUploadList(chatPartner.chatRoomID).size) { index ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .padding(5.dp)
                        ) {
                            SubcomposeAsyncImage(
                                model = getImageUploadList(chatPartner.chatRoomID)[index],
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
                                        getImageUploadList(chatPartner.chatRoomID)[index]
                                    removeItemFromUploadList(
                                        chatPartner.chatRoomID,
                                        currentItem
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
                            onSentPressed(sharedViewModel, chatPartner, chatMateChat, context, text,
                                { onSentWhileBlocked.invoke() },
                                { isLoading = it },
                                { text = "" }
                            )
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
                            )
                        ), Offset.Zero, Offset.Infinite, TileMode.Repeated
                    ),
                    onValueChange = { text = it },
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
                                        if (chatMateChat) createToast(
                                            true,
                                            context
                                        ) else {
                                            if (checkAndRequestPermission(
                                                    context,
                                                    android.Manifest.permission.CAMERA,
                                                    cameraLauncher
                                                )
                                            ) {
                                                navController.saveState()
                                                navController.navigate("CameraFlow")
                                            }
                                        }
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
                                            if (checkAndRequestPermission(
                                                    context,
                                                    android.Manifest.permission.READ_MEDIA_IMAGES,
                                                    mediaLauncher
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
                                            colorFilter = ColorFilter.tint(Color.White.copy(alpha = if (chatMateChat) 0.7f else 1f)),
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
                                .padding(if (text.isEmpty() && getImageUploadList(chatPartner.chatRoomID).isEmpty()) 6.dp else 12.dp),
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
                                if (text.isNotEmpty() || getImageUploadList(chatPartner.chatRoomID).isNotEmpty()) {
                                    Text(text = "Send",
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFF00A0E8),
                                        fontWeight = Bold,
                                        modifier = Modifier.clickable {
                                            onSentPressed(sharedViewModel,
                                                chatPartner,
                                                chatMateChat,
                                                context,
                                                text,
                                                { onSentWhileBlocked.invoke() },
                                                { isLoading = it },
                                                { text = "" }
                                            )
                                        }
                                    )
                                } else {
                                    IconButton(onClick = {
                                        if (chatMateChat) {
                                            createToast(false, context)
                                        } else {
                                            if (checkAndRequestPermission(
                                                    context,
                                                    android.Manifest.permission.READ_MEDIA_IMAGES,
                                                    mediaLauncher
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
            }
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
    val list = arrayListOf<String>()
    selectedImageUris.forEach { imageUri ->
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val webpByteArray = convertBitmapToWebP(bitmap)
        uploadWebPImage(webpByteArray, {
            list.addAll(it)
            if (list.size == selectedImageUris.size) onUploadSuccess.invoke(list)
        }, onUploadError)
    }
}

@Suppress("DEPRECATION")
private fun convertBitmapToWebP(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
    return outputStream.toByteArray()
}

private fun uploadWebPImage(
    webpByteArray: ByteArray,
    onUploadSuccess: (List<String>) -> Unit,
    onUploadError: () -> Unit
) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val webpImageRef = storageRef.child("chats/${Timestamp.now().seconds}.webp")

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
    Log.println(Log.INFO, "Message", "Message sending")
    Log.println(Log.INFO, "Message", sharedViewModel.friend.value.chatRoomID)
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
        sharedViewModel.sendDataToServer(text) {
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

fun onSentPressed(
    sharedViewModel: SharedViewModel,
    chatPartner: InternalChatInstance,
    chatMateChat: Boolean,
    context: Context,
    text: String,
    onSentWhileBlocked: () -> Unit,
    isLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {
    if (!sharedViewModel.user.value.blocked.contains(chatPartner.personList.id)) {
        if (text.isNotEmpty() || getImageUploadList(chatPartner.chatRoomID).isNotEmpty()) {
            if (getImageUploadList(chatPartner.chatRoomID).isEmpty()) {
                uploadMessage(chatMateChat, sharedViewModel, text) {
                    if (it) {
                        onSuccess.invoke()
                    } else {
                        //TODO Show error message
                    }
                }
            } else {
                isLoading.invoke(true)
                uploadImage(
                    context,
                    getImageUploadList(chatPartner.chatRoomID),
                    { success ->
                        removeImageUploadList(chatPartner.chatRoomID)
                        uploadMessage(
                            chatMateChat,
                            sharedViewModel,
                            text,
                            success
                        ) {
                            isLoading.invoke(false)
                            if (it) {
                                removeImageUploadList(chatPartner.chatRoomID)
                                onSuccess.invoke()
                            } else {
                                //TODO Show error message
                            }
                        }
                    },
                    {
                        isLoading.invoke(false)
                        //TODO Show error message
                    }
                )
            }
        }
    } else {
        onSentWhileBlocked.invoke()
    }

}

private fun checkAndRequestPermission(
    context: Context,
    permission: String,
    launcher: ManagedActivityResultLauncher<String, Boolean>
): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    return if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
        true
    } else {
        // Request a permission
        launcher.launch(permission)
        false
    }
}