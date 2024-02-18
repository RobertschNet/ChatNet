package at.htlhl.chatnet.util.firebase

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import at.htlhl.chatnet.util.convertBitmapToWebP

fun createImageToUpload(
    context: Context,
    selectedImageUris: List<Uri>,
    onUploadSuccess: (List<String>) -> Unit,
    onUploadError: () -> Unit
) {
    val list = arrayListOf<String>()
    selectedImageUris.forEach { imageUri ->
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val webpByteArray = convertBitmapToWebP(bitmap = bitmap)
        uploadWebPImage(webpByteArray, {
            list.addAll(it)
            if (list.size == selectedImageUris.size) onUploadSuccess(list)
        }, onUploadError)
    }
}
