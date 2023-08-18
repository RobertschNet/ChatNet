package at.htlhl.testing.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.views.ChatMate
import at.htlhl.testing.views.ChatView
import at.htlhl.testing.views.DropIn
import at.htlhl.testing.views.Loading
import at.htlhl.testing.views.LoginView
import at.htlhl.testing.views.Profile
import at.htlhl.testing.views.RandChat
import at.htlhl.testing.views.RegisterView
import at.htlhl.testing.views.SearchView

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    NavHost(navController = navController, startDestination = "LoadingScreen") {
        composable("DropInScreen",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            }
        ) {
            DropIn().DropInScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(
            "ChatScreen",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            }
        ) {
            ChatView().ChatViewScreen(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable("RandChatScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            RandChat().RandChatScreen()
        }
        composable("SearchViewScreen",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            }) {
            SearchView().SearchViewScreen(navController)
        }
        composable("ChatMateScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            ChatMate().ChatMateScreen()
        }
        composable("ProfileScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            Profile().ProfileScreen(navController)
        }
        composable("LoginScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            LoginView().LoginScreen(navController, sharedViewModel)
        }
        composable("RegisterScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            RegisterView().RegisterScreen(navController)
        }
        composable("LoadingScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            Loading().LoadingScreen(navController)
        }

    }
}