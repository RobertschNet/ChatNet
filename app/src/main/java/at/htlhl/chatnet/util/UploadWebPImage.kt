package at.htlhl.chatnet.util

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.storage

fun uploadWebPImage(
    webpByteArray: ByteArray,
    saveLocation: String,
    onUploadSuccess: (String) -> Unit,
    onUploadError: () -> Unit
) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val webpImageRef = storageRef.child(saveLocation + Timestamp.now().seconds + ".webp")

    webpImageRef.putBytes(webpByteArray)
        .addOnSuccessListener {
            webpImageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onUploadSuccess.invoke(downloadUrl.toString())
            }.addOnFailureListener {
                onUploadError.invoke()
            }
        }
        .addOnFailureListener {
            onUploadError.invoke()
        }
}