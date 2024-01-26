package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CameraPhotoView {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun CameraPhotoScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val bitmap = sharedViewModel.bitmaps.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val chatDataState = sharedViewModel.chatData.collectAsState(initial = emptyList())
        val chatData: List<FirebaseChat> = chatDataState.value
        val filteredChats = chatData.find { chat ->
            chat.members.contains(sharedViewModel.friend.value.personList.id) && chat.members.contains(
                sharedViewModel.auth.currentUser?.uid.toString()
            )
        }
        val chatRoomId = filteredChats?.chatRoomID ?: ""
        Scaffold(
            contentColor = Color.Black,
            backgroundColor = Color.Black,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            bottomBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                ) {
                    TextFieldCameraPhotoView(
                        sharedViewModel = sharedViewModel,
                        navController = navController,
                        onMessageSent = { text ->
                            val storage = Firebase.storage
                            val storageRef = storage.reference
                            val cachePath = File(context.cacheDir, "tempImage.webp")
                            cachePath.createNewFile()
                            val outputStream = FileOutputStream(cachePath)
                            bitmap.value.compress(
                                Bitmap.CompressFormat.WEBP,
                                50,
                                outputStream
                            )
                            outputStream.close()
                            val imageRef =
                                storageRef.child("images/${System.currentTimeMillis()}") // Not safe
                            val uploadTask = imageRef.putFile(Uri.fromFile(cachePath))
                            uploadTask.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                        Log.d("Image", "Download URL: $downloadUrl")
                                        Log.println(
                                            Log.INFO,
                                            "Image",
                                            "Download URL: $chatRoomId"
                                        )
                                        coroutineScope.launch {
                                            sharedViewModel.saveMessages(
                                                chatRoomId, FirebaseMessage(
                                                    sender = sharedViewModel.auth.currentUser?.uid.toString(),
                                                    text = text,
                                                    timestamp = Timestamp.now(),
                                                    read = false,
                                                    images = arrayListOf(downloadUrl.toString()),
                                                    visible = arrayListOf(
                                                        sharedViewModel.friend.value.personList.id,
                                                        sharedViewModel.auth.currentUser?.uid.toString()
                                                    ),
                                                )
                                            ) {
                                            }
                                        }
                                        navController.navigate(Screens.ChatViewScreen.route) {
                                            popUpTo(Screens.ChatViewScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                        sharedViewModel.text.value = ""
                                    }.addOnFailureListener { exception ->
                                        Log.e(
                                            "Image",
                                            "Failed to get download URL. Exception: $exception"
                                        )
                                    }
                                } else {
                                    Log.e(
                                        "Image",
                                        "Image upload failed. Task exception: ${task.exception}"
                                    )
                                }
                            }
                        },
                        chatMateChat = false
                    )
                }
            },
            content = {
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigateUp()

                            },
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        IconButton(
                            onClick = {
                                sharedViewModel.saveBitmapToGallery(
                                    bitmap = bitmap.value,
                                    displayName = "ChatNet${Timestamp.now()}",
                                    context = context
                                ) {
                                    Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    }
                    Image(
                        bitmap = bitmap.value.asImageBitmap(),
                        contentDescription = "Camera Photo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )

                }

            }
        )

    }

    @Composable
    fun TextFieldCameraPhotoView(
        sharedViewModel: SharedViewModel,
        navController: NavController,
        onMessageSent: (String) -> Unit,
        chatMateChat: Boolean
    ) {
        val text = sharedViewModel.text.value
        var badgeCount by remember { mutableIntStateOf(0) }
        var isLoading by remember { mutableStateOf(false) }
        BottomAppBar(
            elevation = 0.dp,
            modifier = Modifier
                .height(70.dp + badgeCount.dp)
                .padding(bottom = 10.dp, top = 10.dp),
            backgroundColor = Color.Transparent,
        ) {
            BasicTextField(
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        isLoading = true
                        onMessageSent(text)
                    }
                ),
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
                        Color.White, Color.White
                    ), Offset.Zero, Offset.Infinite, TileMode.Clamp
                ),
                onValueChange = { sharedViewModel.text.value = it },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp + badgeCount.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .background(
                        Color(0xFF2B2D30).copy(alpha = 0.6f),
                        RoundedCornerShape(26.dp)
                    ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                        ) {
                            IconButton(
                                enabled = !chatMateChat,
                                onClick = {
                                    navController.navigateUp()
                                }) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.image_redo_svgrepo_com,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(Color.White),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                        }
                        Box(Modifier.padding(start = 10.dp, end = 70.dp)) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Add Caption ...",
                                    textAlign = TextAlign.Start,
                                    fontSize = 18.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                            innerTextField()
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(12.dp),
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
                            Text(
                                text = "Send",
                                fontSize = 18.sp,
                                color = Color(0xFF00A0E8),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isLoading = true
                                        onMessageSent(text)

                                    }
                            )
                        }

                    }
                },
            )
        }
    }
}
