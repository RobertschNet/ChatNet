package at.htlhl.chatnet.navigation

import at.htlhl.chatnet.ui.features.tags.screens.TagSelectView
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
import at.htlhl.chatnet.ui.views.CameraPhotoView
import at.htlhl.chatnet.ui.views.CameraView
import at.htlhl.chatnet.ui.features.chatmate.screens.ChatMateView
import at.htlhl.chatnet.ui.features.chat.ChatView
import at.htlhl.chatnet.ui.features.chats.screens.ChatsView
import at.htlhl.chatnet.ui.features.dropin.screens.DropInView
import at.htlhl.chatnet.ui.features.finduser.screens.FindUserView
import at.htlhl.chatnet.ui.features.login_register.screens.ForgotPasswordView
import at.htlhl.chatnet.ui.views.ImageView
import at.htlhl.chatnet.ui.features.login_register.screens.LoginView
import at.htlhl.chatnet.ui.views.ProfileInfoView
import at.htlhl.chatnet.ui.views.ProfilePictureView
import at.htlhl.chatnet.ui.features.profile.screens.ProfileView
import at.htlhl.chatnet.ui.views.PublicProfileView
import at.htlhl.chatnet.ui.features.randchat.screens.RandChatStartView
import at.htlhl.chatnet.ui.features.randchat.screens.RandChatView
import at.htlhl.chatnet.ui.features.login_register.screens.RegisterView
import at.htlhl.chatnet.ui.features.login_register.screens.RegisterWithGoggleView
import at.htlhl.chatnet.viewmodels.SharedViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    startView: String,
    context: Context,
    onBottomBarDisabled: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = startView) {
        composable("RegisterWithGoogleScreen",
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
            RegisterWithGoggleView().RegisterWithGoggleScreen(sharedViewModel, navController)
        }

        navigation(
            startDestination = "CameraViewScreen",
            route = "CameraFlow"
        ){
            composable("CameraViewScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                CameraView().CameraView(navController, sharedViewModel, context)
            }
            composable("CameraPhotoScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                CameraPhotoView().CameraPhotoScreen(navController, sharedViewModel)
            }
        }


        navigation(
            startDestination = "LoginScreen",
            route = "LoginFlow"
        ) {
            composable("LoginScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                LoginView().LoginScreen(navController, sharedViewModel)
            }
            composable("RegisterScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                RegisterView().RegisterScreen(navController, sharedViewModel)
            }
            composable("ForgotPasswordScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                ForgotPasswordView().ForgotPasswordScreen(sharedViewModel, navController)
            }
        }
        navigation(
            startDestination = "ChatsViewScreen",
            route = "MainFlow"
        )
        {
            composable("ProfilePictureScreen",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 0)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 0)
                    )
                }
                ){
                ProfilePictureView().ProfilePictureViewScreen(sharedViewModel, navController)
                onBottomBarDisabled.invoke(false)
            }
            composable("ChatsViewScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
            ) {
                LaunchedEffect(Unit) {
                    delay(50)
                    onBottomBarDisabled.invoke(true)
                }
                ChatsView().ChatsScreen(navController, sharedViewModel)
            }
            composable("DropInScreen",
                enterTransition = {
                    fadeIn(animationSpec = tween(durationMillis = 0))
                },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
            ) {
                onBottomBarDisabled.invoke(true)
                DropInView().DropInScreen(
                    navController = navController,
                    sharedViewModel = sharedViewModel
                )
            }
            composable("ChatViewScreen",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 0)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 0)
                    )
                }
            ) {
                onBottomBarDisabled.invoke(false)
                ChatView().ChatViewScreen(
                    navController = navController,
                    sharedViewModel = sharedViewModel
                )
            }
            composable("RandChatScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                RandChatView().RandChatScreen(navController, sharedViewModel)
            }
            composable("FindUserScreen",
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
                LaunchedEffect(Unit){
                    onBottomBarDisabled.invoke(false)
                }
                FindUserView().FindUserScreen(navController, sharedViewModel)
            }
            composable("ChatMateScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ChatMateView().ChatMateScreen(navController, sharedViewModel)
                }
            }
            composable("ProfileScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                ProfileView().ProfileScreen(navController, sharedViewModel)
            }
            composable("RandChatStartScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(true)
                RandChatStartView().RandChatStartScreen(navController, sharedViewModel)
            }
            composable("ProfileInfoScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                ProfileInfoView().ProfileInfoScreen(sharedViewModel, navController)
            }
            composable("ImageViewScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                ImageView().ImageViewScreen(sharedViewModel, navController)
            }
            composable("TagSelectScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                TagSelectView().TagSelectScreen(sharedViewModel,navController)
            }
            composable("PublicProfileScreen",
                enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }) {
                onBottomBarDisabled.invoke(false)
                PublicProfileView().PublicProfileScreen(sharedViewModel, navController)
            }
        }
    }
}