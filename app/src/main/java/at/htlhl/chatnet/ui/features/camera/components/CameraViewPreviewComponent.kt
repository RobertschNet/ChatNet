package at.htlhl.chatnet.ui.features.camera.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraViewPreviewComponent(
    cameraController: LifecycleCameraController,
) {
    val touchPosition = remember { mutableStateOf<Offset?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)

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
        }, modifier = Modifier.fillMaxSize()
    )

    touchPosition.value?.let { position ->
        Canvas(modifier = Modifier.fillMaxSize(), onDraw = {
            drawCircle(Color.White, 80f, position, style = Stroke(4f))
        })
    }
}