package at.htlhl.chatnet.services

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class SaveImageTask(private val contextRef: WeakReference<Context>) {

    suspend fun saveImage(imageUrl: String) {
        val context = contextRef.get()

        if (context != null) {
            try {
                val displayName = "Image_${System.currentTimeMillis()}.png"
                val bitmap = withContext(Dispatchers.IO) { downloadImage(imageUrl) }
                val success = withContext(Dispatchers.IO) { saveImageToGallery(context, bitmap, displayName) }
                if (success) {
                    showToast(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun downloadImage(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        }
    }

    private suspend fun saveImageToGallery(context: Context, bitmap: Bitmap, displayName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }

            val resolver = context.contentResolver
            val imageUri = resolver.insert(imageCollection, contentValues)

            try {
                if (imageUri != null) {
                    val outputStream = resolver.openOutputStream(imageUri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    outputStream?.close()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun showToast(context: Context) {
        Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
    }
}
