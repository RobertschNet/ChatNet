package at.htlhl.chatnet.navigation

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import at.htlhl.chatnet.ui.views.ChatMateView
import at.htlhl.chatnet.ui.views.ChatView
import at.htlhl.chatnet.ui.views.Chats
import at.htlhl.chatnet.ui.views.DropIn
import at.htlhl.chatnet.ui.views.InboxView
import at.htlhl.chatnet.ui.views.LoadingView
import at.htlhl.chatnet.ui.views.LoginView
import at.htlhl.chatnet.ui.views.ProfileView
import at.htlhl.chatnet.ui.views.RandChatView
import at.htlhl.chatnet.ui.views.RegisterView
import at.htlhl.chatnet.ui.views.SearchView
import at.htlhl.chatnet.viewmodels.SharedViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    lifecycleOwner: Context
) {
    NavHost(navController = navController, startDestination = "LoadingScreen") {
        composable("ChatsViewScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
        ) {
            Chats().ChatsScreen(navController, sharedViewModel)
        }
        composable("DropInScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
        ) {
            sharedViewModel.bottomBarState.value = true
            DropIn().DropInScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
        composable(
            "ChatViewScreen",
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
            sharedViewModel.bottomBarState.value = false
            ChatView().ChatViewScreen(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable("RandChatScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            RandChatView().RandChatScreen(navController, sharedViewModel)
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
            sharedViewModel.bottomBarState.value = false
            SearchView().SearchViewScreen(navController, sharedViewModel)
        }
        composable("ChatMateScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ChatMateView().ChatMateScreen(navController, sharedViewModel)
            }
        }
        composable("ProfileScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            ProfileView().ProfileScreen(navController, sharedViewModel)
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
            LoadingView().LoadingScreen(navController)
        }
        composable("InboxScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            InboxView().Inbox(sharedViewModel = sharedViewModel, lifecycleOwner)
        }
    }
}