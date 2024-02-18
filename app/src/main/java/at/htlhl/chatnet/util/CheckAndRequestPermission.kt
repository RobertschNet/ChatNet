package at.htlhl.chatnet.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat

fun checkAndRequestPermission(
    context: Context, permission: String, launcher: ManagedActivityResultLauncher<String, Boolean>
): Boolean {
    val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
    return if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
        true
    } else {
        launcher.launch(permission)
        false
    }
}