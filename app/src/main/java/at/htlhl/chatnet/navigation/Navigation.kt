package at.htlhl.chatnet.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import at.htlhl.chatnet.ui.features.camera.screens.CameraPhotoView
import at.htlhl.chatnet.ui.features.camera.screens.CameraView
import at.htlhl.chatnet.ui.features.chat.screens.ChatView
import at.htlhl.chatnet.ui.features.chatmate.screens.ChatMateView
import at.htlhl.chatnet.ui.features.chats.screens.ChatsView
import at.htlhl.chatnet.ui.features.dropin.screens.DropInView
import at.htlhl.chatnet.ui.features.finduser.screens.FindUserView
import at.htlhl.chatnet.ui.features.images.screens.ImageView
import at.htlhl.chatnet.ui.features.login_register.screens.ForgotPasswordView
import at.htlhl.chatnet.ui.features.login_register.screens.LoginView
import at.htlhl.chatnet.ui.features.login_register.screens.RegisterView
import at.htlhl.chatnet.ui.features.login_register.screens.RegisterWithGoggleView
import at.htlhl.chatnet.ui.features.profile.screens.ProfileView
import at.htlhl.chatnet.ui.features.profilepicture.screens.ProfilePictureView
import at.htlhl.chatnet.ui.features.randchat.screens.RandChatStartView
import at.htlhl.chatnet.ui.features.randchat.screens.RandChatView
import at.htlhl.chatnet.ui.features.tags.screens.TagSelectView
import at.htlhl.chatnet.ui.features.usersheet.screens.PublicUserSheetView
import at.htlhl.chatnet.ui.features.usersheet.screens.UserSheetView
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.delay

@Composable
fun Navigation(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    startDestination: String,
    onBottomBarDisabled: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screens.RegisterWithGoogleScreen.route,
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            RegisterWithGoggleView().RegisterWithGoggleScreen(sharedViewModel, navController)
        }

        navigation(
            startDestination = Screens.CameraViewScreen.route, route = Screens.CameraFlow.route
        ) {
            composable(Screens.CameraViewScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                CameraView().CameraView(navController, sharedViewModel)
            }
            composable(Screens.CameraPhotoScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                CameraPhotoView().CameraPhotoScreen(navController, sharedViewModel)
            }
        }


        navigation(
            startDestination = Screens.LoginScreen.route, route = Screens.LoginFlow.route
        ) {
            composable(Screens.LoginScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                LoginView().LoginScreen(navController, sharedViewModel)
            }
            composable(Screens.RegisterScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                RegisterView().RegisterScreen(navController, sharedViewModel)
            }
            composable(Screens.ForgotPasswordScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                ForgotPasswordView().ForgotPasswordScreen(sharedViewModel, navController)
            }
        }
        navigation(
            startDestination = Screens.ChatsViewScreen.route, route = Screens.MainFlow.route
        ) {
            composable(Screens.ProfilePictureView.route, enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 0)
                )
            }, exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 0)
                )
            }) {
                ProfilePictureView().ProfilePictureViewScreen(sharedViewModel, navController)
                onBottomBarDisabled.invoke(false)
            }
            composable(Screens.ChatsViewScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                LaunchedEffect(Unit) {
                    delay(50)
                    onBottomBarDisabled.invoke(true)
                }

                ChatsView().ChatsScreen(navController, sharedViewModel)
            }
            composable(Screens.DropInScreen.route, enterTransition = {
                fadeIn(animationSpec = tween(durationMillis = 0))
            }, exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                DropInView().DropInScreen(
                    navController = navController, sharedViewModel = sharedViewModel
                )
            }
            composable(Screens.ChatViewScreen.route, enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 0)
                )
            }, exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 0)
                )
            }) {
                onBottomBarDisabled.invoke(false)
                ChatView().ChatViewScreen(
                    navController = navController, sharedViewModel = sharedViewModel
                )
            }
            composable(Screens.RandChatScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                RandChatView().RandChatScreen(navController, sharedViewModel)
            }
            composable(Screens.FindUserScreen.route, enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            }, exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 500)
                )
            }) {
                LaunchedEffect(Unit) {
                    onBottomBarDisabled.invoke(false)
                }
                FindUserView().FindUserScreen(navController, sharedViewModel)
            }
            composable(Screens.ChatMateScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                ChatMateView().ChatMateScreen(navController, sharedViewModel)

            }
            composable(Screens.ProfileScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                ProfileView().ProfileScreen(navController, sharedViewModel)
            }
            composable(Screens.RandChatStartScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                RandChatStartView().RandChatStartScreen(navController, sharedViewModel)
            }
            composable(Screens.UserSheetScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                UserSheetView().UserSheetScreen(sharedViewModel, navController)
            }
            composable(Screens.ImageViewScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                ImageView().ImageViewScreen(sharedViewModel, navController)
            }
            composable(Screens.TagSelectScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                TagSelectView().TagSelectScreen(sharedViewModel, navController)
            }
            composable(Screens.PublicUserSheetScreen.route,
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                PublicUserSheetView().PublicUserSheetScreen(sharedViewModel, navController)
            }
        }
    }
}