package at.htlhl.chatnet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import at.chatnet.R
import at.htlhl.chatnet.data.BottomNavItem
import at.htlhl.chatnet.viewmodels.SharedViewModel

@Composable
fun NavigationBottomBarLayout(
    navController: NavHostController,
    startDestination: String,
    viewModel: SharedViewModel,
) {
    var isBottomBarEnabled by remember {
        mutableStateOf(false)
    }
    Scaffold(bottomBar = {
        NavigationBottomBarComponent(isEnabled = isBottomBarEnabled, items = listOf(
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
            Navigation(navController = navController,
                sharedViewModel = viewModel,
                startDestination = startDestination,
                onBottomBarDisabled = {
                    isBottomBarEnabled = it
                })
        }
    }
}