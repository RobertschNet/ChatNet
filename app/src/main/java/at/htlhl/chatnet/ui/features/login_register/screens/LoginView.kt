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
import at.htlhl.chatnet.ui.features.login_register.components.LoginBottomBarComponent
import at.htlhl.chatnet.ui.features.login_register.components.LoginContentComponent
import at.htlhl.chatnet.ui.features.login_register.viewmodels.LoginRegisterViewModel
import at.htlhl.chatnet.util.checkIfValueIsValid
import at.htlhl.chatnet.util.loadImage
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch


class LoginView {

    @OptIn(ExperimentalComposeUiApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun LoginScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val loginRegisterViewModel = viewModel<LoginRegisterViewModel>()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1077068573755-8dqkdh2upl4h7rgkeab8slnv5dlps6c5.apps.googleusercontent.com")
                .requestEmail()
                .build()
        }
        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        var emailIsNotVerifiedText by remember { mutableStateOf(false) }
        var wrongPasswordText by remember { mutableStateOf(false) }
        var wrongEmailText by remember { mutableStateOf(false) }
        var loginErrorText by remember { mutableStateOf(false) }
        var accountDisabledOrNotFoundText by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var emailTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        var passwordTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        val emailColor =
            if (emailTexFieldColor == AccountDataState.Empty) Color.Gray else if (emailTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val passwordColor =
            if (passwordTexFieldColor == AccountDataState.Empty) Color.Gray else if (passwordTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        var isLoading by remember { mutableStateOf(false) }
        val activity = LocalContext.current as Activity
        val controller = LocalSoftwareKeyboardController.current
        val signInLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    isLoading = false
                }
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    sharedViewModel.unfinishedGoogleRegistration.value = account.idToken.toString()
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    account.email?.let { it ->
                                        loginRegisterViewModel.checkIfUserExists(email = it) {
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
                                                navController.navigate("MainFlow") {
                                                    popUpTo("LoginFlow") {
                                                        inclusive = true
                                                    }
                                                }
                                            } else {
                                                loginRegisterViewModel.auth.signOut()
                                                navController.navigate(Screens.RegisterWithGoogleScreen.route)
                                            }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    loginErrorText = true
                                }
                            }
                    }
                } catch (e: ApiException) {
                    isLoading = false
                    loginErrorText = true
                }
            }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            content = {
                LoginContentComponent(
                    email = email,
                    emailTexFieldColor = emailTexFieldColor,
                    emailIsNotVerifiedText = emailIsNotVerifiedText,
                    emailColor = emailColor,
                    wrongEmailText = wrongEmailText,
                    password = password,
                    passwordTexFieldColor = passwordTexFieldColor,
                    passwordColor = passwordColor,
                    wrongPasswordText = wrongPasswordText,
                    accountDisabledOrNotFoundText = accountDisabledOrNotFoundText,
                    loginErrorText = loginErrorText,
                    isLoading = isLoading,
                    onNavigate = {
                        navController.navigate(Screens.ForgotPasswordScreen.route)
                    },
                    onEmailValueChange = { emailText ->
                        email = emailText
                        emailTexFieldColor =
                            if (checkIfValueIsValid(type = TextFieldTypeState.EMAIL, value = email)) {
                                AccountDataState.Valid
                            } else {
                                AccountDataState.Invalid
                            }
                        if (email.isEmpty()) {
                            emailTexFieldColor = AccountDataState.Empty
                        }
                        emailIsNotVerifiedText = false
                        wrongEmailText = false
                        wrongPasswordText = false
                        accountDisabledOrNotFoundText = false
                        loginErrorText = false

                    },
                    onPasswordValueChange = { passwordText ->
                        password = passwordText
                        passwordTexFieldColor = if (checkIfValueIsValid(
                                type = TextFieldTypeState.PASSWORD,
                                value = password
                            )
                        ) {
                            AccountDataState.Valid
                        } else {
                            AccountDataState.Invalid
                        }
                        if (password.isEmpty()) {
                            passwordTexFieldColor = AccountDataState.Empty
                        }
                        emailIsNotVerifiedText = false
                        wrongEmailText = false
                        wrongPasswordText = false
                        accountDisabledOrNotFoundText = false
                        loginErrorText = false
                    },
                    onSignInPressed = {
                        isLoading = true
                        controller?.hide()
                        try {
                            loginRegisterViewModel.auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(activity) { task ->
                                    if (task.isSuccessful) {
                                        if (!loginRegisterViewModel.isUserEmailVerified()) {
                                            sharedViewModel.updateOnlineStatus(status = true)
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
                                            navController.navigate(Screens.MainFlow.route) {
                                                popUpTo(Screens.LoginFlow.route) {
                                                    inclusive = true
                                                }
                                            }

                                        } else {
                                            isLoading = false
                                            emailIsNotVerifiedText = true
                                            loginRegisterViewModel.auth.signOut()
                                        }
                                    } else {
                                        isLoading = false
                                        val exception = task.exception
                                        if (exception is FirebaseAuthException) {
                                            when (exception.errorCode) {
                                                "ERROR_INVALID_EMAIL" -> {
                                                    wrongEmailText = true
                                                }

                                                "ERROR_USER_DISABLED", "ERROR_USER_NOT_FOUND" -> {
                                                    accountDisabledOrNotFoundText = true
                                                }

                                                "ERROR_WRONG_PASSWORD" -> {
                                                    wrongPasswordText = true
                                                }
                                            }
                                        } else {
                                            loginErrorText = true
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            isLoading = false
                            loginErrorText = true
                        }
                    },
                    onGoogleSignInPressed = {
                        isLoading = true
                        googleSignInClient.signOut()
                        coroutineScope.launch {
                            val signInIntent = googleSignInClient.signInIntent
                            signInLauncher.launch(signInIntent)
                        }
                    }
                )
            },
            bottomBar = {
                LoginBottomBarComponent {
                    navController.navigate(Screens.RegisterScreen.route)
                }
            }
        )
    }
}