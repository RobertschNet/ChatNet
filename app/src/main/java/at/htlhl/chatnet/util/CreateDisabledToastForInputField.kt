package at.htlhl.chatnet.util

import android.content.Context
import android.widget.Toast

fun createDisabledToastForInputField(isCameraPressed: Boolean, context: Context) {
    Toast.makeText(
        context,
        if (isCameraPressed) "Camera not available" else "Gallery not available",
        Toast.LENGTH_SHORT
    ).show()
}