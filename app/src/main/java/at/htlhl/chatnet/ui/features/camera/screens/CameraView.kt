package at.htlhl.chatnet.ui.features.camera.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.camera.components.CameraViewPreviewComponent
import at.htlhl.chatnet.ui.features.camera.viewmodels.CameraViewModel
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class CameraView {

    @SuppressLint("NotConstructor")
    @Composable
    fun CameraView(
        navController: NavController, sharedViewModel: SharedViewModel
    ) {
        val cameraViewModel = viewModel<CameraViewModel>()
        val context = LocalContext.current
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = Color.Black, darkIcons = false
        )
        var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
        var flashLightState by remember { mutableStateOf(false) }
        var cameraSideState by remember { mutableStateOf(false) }
        val cameraController = remember {
            LifecycleCameraController(context).apply {
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
                )
                imageCaptureFlashMode = flashMode
            }
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = Color.Black,
            contentColor = Color.Black,
        ) { paddingValues ->
            Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    CameraViewPreviewComponent(
                        cameraController = cameraController,
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }, modifier = Modifier.padding(start = 10.dp)
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
                                flashLightState = !flashLightState
                                cameraController.cameraControl?.enableTorch(flashLightState)
                            }, modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (flashLightState) {
                                    Icons.Default.FlashlightOn
                                } else Icons.Default.FlashlightOff,
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(20.dp), horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            modifier = Modifier.background(
                                Color.Black.copy(alpha = 0.4f), RoundedCornerShape(36.dp)
                            )
                        ) {
                            IconButton(onClick = {
                                flashMode = when (flashMode) {
                                    ImageCapture.FLASH_MODE_OFF -> {
                                        ImageCapture.FLASH_MODE_ON
                                    }

                                    ImageCapture.FLASH_MODE_ON -> {
                                        ImageCapture.FLASH_MODE_AUTO
                                    }

                                    else -> {
                                        ImageCapture.FLASH_MODE_OFF
                                    }
                                }
                                cameraController.imageCaptureFlashMode = flashMode
                            }) {
                                Icon(
                                    imageVector = when (flashMode) {
                                        ImageCapture.FLASH_MODE_OFF -> {
                                            Icons.Default.FlashOff
                                        }

                                        ImageCapture.FLASH_MODE_ON -> {
                                            Icons.Default.FlashOn
                                        }

                                        else -> {
                                            Icons.Default.FlashAuto
                                        }
                                    }, tint = Color.White, contentDescription = null
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.2f), RoundedCornerShape(100)
                                )
                                .border(
                                    3.dp, Color.White, RoundedCornerShape(36.dp)
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val screenVibrationManager =
                                            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                                        val effect = VibrationEffect.createOneShot(
                                            100, VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                        screenVibrationManager.defaultVibrator.vibrate(effect)
                                    } else {
                                        @Suppress("DEPRECATION") val screenVibrationManager =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        @Suppress("DEPRECATION") screenVibrationManager.vibrate(100)
                                    }
                                    cameraViewModel.takePhoto(
                                        cameraController = cameraController, context = context
                                    ) { bitmap ->
                                        navController.navigate(Screens.CameraPhotoScreen.route)
                                        sharedViewModel.updatePhotoBitmap(bitmap)
                                    }

                                }, modifier = Modifier.align(Alignment.Center)
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.Center)
                                ) {
                                    val circleRadius = size.minDimension / 2
                                    val circleCenter = Offset(circleRadius, circleRadius)
                                    drawCircle(
                                        color = Color.White,
                                        center = circleCenter,
                                        radius = circleRadius
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.background(
                                Color.Black.copy(alpha = 0.4f), RoundedCornerShape(36.dp)
                            )
                        ) {
                            IconButton(onClick = {
                                cameraSideState = !cameraSideState
                                if (cameraSideState) {
                                    cameraController.cameraSelector =
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    cameraController.cameraSelector =
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                }
                            }) {
                                Icon(
                                    imageVector = if (cameraSideState) {
                                        Icons.Default.RotateLeft
                                    } else Icons.Default.RotateRight,
                                    tint = Color.White,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}