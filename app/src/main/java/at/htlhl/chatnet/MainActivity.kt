package at.htlhl.chatnet

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import at.htlhl.chatnet.navigation.NavigationBarLayout
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.LocationUpdateService
import at.htlhl.chatnet.ui.theme.TestingTheme
import at.htlhl.chatnet.viewmodels.SharedViewModel

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
                LaunchedEffect(Unit) {
                    if (viewModel.checkIfUserIsLoggedIn()) {
                        Log.println(Log.INFO, "User", "User is logged in!!!!!!!!!")
                        viewModel.updateOnlineStatus("online")
                        viewModel.getUserData()
                        viewModel.fetchFriendsFromUser()
                        viewModel.fetchChatsWithMessages {
                            viewModel.fetchRandomFriendsFromFriend()
                            if (navController.currentDestination?.route == Screens.LoadingScreen.route && start.value) {
                                start.value = false
                                navController.navigate(Screens.ChatsViewScreen.route)
                            }
                        }
                    } else {
                        navController.navigate(Screens.LoginScreen.route)
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
        (application as MyApplication).sharedViewModel.resetRandChat()
        (application as MyApplication).sharedViewModel.updateOnlineStatus("offline")
        (application as MyApplication).sharedViewModel.reset()
    }

    override fun onPause() {
        super.onPause()
        (application as MyApplication).sharedViewModel.updateOnlineStatus("idle")
    }

    override fun onResume() {
        super.onResume()
        (application as MyApplication).sharedViewModel.updateOnlineStatus("online")
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