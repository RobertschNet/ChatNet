package at.htlhl.chatnet.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import at.chatnet.R
import at.htlhl.chatnet.MainActivity
import at.htlhl.chatnet.util.cloudfunctions.updateUsersFCMToken
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {
    val auth: FirebaseAuth = Firebase.auth

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val notificationTitle = it.title
            val notificationText = it.body
            val channelId = "133"
            val channelName = "ChatNet Notification Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationBuilder =
                NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.logo__1_)
                    .setContentTitle(notificationTitle).setContentText(notificationText)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(133, notificationBuilder.build())
            }
        }
    }

    override fun onNewToken(token: String) {
        updateUsersFCMToken(auth = auth)
    }
}

