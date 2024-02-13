package at.htlhl.chatnet.ui.features.login_register.screens

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.data.TextFieldTypeState
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.login_register.components.RegisterWithGoogleBottomBarComponent
import at.htlhl.chatnet.ui.features.login_register.components.RegisterWithGoogleContentComponent
import at.htlhl.chatnet.ui.features.login_register.viewmodels.LoginRegisterViewModel
import at.htlhl.chatnet.util.checkIfValueIsValid
import at.htlhl.chatnet.util.loadImage
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider

class RegisterWithGoggleView {

    @OptIn(ExperimentalComposeUiApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun RegisterWithGoggleScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val loginRegisterViewModel = viewModel<LoginRegisterViewModel>()

        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1077068573755-8dqkdh2upl4h7rgkeab8slnv5dlps6c5.apps.googleusercontent.com")
                .requestEmail().build()
        }

        val googleSignInClient = GoogleSignIn.getClient(LocalContext.current, googleSignInOptions)
        var username by remember { mutableStateOf("") }
        var registerWithGoggleErrorText by remember { mutableStateOf(false) }
        var usernameExists by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var usernameTextFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        val usernameColor =
            if (usernameTextFieldColor == AccountDataState.Empty) Color.Gray else if (usernameTextFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val controller = LocalSoftwareKeyboardController.current
        val context = LocalContext.current
        Scaffold(containerColor = MaterialTheme.colorScheme.background, content = {
            RegisterWithGoogleContentComponent(username = username,
                usernameColor = usernameColor,
                usernameExists = usernameExists,
                isLoading = isLoading,
                registerWithGoggleErrorText = registerWithGoggleErrorText,
                usernameTextFieldColor = usernameTextFieldColor,
                onUsernameValueChange = { usernameText ->
                    username = usernameText
                    loginRegisterViewModel.checkIfUsernameExists(name = username) { success ->
                        usernameExists = success
                        usernameTextFieldColor = if (success || !checkIfValueIsValid(
                                type = TextFieldTypeState.USERNAME, value = username
                            )
                        ) {
                            AccountDataState.Invalid
                        } else {
                            AccountDataState.Valid
                        }
                        if (username.isEmpty()) {
                            usernameTextFieldColor = AccountDataState.Empty
                        }
                    }
                    if (username.isEmpty()) {
                        usernameTextFieldColor = AccountDataState.Empty
                    }
                    registerWithGoggleErrorText = false

                },
                onRegisterWithGooglePressed = {
                    isLoading = true
                    controller?.hide()
                    loginRegisterViewModel.auth.signInWithCredential(
                        GoogleAuthProvider.getCredential(
                            sharedViewModel.unfinishedGoogleRegistration.value, null
                        )
                    ).addOnCompleteListener {
                        if (it.isSuccessful) {
                            loginRegisterViewModel.createUserEntry(name = username, onSuccess = {
                                sharedViewModel.updateOnlineStatus(true)
                                sharedViewModel.getUserData {
                                    loadImage(
                                        context = context,
                                        imageUrl = sharedViewModel.user.value.image
                                    )
                                }
                                sharedViewModel.fetchFriendsFromUser {
                                    for (friend in sharedViewModel.friendListData.value) {
                                        loadImage(context, friend.image)
                                    }
                                }
                                sharedViewModel.fetchChatsWithMessages()
                                sharedViewModel.fetchRandomFriendsFromFriend()
                                navController.navigate(Screens.MainFlow.route) {
                                    popUpTo(Screens.RegisterWithGoogleScreen.route) {
                                        inclusive = true
                                    }
                                }

                            }, onFailure = {
                                isLoading = false
                                googleSignInClient.signOut()
                                loginRegisterViewModel.auth.signOut()
                                navController.navigate(Screens.LoginFlow.route) {
                                    popUpTo(Screens.RegisterWithGoogleScreen.route) {
                                        inclusive = true
                                    }
                                }
                            })
                        } else {
                            isLoading = false
                            registerWithGoggleErrorText = true

                        }
                    }.addOnFailureListener { _ ->
                        isLoading = false
                        registerWithGoggleErrorText = true
                    }
                }

            )
        }, bottomBar = {
            RegisterWithGoogleBottomBarComponent {
                navController.navigate(Screens.LoginFlow.route) {
                    popUpTo(Screens.RegisterWithGoogleScreen.route) {
                        inclusive = true
                    }
                }
            }
        })
    }
}