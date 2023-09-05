package at.htlhl.testing.views

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LocationUpdateService : Service() {

    private val locationScanInterval = 10000L // 10 seconds
    private val handler = Handler()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val auth: FirebaseAuth = Firebase.auth

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(11, notification)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_service_channel"
            val channelName = "My Service Channel"
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(this, "my_service_channel")
            .setContentTitle("My Foreground Service")
            .setContentText("Service is running")
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.postDelayed(locationRunnable, locationScanInterval)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(locationRunnable)
        super.onDestroy()
    }

    private val locationRunnable = object : Runnable {
        override fun run() {
            getLocation(applicationContext, fusedLocationClient, auth)
            handler.postDelayed(this, locationScanInterval)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

private var lastKnownLocation: Location? = null
private const val MIN_DISTANCE_THRESHOLD = 0.0000001
fun getLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    auth: FirebaseAuth
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    if (lastKnownLocation == null || location.distanceTo(lastKnownLocation!!) > MIN_DISTANCE_THRESHOLD) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val locations = listOf(latitude, longitude)
                        println("Latitude: $latitude, Longitude: $longitude")
                        sendLocation(locations, auth)
                        lastKnownLocation = location
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}

fun sendLocation(documentId: List<Double>?, auth: FirebaseAuth) {
    FirebaseFirestore.getInstance().document("user/${auth.currentUser?.uid}")
        .update("location", documentId)
        .addOnSuccessListener {
            println("Location sent successfully.")
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
}
