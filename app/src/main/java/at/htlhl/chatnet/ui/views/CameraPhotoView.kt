package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.Timestamp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CameraPhotoView {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
        val systemUiController = rememberSystemUiController()

        Scaffold(
            contentColor = Color.Black,
            containerColor = Color.Black,
            modifier = Modifier.fillMaxSize(),
            content = {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .weight(1f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.background(Color.Red, RoundedCornerShape(100))) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    tint = Color.White,
                                    contentDescription = "Close Camera"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.background(Color.Green, RoundedCornerShape(100))) {
                            IconButton(
                                onClick = {
                                    systemUiController.setStatusBarColor(
                                        color = Color.Transparent,
                                        darkIcons = true
                                    )
                                    navController.navigate(Screens.ChatViewScreen.route)
                                    val storage = Firebase.storage
                                    val storageRef = storage.reference
                                    val cachePath = File(context.cacheDir, "tempImage.jpg")
                                    cachePath.createNewFile()
                                    val outputStream = FileOutputStream(cachePath)
                                    bitmap.value.compress(
                                        Bitmap.CompressFormat.JPEG,
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
                                                Log.d("Image", downloadUrl.toString())
                                                coroutineScope.launch {
                                                    sharedViewModel.saveMessages(
                                                        chatRoomId, FirebaseMessage(
                                                            sender = sharedViewModel.auth.currentUser?.uid.toString(),
                                                            content = "",
                                                            timestamp = Timestamp.now(),
                                                            read = false,
                                                            image = downloadUrl.toString(),
                                                            visible = arrayListOf(
                                                                sharedViewModel.friend.value.personList.id,
                                                                sharedViewModel.auth.currentUser?.uid.toString()
                                                            ),
                                                        )
                                                    )
                                                }

                                            }.addOnFailureListener { exception ->
                                                Log.e("Image", exception.toString())
                                            }
                                        }
                                    }

                                }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    tint = Color.White,
                                    contentDescription = "Send Photo"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }

            }
        )

    }
}