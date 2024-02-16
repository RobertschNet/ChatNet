package at.htlhl.chatnet.ui.features.camera.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.camera.components.CameraPhotoViewTakenPictureComponent
import at.htlhl.chatnet.ui.features.camera.components.CameraPhotoViewTextFieldComponent
import at.htlhl.chatnet.ui.features.camera.viewmodels.CameraViewModel
import at.htlhl.chatnet.viewmodels.SharedViewModel

class CameraPhotoView {

    @Composable
    fun CameraPhotoScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val cameraViewModel = viewModel<CameraViewModel>()
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        var cameraTextFieldTextValues by remember {
            mutableStateOf("")
        }
        var isLoading by remember {
            mutableStateOf(false)
        }

        val bitmapState by sharedViewModel.bitmap.collectAsState()
        val friendDataState by sharedViewModel.friend.collectAsState()

        val bitmap: Bitmap = bitmapState
        val friendData: InternalChatInstance = friendDataState

        Scaffold(contentColor = Color.Black,
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
                    CameraPhotoViewTextFieldComponent(currentTextFieldText = cameraTextFieldTextValues,
                        isLoading = isLoading,
                        onRetakePhotoClicked = {
                            navController.navigateUp()
                        },
                        onValueChange = { changedText ->
                            cameraTextFieldTextValues = changedText
                        },
                        onMessageSentPressed = { text ->
                            isLoading = true
                            cameraViewModel.uploadTakenPhoto(bitmap = bitmap,
                                context = context,
                                onFailure = {
                                    Toast.makeText(
                                        context, "Failed to upload image", Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onSuccess = { imageRef ->
                                    imageRef.downloadUrl.addOnSuccessListener { imageDownloadUrl ->
                                        cameraViewModel.sentMessageWithUploadedPhoto(coroutineScope = coroutineScope,
                                            messageText = text,
                                            friendData = friendData,
                                            imageDownloadUrl = imageDownloadUrl,
                                            onSuccess = {
                                                navController.navigate(Screens.ChatViewScreen.route) {
                                                    popUpTo(Screens.ChatViewScreen.route) {
                                                        inclusive = true
                                                    }
                                                }
                                                cameraTextFieldTextValues = ""
                                            },
                                            onFailure = {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to send Message",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            })
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            context, "Failed to send Image", Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        })
                }
            },
            content = { paddingValues ->
                CameraPhotoViewTakenPictureComponent(paddingValues = paddingValues,
                    bitmap = bitmap,
                    onSaveImageToGalleryClicked = {
                        cameraViewModel.saveBitmapToGallery(bitmap = bitmap,
                            context = context,
                            onSuccess = {
                                Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT)
                                    .show()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context, "Failed to save to Gallery", Toast.LENGTH_SHORT
                                ).show()
                            })
                    },
                    onRetakePhotoClicked = {
                        navController.navigateUp()
                    })
            })
    }
}
