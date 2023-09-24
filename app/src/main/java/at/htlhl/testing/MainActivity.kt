package at.htlhl.testing

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LiveHelp
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import at.htlhl.testing.data.BottomNavItem
import at.htlhl.testing.data.LoadingState
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Navigation
import at.htlhl.testing.navigation.Screens
import at.htlhl.testing.service.LocationUpdateService
import at.htlhl.testing.ui.theme.TestingTheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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
            viewModel.updateOnlineStatus("Online")
            val navController = rememberNavController()
            TestingTheme {
                val loadingState = viewModel.loadingState.value
                LaunchedEffect(key1 = true) {
                    viewModel.fetchAuthenticationStatus()
                }
                NavigationBarLayout(
                    navController = navController,
                    viewModel = viewModel,
                )
                LaunchedEffect(key1 = loadingState) {
                    when (loadingState) {
                        LoadingState.Authenticated -> {
                            Log.println(Log.INFO, "Authentication", "User is logged in")
                            viewModel.getUserData()
                            viewModel.startListeningForFriends()
                            viewModel.startListeningForMessagesForPairs(
                                viewModel.auth.currentUser!!.uid,
                                {},
                                {})
                            navController.navigate(Screens.Chats.Route)
                        }

                        LoadingState.NotAuthenticated -> {
                            Log.println(Log.INFO, "Authentication", "User is not logged in")
                            navController.navigate(Screens.LoginScreen.Route)
                        }

                        LoadingState.Error -> {
                            Log.println(Log.ERROR, "Authentication", "Error while loading")
                        }

                        else -> Unit
                    }
                }
            }
            manageLocationServiceStatus(viewModel, serviceIntent)
            if (viewModel.imageCall.value) {
                openPhotoGallery()
                viewModel.imageCall.value = false
            }
        }
    }

    private fun openPhotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri = data.data
                    val storage = Firebase.storage
                    val storageRef = storage.reference
                    val imageRef = storageRef.child("${(application as MyApplication).myViewModel.auth.currentUser!!.uid}/profilePicture")
                    val uploadTask = imageRef.putFile(selectedImageUri!!)
                    uploadTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                Log.d("Image", downloadUrl.toString())
                                (application as MyApplication).myViewModel.updateUserProfilePicture(downloadUrl.toString())
                            }.addOnFailureListener { exception ->
                                Log.e("Image", exception.toString())
                            }
                        } else {
                            val exception = task.exception
                            Log.e("Image", exception.toString())
                        }
                    }
                }
            }
        }



    override fun onDestroy() {
        super.onDestroy()
        serviceConnection?.let { unbindService(it) }
        stopService(Intent(this, LocationUpdateService::class.java))
        (application as MyApplication).myViewModel.updateOnlineStatus("Offline")
        (application as MyApplication).myViewModel.reset()
    }

    override fun onPause() {
        super.onPause()
        (application as MyApplication).myViewModel.updateOnlineStatus("Idle")
    }

    override fun onResume() {
        super.onResume()
        (application as MyApplication).myViewModel.updateOnlineStatus("Online")
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

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NavigationBarLayout(
        navController: NavHostController,
        viewModel: SharedViewModel,
    ) {
        Scaffold(bottomBar = {
            BottomNavigationBar(isBottomBarEnabled = viewModel.bottomBarState, items = listOf(
                BottomNavItem(
                    name = "Chats",
                    route = Screens.Chats.Route,
                    icon = Icons.Default.Group,
                    color = Color(0xFF00A0E8)
                ),
                BottomNavItem(
                    name = "Drop In",
                    route = Screens.DropInScreen.Route,
                    icon = Icons.Default.Message,
                    color = Color(0xFF00B1A9)
                ),
                BottomNavItem(
                    name = "RandChat",
                    route = Screens.RandChatScreen.Route,
                    icon = Icons.Default.LiveHelp,
                    color = Color(0xFFE21515)
                ),
                BottomNavItem(
                    name = "ChatMate",
                    route = Screens.ChatMateScreen.Route,
                    icon = Icons.Default.Api,
                    color = Color(0xFF15B625)
                ),
                BottomNavItem(
                    name = "Profile",
                    route = Screens.ProfileScreen.Route,
                    icon = Icons.Default.ManageAccounts,
                    color = Color(0xFF00A0E8)
                ),
            ), navController = navController, onItemClick = {
                if (navController.currentDestination?.route != it.route) {
                    navController.navigate(it.route)
                }
            })
        }) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                Navigation(
                    navController = navController,
                    sharedViewModel = viewModel
                )
            }
        }
    }

    @Composable
    fun BottomNavigationBar(
        isBottomBarEnabled: MutableState<Boolean>,
        items: List<BottomNavItem>,
        navController: NavController,
        onItemClick: (BottomNavItem) -> Unit
    ) {
        val backStackEntry = navController.currentBackStackEntryAsState()
        if (isBottomBarEnabled.value) {
            NavigationBar(
                containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                items.forEach { item ->
                    val selected = item.route == backStackEntry.value?.destination?.route
                    NavigationBarItem(
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (selected) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(35.dp),
                                        tint = item.color
                                    )
                                    Text(
                                        text = item.name,
                                        color = item.color,
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                        contentDescription = item.name,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
                        ),
                        onClick = {
                            onItemClick(item)
                        },
                    )
                }
            }
        }
    }
}