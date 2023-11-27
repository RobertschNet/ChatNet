package at.htlhl.chatnet.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import at.chatnet.R
import at.htlhl.chatnet.data.BottomNavItem
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.rememberAsyncImagePainter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationBarLayout(
    navController: NavHostController,
    viewModel: SharedViewModel,
    context: Context
) {
    Scaffold(
        bottomBar = {
        BottomNavigationBar(isBottomBarEnabled = viewModel.bottomBarState, items = listOf(
            BottomNavItem(
                name = "Chats",
                route = Screens.ChatsViewScreen.route,
                icon = R.drawable.chat_ui_web_svgrepo_com,
                color = Color(0xFF00A0E8)
            ),
            BottomNavItem(
                name = "Drop In",
                route = Screens.DropInScreen.route,
                icon = R.drawable.location_place_pin_svgrepo_com,
                color = Color(0xFF00B1A9)
            ),
            BottomNavItem(
                name = "RandChat",
                route = Screens.RandChatStartScreen.route,
                icon = R.drawable.chat_bubbles_question_svgrepo_com_1_,
                color = Color(0xFFE21515)
            ),
            BottomNavItem(
                name = "ChatMate",
                route = Screens.ChatMateScreen.route,
                icon = R.drawable.brain_illustration_12_svgrepo_com,
                color = Color(0xFF15B625)
            ),
            BottomNavItem(
                name = "Profile",
                route = Screens.ProfileScreen.route,
                icon = R.drawable.user_circle_svgrepo_com,
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
                sharedViewModel = viewModel,
                context = context
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
            containerColor = MaterialTheme.colorScheme.background,
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
                                Image(
                                    painter = rememberAsyncImagePainter(item.icon),
                                    contentDescription = item.name,
                                    colorFilter = ColorFilter.tint(item.color),
                                    modifier = Modifier.size(38.dp),
                                )
                                Text(
                                    text = item.name,
                                    color = item.color,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
                            } else {
                                Image(
                                    painter = rememberAsyncImagePainter(item.icon),
                                    contentDescription = item.name,
                                    modifier = Modifier.size(30.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                )
                            }
                        }
                    },
                    selected = selected,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.background,
                    ),
                    onClick = {
                        onItemClick(item)
                    },
                )
            }
        }
    }
}
