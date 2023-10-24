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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseMessages
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.firebase.Timestamp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@Composable
fun InputField(sharedViewModel: SharedViewModel, onMessageSent: (String, String) -> Unit) {
    var badgeCount by remember { mutableIntStateOf(0) }
    val text=sharedViewModel.text.value
    var selectedImageUris by remember {
        mutableStateOf<List<Uri>>(emptyList())
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImageUris = uris
            selectedImageUris.forEach { image ->
                sharedViewModel.updatePhotoCount(FirebaseMessages(sharedViewModel.auth.currentUser?.uid.toString(), "load", false, uris.toString(), Timestamp.now(),
                    listOf()))
                val storage = Firebase.storage
                val storageRef = storage.reference
                val imageRef =
                    storageRef.child("${sharedViewModel.auth.currentUser?.uid.toString()}/messagePictures/${image.lastPathSegment}")
                val uploadTask = imageRef.putFile(image)
                uploadTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            Log.d("Image", downloadUrl.toString())
                            sharedViewModel.removePhotoCount()
                            onMessageSent(downloadUrl.toString(), "image")
                        }.addOnFailureListener { exception ->
                            Log.e("Image", exception.toString())
                        }
                    }
                }
            }
        }
    )
    BasicTextField(
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
            onSend = {
                if (text.isNotEmpty()) {
                    onMessageSent(text, "text")
                    sharedViewModel.text.value = ""
                }
            }
        ),
        value = text,
        onTextLayout = { textLayoutResult ->
            when (textLayoutResult.lineCount) {
                1 -> {
                    badgeCount = 0
                }

                2 -> {
                    badgeCount = 12
                }

                3 -> {
                    badgeCount = 24
                }

                4 -> {
                    badgeCount = 36
                }
            }
        },
        maxLines = 4,
        cursorBrush = Brush.verticalGradient(
            0.00f to Color.White,
            0.35f to Color.White,
            0.35f to Color.White,
            0.90f to Color.White,
            0.90f to Color.White,
            1.00f to Color.White
        ),
        onValueChange = { sharedViewModel.text.value = it },
        textStyle = LocalTextStyle.current.copy(
            fontSize = 20.sp,
            color = if (isSystemInDarkTheme()) Color.White else Color.Black,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .background(
                if (isSystemInDarkTheme()) Color.Black else Color.White,
                RoundedCornerShape(26.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                shape = RoundedCornerShape(26.dp),
            ),
        decorationBox = { innerTextField ->
            Column(
                Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        modifier = Modifier
                            .size(30.dp)
                            .clickable(onClick = {}),
                        imageVector = Icons.Outlined.EmojiEmotions,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    )

                    Box(Modifier.padding(end = 95.dp)) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Message...",
                                fontSize = 20.sp,
                                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            )
                        }
                        innerTextField()

                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 12.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (text.isEmpty()) {
                        Icon(
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    multiplePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = null,
                            tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        )
                    } else {
                        Text(
                            text = "Send",
                            fontSize = 20.sp,
                            color = Color(0xFF00B1A9),
                            modifier = Modifier
                                .padding(top = badgeCount.dp)
                                .clickable {
                                    if (text.isNotEmpty()) {
                                        onMessageSent(text, "text")
                                        sharedViewModel.text.value = ""
                                    }
                                }
                        )
                    }
                }
            }
        },
    )
}