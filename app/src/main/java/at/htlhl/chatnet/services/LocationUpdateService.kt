package at.htlhl.chatnet.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import at.htlhl.chatnet.data.LocationUserInstance
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.Locale

class LocationUpdateService : Service() {
    private val locationScanInterval = 30000L // 60 second
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    val auth: FirebaseAuth = Firebase.auth
    private var lastKnownLocation: Location? = null
    val locationLiveData = MutableLiveData<List<LocationUserInstance>>()

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(locationScanInterval)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
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
                    if (isLocationDifferent(location)) {
                        sendLocation(updates, auth)
                    }
                    fetchLocation(latitude, longitude)
                }
            }
        }
    }

    private fun getCityName(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val cityName = addresses[0].locality
                    return cityName ?: "Unknown City"
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Unknown City"
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
        requestLocationUpdates()
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
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("online", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val matchingDocs: MutableList<Map<String, Any>> = ArrayList()
                for (doc in snapshot.documents) {
                    val location = doc.get("location") as? Map<*, *>
                    if (location != null) {
                        doc.data?.let { it1 -> matchingDocs.add(it1) }
                    }
                }
                val sortedUsers = matchingDocs.sortedBy {
                    val location = it["location"] as? Map<*, *>
                    val geolocation = location?.get("geopoint") as? GeoPoint
                    val docLocation =
                        GeoLocation(geolocation?.latitude ?: 0.0, geolocation?.longitude ?: 0.0)
                    GeoFireUtils.getDistanceBetween(docLocation, center)
                }
                val top5ClosestUsers = sortedUsers.take(5)
                val personList = top5ClosestUsers.map { dataMap ->
                    val location = dataMap["location"] as? Map<*, *>
                    val geolocation = location?.get("geopoint") as? GeoPoint
                    val docLocation =
                        GeoLocation(geolocation?.latitude ?: 0.0, geolocation?.longitude ?: 0.0)
                    val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                    val formattedDistance = String.format("%.2f", distanceInM)

                    LocationUserInstance(
                        id = dataMap["id"].toString(),
                        username = dataMap["username"] as? Map<String, String> ?: emptyMap(),
                        image = dataMap["image"].toString(),
                        online = dataMap["online"] as? Boolean ?: false,
                        blocked = dataMap["blocked"] as? List<String> ?: emptyList(),
                        muted = dataMap["muted"] as? List<String> ?: emptyList(),
                        location = if (getCityName(
                                applicationContext,
                                geolocation?.latitude ?: 0.0,
                                geolocation?.longitude ?: 0.0
                            ) != getCityName(applicationContext, latitude, longitude)
                        ) {
                            getCityName(
                                applicationContext,
                                geolocation?.latitude ?: 0.0,
                                geolocation?.longitude ?: 0.0
                            )
                        } else if (auth.currentUser?.uid == dataMap["id"].toString()) {
                            getCityName(applicationContext, latitude, longitude)
                        } else {
                            "$formattedDistance m away"
                        }
                    )

                }
                locationLiveData.postValue(personList)
            }
    }
}