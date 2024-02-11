package at.htlhl.chatnet.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
fun convertBitmapToWebP(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.WEBP, 50, outputStream)
    return outputStream.toByteArray()
}