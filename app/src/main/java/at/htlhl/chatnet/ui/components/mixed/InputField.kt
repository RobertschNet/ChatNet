package at.htlhl.chatnet.ui.components.mixed

import android.net.Uri
import android.util.Log
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
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.delay

@Composable
fun InputField(
    sharedViewModel: SharedViewModel,
    navController: NavController,
    chatMateChat: Boolean,
    onMessageSent: (String, List<String>) -> Unit
) {
    var badgeCount by remember { mutableIntStateOf(0) }
    val isLoading = remember {
        mutableStateOf(false)
    }
    val systemUiController = rememberSystemUiController()
    val text = sharedViewModel.text.value
    var chatMateResponseText by remember { mutableStateOf("ChatMate is thinking") }
    val chatMatePadding =
        if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) 10.dp else 0.dp
    val multiplePhotoPickerLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickMultipleVisualMedia(),
            onResult = { uris ->
                sharedViewModel.galleryImageList.value = uris
            })
    BottomAppBar(
        elevation = 10.dp,
        modifier = if (sharedViewModel.galleryImageList.value.isEmpty()) Modifier.height(70.dp + badgeCount.dp + chatMatePadding)
        else Modifier.height(160.dp + badgeCount.dp + chatMatePadding),
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
                                    width = 1.dp,
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
            }

            BasicTextField(
                // TODO  enabled = sharedViewModel.isConnected.value,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (text.isNotEmpty() || sharedViewModel.galleryImageList.value.isNotEmpty()) {
                        if (sharedViewModel.galleryImageList.value.isEmpty()) {
                            onMessageSent(text, listOf())
                        } else {
                            isLoading.value = true
                            uploadImage(sharedViewModel.galleryImageList.value) {
                                isLoading.value = false
                                sharedViewModel.galleryImageList.value = emptyList()
                                onMessageSent(text, it)
                            }
                        }
                        if (!sharedViewModel.user.value.blocked.contains(
                                sharedViewModel.friend.value.personList.id
                            )
                        ) {
                            sharedViewModel.text.value = ""
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
                onValueChange = { sharedViewModel.text.value = it },
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
                        width = 1.dp,
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
                                IconButton(enabled = !chatMateChat, onClick = {
                                    systemUiController.setStatusBarColor(
                                        color = Color.Black, darkIcons = false
                                    )
                                    navController.navigate(Screens.CameraViewScreen.route)
                                }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.camera_svgrepo_com_5_,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                IconButton(enabled = !chatMateChat, onClick = {
                                    multiplePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.gallery_svgrepo_com,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        colorFilter = ColorFilter.tint(Color.White),
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
                        if (text.isNotEmpty() || sharedViewModel.galleryImageList.value.isNotEmpty()) {
                            Text(text = "Send",
                                fontSize = 18.sp,
                                color = Color(0xFF00A0E8),
                                fontWeight = Bold,
                                modifier = Modifier.clickable {
                                    if (text.isNotEmpty() || sharedViewModel.galleryImageList.value.isNotEmpty()) {
                                        if (sharedViewModel.galleryImageList.value.isEmpty()) {
                                            onMessageSent(text, listOf())
                                        } else {
                                            isLoading.value = true
                                            uploadImage(sharedViewModel.galleryImageList.value) {
                                                isLoading.value = false
                                                sharedViewModel.galleryImageList.value =
                                                    emptyList()
                                                onMessageSent(text, it)
                                            }
                                        }
                                        if (!sharedViewModel.user.value.blocked.contains(
                                                sharedViewModel.friend.value.personList.id
                                            )
                                        ) {
                                            sharedViewModel.text.value = ""
                                        }
                                    }
                                })
                        } else {
                            IconButton(enabled = !chatMateChat, onClick = {
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.gallery_svgrepo_com,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                        }
                    }

                },
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

fun uploadImage(selectedImageUris: List<Uri>, onUploadSuccess: (List<String>) -> Unit) {
    val images = arrayListOf<String>()
    var successCount = 0
    selectedImageUris.forEach { image ->
        val storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${image.lastPathSegment}")
        val uploadTask = imageRef.putFile(image)

        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("Image", downloadUrl.toString())
                    images.add(downloadUrl.toString())

                    successCount++
                    if (successCount == selectedImageUris.size) {
                        Log.println(Log.INFO, "Image", images.toString())
                        onUploadSuccess(images)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Image", exception.toString())
                }
            } else {
                // Handle upload failure if needed
            }
        }
    }
}