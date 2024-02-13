package at.htlhl.chatnet.ui.features.login_register.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import at.htlhl.chatnet.ui.features.login_register.components.ForgotPasswordBottomBarComponent
import at.htlhl.chatnet.ui.features.login_register.components.ForgotPasswordContentComponent
import at.htlhl.chatnet.ui.features.login_register.viewmodels.LoginRegisterViewModel
import at.htlhl.chatnet.util.checkIfValueIsValid
import at.htlhl.chatnet.util.loadImage
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class ForgotPasswordView {

    @OptIn(ExperimentalComposeUiApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun ForgotPasswordScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val loginRegisterViewModel = viewModel<LoginRegisterViewModel>()
        val coroutineScope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1077068573755-8dqkdh2upl4h7rgkeab8slnv5dlps6c5.apps.googleusercontent.com")
                .requestEmail().build()
        }
        val googleSignInClient = GoogleSignIn.getClient(LocalContext.current, googleSignInOptions)
        var forgotPasswordDialog by remember { mutableStateOf(false) }
        var registerErrorText by remember { mutableStateOf(false) }
        var passwordResetErrorText by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var emailExists by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var emailTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        val emailColor =
            if (emailTexFieldColor == AccountDataState.Empty) Color.Gray else if (emailTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val controller = LocalSoftwareKeyboardController.current
        val context = LocalContext.current
        val signInLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    isLoading = false
                }
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    account.email?.let { it ->
                                        loginRegisterViewModel.checkIfUserExists(it) {
                                            if (it) {
                                                sharedViewModel.updateOnlineStatus(true)
                                                sharedViewModel.getUserData {
                                                    loadImage(
                                                        context = context,
                                                        imageUrl = sharedViewModel.user.value.image
                                                    )
                                                }
                                                sharedViewModel.fetchFriendsFromUser {
                                                    for (friend in sharedViewModel.friendListData.value) {
                                                        loadImage(
                                                            context = context,
                                                            imageUrl = friend.image
                                                        )
                                                    }
                                                }
                                                sharedViewModel.fetchChatsWithMessages()
                                                sharedViewModel.fetchRandomFriendsFromFriend()
                                                navController.navigate(Screens.ChatsViewScreen.route)

                                            } else {
                                                navController.navigate(Screens.RegisterWithGoogleScreen.route) {
                                                    popUpTo("LoginFlow") {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    registerErrorText = true
                                }
                            }
                    }
                } catch (e: ApiException) {
                    isLoading = false
                    registerErrorText = true
                }
            }
        Scaffold(containerColor = MaterialTheme.colorScheme.background, content = {
            ForgotPasswordContentComponent(email = email,
                emailTexFieldColor = emailTexFieldColor,
                emailExists = emailExists,
                emailColor = emailColor,
                registerErrorText = registerErrorText,
                passwordResetErrorText = passwordResetErrorText,
                forgotPasswordDialog = forgotPasswordDialog,
                isLoading = isLoading,
                onEmailValueChange = { emailText ->
                    email = emailText
                    loginRegisterViewModel.checkIfEmailExists(email = email) { success ->
                        emailExists = success
                        emailTexFieldColor = if (!success || !checkIfValueIsValid(
                                type = TextFieldTypeState.EMAIL, value = email
                            )
                        ) {
                            AccountDataState.Invalid
                        } else {
                            AccountDataState.Valid
                        }
                        if (email.isEmpty()) {
                            emailTexFieldColor = AccountDataState.Empty
                        }
                    }
                    if (email.isEmpty()) {
                        emailTexFieldColor = AccountDataState.Empty
                    }
                    registerErrorText = false
                    passwordResetErrorText = false
                },
                onResetPasswordPressed = {
                    isLoading = true
                    controller?.hide()
                    loginRegisterViewModel.auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                forgotPasswordDialog = true
                            } else {
                                isLoading = false
                                passwordResetErrorText = true
                            }
                        }
                },
                onGoogleSignInPressed = {
                    isLoading = true
                    googleSignInClient.signOut()
                    coroutineScope.launch {
                        val signInIntent = googleSignInClient.signInIntent
                        signInLauncher.launch(signInIntent)
                    }
                },
                onPasswordResetEmailSent = {
                    isLoading = false
                    forgotPasswordDialog = false
                    navController.navigate(Screens.LoginScreen.route)
                }
            )
        }, bottomBar = {
            ForgotPasswordBottomBarComponent {
                navController.navigate(Screens.LoginScreen.route) {
                    popUpTo(Screens.ForgotPasswordScreen.route) {
                        inclusive = true
                    }
                }
            }
        })
    }
}