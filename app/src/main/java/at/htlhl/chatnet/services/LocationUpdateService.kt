package at.htlhl.chatnet.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import at.htlhl.chatnet.data.FirebaseUsers
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase

class LocationUpdateService : Service() {
    private val locationScanInterval = 1000L // 1 second
    private val handler = Handler()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    val auth: FirebaseAuth = Firebase.auth
    private var lastKnownLocation: Location? = null
    val locationLiveData = MutableLiveData<List<FirebaseUsers>>()

    override fun onCreate() {
        super.onCreate()
        val notification = createNotification()
        startForeground(11, notification)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(locationScanInterval)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    if (isLocationDifferent(location)) {
                        lastKnownLocation = location
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val hash =
                            GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))
                        val updates: MutableMap<String, Any> = mutableMapOf(
                            "geohash" to hash,
                            "lat" to latitude,
                            "lng" to longitude,
                        )
                        sendLocation(updates, auth)
                        fetchLocation(latitude, longitude)
                    }
                }
            }
        }
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
        println(intent.toString())
        if (intent?.action == "STOP_LOCATION_SERVICE") {
            println("Stopping the location service.")
            removeLocationUpdates()
            stopSelf()
            super.onDestroy()
            return START_NOT_STICKY
        }
        handler.postDelayed(locationRunnable, locationScanInterval)

        return START_STICKY

    }

    override fun onDestroy() {
        handler.removeCallbacks(locationRunnable)
        super.onDestroy()
        removeLocationUpdates()
    }

    private val locationRunnable = object : Runnable {
        override fun run() {
            requestLocationUpdates()
            handler.postDelayed(this, locationScanInterval)
        }
    }

    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun isLocationDifferent(newLocation: Location): Boolean {
        return lastKnownLocation == null || newLocation.distanceTo(lastKnownLocation!!) > MIN_DISTANCE_THRESHOLD
    }

    companion object {
        private const val MIN_DISTANCE_THRESHOLD = 1.0
    }

    override fun onBind(intent: Intent?): IBinder {
        return YourBinder()
    }

    inner class YourBinder : Binder() {
        fun getService(): LocationUpdateService = this@LocationUpdateService
    }

    fun sendLocation(documentId: MutableMap<String, Any>, auth: FirebaseAuth) {
        val location = mutableMapOf(
            "geohash" to documentId["geohash"],
            "geopoint" to GeoPoint(documentId["lat"] as Double, documentId["lng"] as Double)
        )
        FirebaseFirestore.getInstance().document("users/${auth.currentUser?.uid}")
            .update("location", location)
            .addOnSuccessListener {
                println("Location sent successfully.")
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    fun fetchLocation(latitude: Double, longitude: Double) {
        val center = GeoLocation(latitude, longitude)
        val radiusInM = 5.0 * 100.0
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("status", "online")
                .orderBy("location.geohash")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingDocs: MutableList<Map<String, Any>> = ArrayList()
                for (task in tasks) {
                    val snap = task.result
                    for (doc in snap!!.documents) {
                        val location = doc.get("location") as Map<*, *>
                        val geolocation = location["geopoint"] as GeoPoint
                        val lat = geolocation.latitude
                        val lng = geolocation.longitude
                        val docLocation = GeoLocation(lat, lng)
                        val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                        println(distanceInM)
                        if (distanceInM <= radiusInM) {
                            doc.data?.let { it1 -> matchingDocs.add(it1) }
                        }
                    }
                    val personList = matchingDocs.map { dataMap ->
                        val location = dataMap["location"] as Map<*, *>
                        val geolocation = location["geopoint"] as GeoPoint
                        val docLocation = GeoLocation(geolocation.latitude, geolocation.longitude)
                        val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)

                        FirebaseUsers(
                            id = dataMap["id"].toString(),
                            username =  dataMap["username"] as Map<String, String>,
                            image = dataMap["image"].toString(),
                            connection = dataMap["connection"].toString(),
                            status = dataMap["status"] as String,
                            email = dataMap["email"].toString(),
                            color = dataMap["color"].toString(),
                            blocked = dataMap["blocked"] as List<String>,
                            pinned = dataMap["pinned"] as List<String>,
                            mutedFriend = false,
                            statusFriend = "User is ${distanceInM.toInt()} meters away",
                        )
                    }
                    locationLiveData.postValue(personList)
                }
            }
    }

}
