package at.htlhl.chatnet

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import at.htlhl.chatnet.navigation.NavigationBottomBarLayout
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.services.DropInUpdateService
import at.htlhl.chatnet.ui.theme.ChatNetTheme
import at.htlhl.chatnet.util.cloudfunctions.resetRandChatPairedUser
import at.htlhl.chatnet.util.cloudfunctions.updateUsersFCMToken
import at.htlhl.chatnet.util.preLoadImages
import at.htlhl.chatnet.viewmodels.SharedViewModel


class MainActivity : ComponentActivity() {
    private var serviceConnection: ServiceConnection? = null
    private var dropInUpdateService: DropInUpdateService? = null
    private val viewModel by viewModels<SharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition {
            viewModel.auth.currentUser != null && !viewModel.isDataReady
            }
        }
        val serviceIntent = Intent(this, DropInUpdateService::class.java)
        setContent {
            val navController = rememberNavController()
            ChatNetTheme {
                if (viewModel.auth.currentUser != null) {
                    NavigationBottomBarLayout(
                        navController = navController,
                        startDestination = Screens.MainFlow.route,
                        viewModel = viewModel
                    )
                } else {
                    NavigationBottomBarLayout(
                        navController = navController,
                        startDestination = Screens.LoginFlow.route,
                        viewModel = viewModel
                    )
                }
                updateUsersFCMToken(auth = viewModel.auth)
                LaunchedEffect(Unit) {
                    if (viewModel.auth.currentUser != null) {
                        viewModel.updateOnlineStatus(true)
                        viewModel.getUserData {
                            preLoadImages(
                                context = applicationContext, imageUrls = viewModel.userData.value.image
                            )
                        }
                        viewModel.fetchFriendsFromUser {
                            for (friend in viewModel.friendListData.value) {
                                preLoadImages(
                                    context = applicationContext, imageUrls = friend.image
                                )
                            }
                        }
                        viewModel.fetchChatsWithMessages()
                        for (chat in viewModel.chatData.value) {
                            for (message in chat.messages) {
                                if (message.images.isNotEmpty()) {
                                    for (image in message.images) {
                                        preLoadImages(
                                            context = applicationContext, imageUrls = image
                                        )
                                    }
                                }
                            }
                        }
                        viewModel.fetchRandomFriendsFromFriend()
                    }
                }
            }
            if (checkFineLocationPermission()) {
                manageDropInServiceStatus(viewModel = viewModel, serviceIntent = serviceIntent)
            } else {
                requestFineLocationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resetRandChatPairedUser(auth = viewModel.auth)
        serviceConnection?.let { unbindService(it) }
        stopService(Intent(this, DropInUpdateService::class.java))
    }

    override fun onPause() {
        super.onPause()
        resetRandChatPairedUser(auth = viewModel.auth)
        viewModel.updateOnlineStatus(false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateOnlineStatus(true)
    }

    private fun manageDropInServiceStatus(viewModel: SharedViewModel, serviceIntent: Intent) {
        if (viewModel.dropInState.value) {
            try {
                unbindService(serviceConnection!!)
                stopService(serviceIntent)
            } catch (_: Exception) {

            }
        } else {
            startService(serviceIntent)
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as DropInUpdateService.LocationBinder
                    dropInUpdateService = binder.getService()
                    dropInUpdateService?.nearbyDropInUsersList?.observe(this@MainActivity) { nearbyUsers ->
                        viewModel.updateNearbyDropInUsersList(newNearbyUsers = nearbyUsers)
                    }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    dropInUpdateService = null
                }
            }
            bindService(serviceIntent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        }
    }

    private fun checkFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestFineLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                manageDropInServiceStatus(
                    viewModel = viewModel, serviceIntent = Intent(this, DropInUpdateService::class.java)
                )
            } else {
                viewModel.updateDropInState(newState = false)
            }
        }
}