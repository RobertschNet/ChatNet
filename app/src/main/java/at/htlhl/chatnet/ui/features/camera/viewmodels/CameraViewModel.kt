package at.htlhl.chatnet.ui.features.camera.viewmodels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.FirebaseMessage
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.util.cloudfunctions.sendPushNotificationToPartner
import at.htlhl.chatnet.util.firebase.saveMessage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraViewModel : ViewModel() {
    val auth: FirebaseAuth = com.google.firebase.ktx.Firebase.auth

    fun takePhoto(
        cameraController: LifecycleCameraController,
        context: Context,
        onPhotoTaken: (Bitmap) -> Unit,
    ) {
        cameraController.takePicture(ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)

                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(), 0, 0, image.width, image.height, matrix, true
                    )

                    onPhotoTaken(rotatedBitmap)
                }
            }
        )
    }

    fun saveBitmapToGallery(
        bitmap: Bitmap,
        context: Context,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
    ): Uri? {
        val saveFolderName = "ChatNet"
        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "${saveFolderName}_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$saveFolderName")
        }
        val contentResolver = context.contentResolver
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        try {
            uri?.let { imageUri ->
                contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    onSuccess()
                }
            }
        } catch (e: IOException) {
            onFailure()
            return null
        }
        return uri
    }

    fun uploadTakenPhoto(
        bitmap: Bitmap,
        context: Context,
        onFailure: () -> Unit,
        onSuccess: (StorageReference) -> Unit
    ) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val cachePath = File(context.cacheDir, "tempImage.webp")
        cachePath.createNewFile()
        val outputStream = FileOutputStream(cachePath)
        @Suppress("DEPRECATION") bitmap.compress(
            Bitmap.CompressFormat.WEBP, 50, outputStream
        )
        outputStream.close()
        val imageRef = storageRef.child("chats/${Timestamp.now().seconds}.webp")
        val uploadTask = imageRef.putFile(Uri.fromFile(cachePath))
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(imageRef)
            } else {
                onFailure()
            }
        }
    }

    fun sentMessageWithUploadedPhoto(
        coroutineScope: CoroutineScope,
        friendData: InternalChatInstance,
        messageText: String,
        imageDownloadUrl: Uri,
        onFailure: () -> Unit,
        onSuccess: () -> Unit,
    ) {
        coroutineScope.launch {
            val message = FirebaseMessage(
                sender = auth.currentUser?.uid.toString(),
                text = messageText,
                timestamp = Timestamp.now(),
                read = false,
                images = arrayListOf(imageDownloadUrl.toString()),
                visible = arrayListOf(
                    friendData.personList.id, auth.currentUser?.uid.toString()
                ),
            )
            saveMessage(chatRoomID = friendData.chatRoomID, message = message, onSuccess = {
                sendPushNotificationToPartner(
                    userID = auth.currentUser?.uid.toString(),
                    friendID = friendData.personList.id,
                    message = message
                )
                onSuccess()
            }, onFailure = {
                onFailure()
            })
        }
    }
}