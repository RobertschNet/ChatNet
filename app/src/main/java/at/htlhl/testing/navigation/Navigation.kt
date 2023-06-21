package at.htlhl.testing.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
fun Navigation(navController: NavHostController, bottomBarState: MutableState<Boolean>) {
    NavHost(navController = navController, startDestination = "LoginScreen") {
        composable("LoadingScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            bottomBarState.value = false
            Loading().LoadingScreen(navController)
        }
        composable("DropInScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
        ) {
            bottomBarState.value = true
            DropIn().DropInScreen(
                navController = navController,
                onNavigateToDestination = { data -> navController.navigate("ChatScreen/$data") })
        }
        composable(

            "ChatScreen/{data}",
            arguments = listOf(navArgument("data") { type = NavType.StringType }),
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
        ) { entry ->
            bottomBarState.value = false
            entry.arguments?.getString("data")
                ?.let { ChatView().ChatViewScreen(data = it, navController = navController) }
        }
        composable("RandChatScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            RandChat().RandChatScreen()
        }
        composable("SearchViewScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            bottomBarState.value = false
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
            bottomBarState.value = false
            LoginView().LoginScreen(navController)
        }
        composable("RegisterScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            bottomBarState.value = false
            RegisterView().RegisterScreen(navController)
        }
    }
}