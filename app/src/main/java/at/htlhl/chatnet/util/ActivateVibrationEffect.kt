package at.htlhl.chatnet.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

@Suppress("DEPRECATION")
fun activateVibrationEffect(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val effect = VibrationEffect.createOneShot(
            100, VibrationEffect.DEFAULT_AMPLITUDE
        )
        val vibrationManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrationManager.defaultVibrator.vibrate(effect)
    } else {
        val vibrationManager = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrationManager.vibrate(100)
    }
}