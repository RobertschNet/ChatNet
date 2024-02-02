package at.htlhl.chatnet

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import at.htlhl.chatnet.navigation.NavigationBarLayout
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.LocationUpdateService
import at.htlhl.chatnet.ui.theme.TestingTheme
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : ComponentActivity() {
    private var serviceConnection: ServiceConnection? = null
    private var locationUpdateService: LocationUpdateService? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        setContent {
            val viewModel = (application as MyApplication).sharedViewModel
            val navController = rememberNavController()
            val start = rememberSaveable { mutableStateOf(true) }
            TestingTheme {

                NavigationBarLayout(
                    navController = navController,
                    viewModel = viewModel,
                    context = applicationContext
                )
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            Log.d("FCM Token", "Token: $token")
                            viewModel.sendDeviceToken(token)
                        } else {
                            Log.e("FCM Token", "Failed to get token", task.exception)
                        }
                    }
                LaunchedEffect(Unit) {
                    if (viewModel.checkIfUserIsLoggedIn()) {
                        Log.println(Log.INFO, "User", "User is logged in!!!!!!!!!")
                        viewModel.updateOnlineStatus(true)
                        viewModel.getUserData {
                            loadImage(applicationContext, viewModel.user.value.image)
                        }
                        viewModel.fetchFriendsFromUser {
                            for (friend in viewModel.friendListData.value) {
                                loadImage(applicationContext, friend.image)
                            }
                        }
                        viewModel.fetchChatsWithMessages()
                        for (chat in viewModel.chatData.value) {
                            for (message in chat.messages) {
                                if (message.images.isNotEmpty()) {
                                    for (image in message.images) {
                                        loadImage(applicationContext, image)
                                    }
                                }
                            }
                        }
                        viewModel.fetchRandomFriendsFromFriend()

                        if (navController.currentDestination?.route == Screens.LoadingScreen.route && start.value) {
                            start.value = false
                            navController.navigate("MainFlow") {
                                popUpTo("LoadingScreen") {
                                    inclusive = true
                                }
                            }
                        }
                    } else {
                        navController.navigate("LoginFlow") {
                            popUpTo("LoadingScreen") {
                                inclusive = true
                            }
                        }
                    }
                }
            }
            if (checkFineLocationPermission()) {
                manageLocationServiceStatus(viewModel, serviceIntent)
            } else {
                requestFineLocationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun loadImage(context: Context, imageUrl: String) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()
        context.imageLoader.enqueue(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let { unbindService(it) }
        stopService(Intent(this, LocationUpdateService::class.java))
    }

    override fun onPause() {
        super.onPause()
        (application as MyApplication).sharedViewModel.resetRandChat()
        (application as MyApplication).sharedViewModel.updateOnlineStatus(false)
    }
    override fun onResume() {
        super.onResume()
        (application as MyApplication).sharedViewModel.updateOnlineStatus(true)
    }

    private fun manageLocationServiceStatus(viewModel: SharedViewModel, serviceIntent: Intent) {
        if (viewModel.gpsState.value) {
            try {
                unbindService(serviceConnection!!)
                stopService(serviceIntent)
            } catch (e: Exception) {
                Log.println(Log.ERROR, "Location", e.toString())
            }
        } else {
            startService(serviceIntent)
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as LocationUpdateService.YourBinder
                    locationUpdateService = binder.getService()
                    locationUpdateService?.locationLiveData?.observe(this@MainActivity) { location ->
                        Log.println(Log.INFO, "Location", location.toString())
                        viewModel.localChatUserList.value = location
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    locationUpdateService = null
                }
            }
            bindService(serviceIntent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        }
    }

    private fun checkFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestFineLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                manageLocationServiceStatus(
                    (application as MyApplication).sharedViewModel,
                    Intent(this, LocationUpdateService::class.java)
                )
            } else {
                // Fine location permission denied, handle accordingly (e.g., show a message)
            }
        }
}