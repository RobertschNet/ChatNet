package at.htlhl.testing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.LiveHelp
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import at.htlhl.testing.data.BottomNavItem
import at.htlhl.testing.data.LoadingState
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Navigation
import at.htlhl.testing.navigation.Screens
import at.htlhl.testing.ui.theme.TestingTheme
import at.htlhl.testing.views.LocationUpdateService

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            TestingTheme {
                val viewModel: SharedViewModel = viewModel()
                val loadingState = viewModel.loadingState.value
                LaunchedEffect(key1 = true) {
                    viewModel.fetchAuthenticationStatus()
                    viewModel.startListeningForFriends(navController)
                }
                val serviceIntent = Intent(this, LocationUpdateService::class.java)
                ContextCompat.startForegroundService(this, serviceIntent)
                NavigationBarLayout(
                    navController = navController,
                    viewModel = viewModel,
                )
                LaunchedEffect(key1 = loadingState) {
                    when (loadingState) {
                        LoadingState.Authenticated -> {
                            println("User is logged in")
                        }

                        LoadingState.NotAuthenticated -> {
                            println("User is not logged in")
                            navController.navigate(Screens.LoginScreen.Route)
                        }

                        LoadingState.Error -> {
                        }

                        else -> Unit
                    }
                }
            }
        }
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
                        Column(horizontalAlignment = CenterHorizontally) {
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

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color.Black else Color.White),
        contentAlignment = Center
    ) {
        Text(text = "Loading...")
    }
}