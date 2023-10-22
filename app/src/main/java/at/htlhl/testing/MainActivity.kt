package at.htlhl.testing

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import at.htlhl.testing.data.LoadingStates
import at.htlhl.testing.navigation.NavigationBarLayout
import at.htlhl.testing.navigation.Screens
import at.htlhl.testing.services.LocationUpdateService
import at.htlhl.testing.ui.theme.TestingTheme
import at.htlhl.testing.viewmodels.SharedViewModel

class MainActivity : ComponentActivity() {
    private var serviceConnection: ServiceConnection? = null
    private var locationUpdateService: LocationUpdateService? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        setContent {
            val viewModel = (application as MyApplication).myViewModel
            val navController = rememberNavController()
            TestingTheme {
                val loadingState = viewModel.loadingState.value
                LaunchedEffect(key1 = true) {
                    viewModel.fetchAuthenticationStatus()
                }
                NavigationBarLayout(
                    navController = navController,
                    viewModel = viewModel,
                    lifecycleOwner = applicationContext
                )
                LaunchedEffect(key1 = loadingState) {
                    when (loadingState) {
                        LoadingStates.Authenticated -> {
                            Log.println(Log.INFO, "Authentication", "User is logged in")
                            viewModel.updateOnlineStatus("online")
                            viewModel.getUserData()
                            viewModel.startListeningForFriends()
                            viewModel.startListeningForMessagesForPairs(
                                viewModel.auth.currentUser!!.uid,
                                {},
                                {})
                            navController.navigate(Screens.Chats.route)
                        }

                        LoadingStates.NotAuthenticated -> {
                            Log.println(Log.INFO, "Authentication", "User is not logged in")
                            navController.navigate(Screens.LoginScreen.route)
                        }

                        LoadingStates.Error -> {
                            Log.println(Log.ERROR, "Authentication", "Error while loading")
                        }

                        else -> Unit
                    }
                }
            }
            manageLocationServiceStatus(viewModel, serviceIntent)

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let { unbindService(it) }
        stopService(Intent(this, LocationUpdateService::class.java))
        (application as MyApplication).myViewModel.updateOnlineStatus("offline")
        (application as MyApplication).myViewModel.resetMatchedUser()
        (application as MyApplication).myViewModel.reset()
    }

    override fun onPause() {
        super.onPause()
        (application as MyApplication).myViewModel.updateOnlineStatus("idle")
    }

    override fun onResume() {
        super.onResume()
        (application as MyApplication).myViewModel.updateOnlineStatus("online")
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
}