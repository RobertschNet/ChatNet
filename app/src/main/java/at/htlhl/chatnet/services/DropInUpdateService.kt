package at.htlhl.chatnet.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
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

class DropInUpdateService : Service() {
    private val locationScanInterval = 30000L // 30 seconds
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    val auth: FirebaseAuth = Firebase.auth
    val nearbyDropInUsersList = MutableLiveData<List<LocationUserInstance>>()

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest =
            LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(locationScanInterval)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))
                    val updates: MutableMap<String, Any> = mutableMapOf(
                        "geohash" to hash,
                        "lat" to latitude,
                        "lng" to longitude,
                    )
                    sendLocation(documentId = updates, auth = auth)
                    fetchLocation(latitude = latitude, longitude = longitude)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
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
        if (intent?.action == "STOP_LOCATION_SERVICE") {
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
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, null
            )
        }
    }

    private fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder {
        return LocationBinder()
    }

    inner class LocationBinder : Binder() {
        fun getService(): DropInUpdateService = this@DropInUpdateService
    }

    fun sendLocation(documentId: MutableMap<String, Any>, auth: FirebaseAuth) {
        val location = mutableMapOf(
            "geohash" to documentId["geohash"],
            "geopoint" to GeoPoint(documentId["lat"] as Double, documentId["lng"] as Double)
        )
        FirebaseFirestore.getInstance().document("users/${auth.currentUser?.uid}")
            .update("location", location)
    }

    @Suppress("UNCHECKED_CAST")
    fun fetchLocation(latitude: Double, longitude: Double) {
        val center = GeoLocation(latitude, longitude)
        val usersCollection = FirebaseFirestore.getInstance().collection("users")

        usersCollection.whereEqualTo("online", true).get()
            .addOnSuccessListener { snapshot ->
                val matchingDocs = snapshot.documents
                    .mapNotNull { doc ->
                        val location = doc["location"] as? Map<*, *>
                        location?.let { doc.data }
                    }

                val sortedUsers = matchingDocs.sortedBy { user ->
                    val location = user["location"] as? Map<*, *>
                    val geolocation = location?.get("geopoint") as? GeoPoint
                    val docLocation = GeoLocation(geolocation?.latitude ?: 0.0, geolocation?.longitude ?: 0.0)
                    GeoFireUtils.getDistanceBetween(docLocation, center)
                }

                val top10ClosestUsers = sortedUsers.take(10)
                val personList = top10ClosestUsers.map { dataMap ->
                    val location = dataMap["location"] as? Map<*, *>
                    val geolocation = location?.get("geopoint") as? GeoPoint
                    val docLocation = GeoLocation(geolocation?.latitude ?: 0.0, geolocation?.longitude ?: 0.0)
                    val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                    val formattedDistance = distanceInM.toInt().toString()

                    LocationUserInstance(
                        id = dataMap["id"].toString(),
                        username = dataMap["username"] as? Map<String, String> ?: emptyMap(),
                        image = dataMap["image"].toString(),
                        online = dataMap["online"] as? Boolean ?: false,
                        blocked = dataMap["blocked"] as? List<String> ?: emptyList(),
                        muted = dataMap["muted"] as? List<String> ?: emptyList(),
                        location = getLocation(
                            dataMap = dataMap,
                            formattedDistance = formattedDistance,
                            geolocation = geolocation,
                            center = center
                        )
                    )
                }

                nearbyDropInUsersList.postValue(personList)
            }
    }

    private fun getLocation(
        dataMap: Map<String, Any>,
        formattedDistance: String,
        geolocation: GeoPoint?,
        center: GeoLocation
    ): String {
        return when {
            getCityName(
                context = applicationContext,
                latitude = geolocation?.latitude ?: 0.0,
                longitude = geolocation?.longitude ?: 0.0
            ) !=
                    getCityName(
                        context = applicationContext,
                        latitude = center.latitude,
                        longitude = center.longitude
                    ) ->
                getCityName(
                    context = applicationContext,
                    latitude = geolocation?.latitude ?: 0.0,
                    longitude = geolocation?.longitude ?: 0.0
                )
            auth.currentUser?.uid == dataMap["id"].toString() ->
                getCityName(
                    context = applicationContext,
                    latitude = center.latitude,
                    longitude = center.longitude
                )
            else ->
                "$formattedDistance m away"
        }
    }

}