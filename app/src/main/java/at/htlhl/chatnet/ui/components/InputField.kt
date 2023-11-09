package at.htlhl.chatnet.ui.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@Composable
fun InputField(sharedViewModel: SharedViewModel, navController: NavController, chatMateChat:Boolean, onMessageSent: (String, String) -> Unit) {
    var badgeCount by remember { mutableIntStateOf(0) }
    val systemUiController = rememberSystemUiController()
    val text = sharedViewModel.text.value
    var selectedImageUris by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImageUris = uris
            selectedImageUris.forEach { image ->
                val storage = Firebase.storage
                val storageRef = storage.reference
                val imageRef =
                    storageRef.child("images/${image.lastPathSegment}")
                val uploadTask = imageRef.putFile(image)
                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            Log.d("Image", downloadUrl.toString())
                            onMessageSent("", downloadUrl.toString())
                        }.addOnFailureListener { exception ->
                            Log.e("Image", exception.toString())
                        }
                    }
                }
            }
        }
    )
    Column {
        BasicTextField(
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotEmpty()) {
                        onMessageSent(text, "")
                        sharedViewModel.text.value = ""
                    }
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
                    Color(0xFF00A0E8), Color(0xFF00A0E8), Color(
                        0xFF0CB0FA
                    ), Color.White
                ), Offset.Zero, Offset.Infinite, TileMode.Clamp
            ),
            onValueChange = { sharedViewModel.text.value = it },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                color = Color.Gray,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp + badgeCount.dp)
                .padding(start = 10.dp, end = 10.dp)
                .background(
                    if (isSystemInDarkTheme()) Color.Black else Color.White,
                    RoundedCornerShape(26.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
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
                            IconButton(
                                enabled= !chatMateChat,
                                onClick = {
                                systemUiController.setStatusBarColor(
                                    color = Color.Black,
                                    darkIcons = false
                                )
                                navController.navigate(Screens.CameraViewScreen.route) }) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.camera_svgrepo_com_5_,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(Color.White),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            IconButton(
                                enabled= !chatMateChat,
                                onClick = {
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
                                color = Color.Gray,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                            )
                        }
                        innerTextField()
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (text.isEmpty()) 6.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (text.isNotEmpty()) {
                        Text(
                            text = "Send",
                            fontSize = 18.sp,
                            color = Color(0xFF00A0E8),
                            fontWeight = Bold,
                            modifier = Modifier
                                .clickable {
                                    if (text.isNotEmpty()) {
                                        onMessageSent(text, "")
                                        sharedViewModel.text.value = ""
                                    }
                                }
                        )
                    } else {
                        IconButton(
                            enabled = !chatMateChat,
                            onClick = {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) {
                            SubcomposeAsyncImage(
                                model = R.drawable.gallery_svgrepo_com,
                                contentDescription = null,
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