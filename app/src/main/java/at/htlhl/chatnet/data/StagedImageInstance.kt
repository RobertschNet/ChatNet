package at.htlhl.chatnet.data

import android.net.Uri
import androidx.compose.runtime.mutableStateOf

data class StagedImageInstance(val id: String, val images: List<Uri>)

val imageUploadMap = mutableStateOf<Map<String, List<StagedImageInstance>>>(emptyMap())

fun addImageUploadList(id: String, images: List<Uri>) {
    val currentMap = imageUploadMap.value.toMutableMap()
    val existingList = currentMap[id] ?: emptyList()
    val updatedList = existingList + StagedImageInstance(id, images)
    currentMap[id] = updatedList
    imageUploadMap.value = currentMap.toMap()
}
fun getImageUploadList(id: String): List<Uri> {
    return imageUploadMap.value[id]?.flatMap { it.images } ?: emptyList()
}

fun removeImageUploadList(id: String) {
    val currentMap = imageUploadMap.value.toMutableMap()
    currentMap.remove(id)
    imageUploadMap.value = currentMap.toMap()
}
fun removeItemFromUploadList(id: String, uri: Uri) {
    val currentMap = imageUploadMap.value.toMutableMap()
    val existingList = currentMap[id] ?: emptyList()
    val updatedList = existingList.map { imageUpload ->
        val updatedImages = imageUpload.images.filterNot { it == uri }
        imageUpload.copy(images = updatedImages)
    }.filterNot { it.images.isEmpty() }
    if (updatedList.isEmpty()) {
        currentMap.remove(id)
    } else {
        currentMap[id] = updatedList
    }
    imageUploadMap.value = currentMap.toMap()
}

