package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import android.view.MotionEvent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class CameraView {

    @SuppressLint("NotConstructor", "UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun CameraView(
        navController: NavController,
        sharedViewModel: SharedViewModel,
        applicationContext: Context
    ) {
        val context = LocalContext.current
        var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
        var flashLightState by remember { mutableStateOf(false) }
        var cameraSide by remember { mutableStateOf(false) }
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            TODO("VERSION.SDK_INT < S")
        }
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(
            color = Color.Black,
            darkIcons = false
        )
        val controller = remember {
            LifecycleCameraController(applicationContext).apply {
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
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    CameraPreview(
                        controller = controller,
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(
                            onClick = {
                                systemUiController.setStatusBarColor(
                                    color = Color.Transparent,
                                    darkIcons = true
                                )
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
                                flashLightState = !flashLightState
                                controller.cameraControl?.enableTorch(flashLightState)
                            },
                            modifier = Modifier.padding(end = 10.dp)
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
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.4f),
                                    RoundedCornerShape(36.dp)
                                )
                        ) {
                            IconButton(
                                onClick = {
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
                                    controller.imageCaptureFlashMode = flashMode
                                }
                            ) {
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
                                    },
                                    tint = Color.White,
                                    contentDescription = "Open gallery"
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.2f),
                                    RoundedCornerShape(100)
                                )
                                .border(
                                    3.dp, Color.White, RoundedCornerShape(36.dp)
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    val effect = VibrationEffect.createOneShot(
                                        100,
                                        VibrationEffect.DEFAULT_AMPLITUDE
                                    )
                                    vibrator.defaultVibrator.vibrate(effect)
                                    takePhoto(controller, applicationContext) { bitmap ->
                                        Log.println(Log.INFO, "Camera", "Photo taken")
                                        navController.navigate(Screens.CameraPhotoScreen.route)
                                        sharedViewModel.onTakePhoto(bitmap)
                                    }

                                },
                                modifier = Modifier.align(Alignment.Center)
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
                            modifier = Modifier
                                .background(
                                    Color.Black.copy(alpha = 0.4f),
                                    RoundedCornerShape(36.dp)
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    cameraSide = !cameraSide
                                    if (cameraSide) {
                                        controller.cameraSelector =
                                            CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        controller.cameraSelector =
                                            CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (cameraSide) {
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

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun CameraPreview(
        controller: LifecycleCameraController,
    ) {
        val touchPosition = remember { mutableStateOf<Offset?>(null) }
        val lifecycleOwner = LocalLifecycleOwner.current
        AndroidView(
            factory = {
                PreviewView(it).apply {
                    this.controller = controller
                    controller.bindToLifecycle(lifecycleOwner)

                    setOnTouchListener { _, motionEvent ->
                        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                            val x = motionEvent.x
                            val y = motionEvent.y
                            val offset = Offset(x, y)
                            touchPosition.value = offset
                            true
                        } else {
                            false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        touchPosition.value?.let { position ->
            Canvas(
                modifier = Modifier.fillMaxSize(),
                onDraw = {
                    drawCircle(Color.White, 80f, position, style = Stroke(4f))
                }
            )
        }
    }

    private fun takePhoto(
        controller: LifecycleCameraController,
        applicationContext: Context,
        onPhotoTaken: (Bitmap) -> Unit,
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )

                    onPhotoTaken(rotatedBitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo: ", exception)
                }
            }
        )
    }
}
