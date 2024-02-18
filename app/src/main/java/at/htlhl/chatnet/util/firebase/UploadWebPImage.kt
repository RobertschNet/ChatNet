package at.htlhl.chatnet.util.firebase

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.storage

fun uploadWebPImage(
    webpByteArray: ByteArray, onUploadSuccess: (List<String>) -> Unit, onUploadError: () -> Unit
) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val webpImageRef = storageRef.child("chats/${Timestamp.now().seconds}.webp")

    webpImageRef.putBytes(webpByteArray).addOnSuccessListener {
        webpImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            onUploadSuccess(listOf(downloadUrl.toString()))
        }.addOnFailureListener {
            onUploadError()
        }
    }.addOnFailureListener {
        onUploadError()
    }
}