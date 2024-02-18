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
import at.chatnet.R
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.data.TextFieldTypeState
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.features.login_register.components.RegisterBottomBarComponent
import at.htlhl.chatnet.ui.features.login_register.components.RegisterContentComponent
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

class RegisterView {

    @OptIn(ExperimentalComposeUiApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun RegisterScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val loginRegisterViewModel = viewModel<LoginRegisterViewModel>()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString())
                .requestEmail()
                .build()
        }
        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        var registerErrorText by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var openDialog by remember { mutableStateOf(false) }
        var usernameExists by remember { mutableStateOf(false) }
        var emailExists by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var usernameTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        var emailTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        var passwordTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        val emailColor =
            if (emailTexFieldColor == AccountDataState.Empty) Color.Gray else if (emailTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val passwordColor =
            if (passwordTexFieldColor == AccountDataState.Empty) Color.Gray else if (passwordTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val usernameColor =
            if (usernameTexFieldColor == AccountDataState.Empty) Color.Gray else if (usernameTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
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
                                                        imageUrl = sharedViewModel.userData.value.image
                                                    )
                                                }
                                                sharedViewModel.fetchFriendsFromUser {
                                                    for (friend in sharedViewModel.friendListData.value) {
                                                        loadImage(context, friend.image)
                                                    }
                                                }
                                                sharedViewModel.fetchChatsWithMessages()

                                                sharedViewModel.fetchRandomFriendsFromFriend()
                                                navController.navigate(Screens.ChatsViewScreen.route)

                                            } else {
                                                navController.navigate(Screens.RegisterWithGoogleScreen.route) {
                                                    popUpTo(Screens.LoginFlow.route) {
                                                        inclusive = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    registerErrorText = true
                                }
                            }
                    }
                } catch (e: ApiException) {
                    registerErrorText = true
                }
            }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            content = {
                RegisterContentComponent(
                    username = username,
                    email = email,
                    password = password,
                    usernameTexFieldColor = usernameTexFieldColor,
                    emailTexFieldColor = emailTexFieldColor,
                    passwordTexFieldColor = passwordTexFieldColor,
                    usernameColor = usernameColor,
                    emailColor = emailColor,
                    passwordColor = passwordColor,
                    usernameExists = usernameExists,
                    emailExists = emailExists,
                    registerErrorText = registerErrorText,
                    isLoading = isLoading,
                    openDialog = openDialog,
                    onNavigate = {
                        navController.navigate(Screens.LoginScreen.route)
                        openDialog = false
                    },
                    onUsernameValueChange = { usernameText ->
                        username = usernameText
                        loginRegisterViewModel.checkIfUsernameExists(name = username) { success ->
                            usernameExists = success
                            usernameTexFieldColor =
                                if (success || !checkIfValueIsValid(
                                        type = TextFieldTypeState.USERNAME,
                                        value = username
                                    )
                                ) {
                                    AccountDataState.Invalid
                                } else {
                                    AccountDataState.Valid
                                }
                            if (username.isEmpty()) {
                                usernameTexFieldColor = AccountDataState.Empty
                            }
                        }
                        if (username.isEmpty()) {
                            usernameTexFieldColor = AccountDataState.Empty
                        }
                        registerErrorText = false
                    },
                    onEmailValueChange = { emailText ->
                        email = emailText
                        loginRegisterViewModel.checkIfEmailExists(email = email) { success ->
                            emailExists = success
                            emailTexFieldColor =
                                if (success || !checkIfValueIsValid(
                                        type = TextFieldTypeState.EMAIL,
                                        value = email
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
                    },
                    onPasswordValueChange = { passwordText ->
                        password = passwordText
                        passwordTexFieldColor =
                            if (!checkIfValueIsValid(type = TextFieldTypeState.PASSWORD, value = password)) {
                                AccountDataState.Invalid
                            } else {
                                AccountDataState.Valid
                            }
                        if (password.isEmpty()) {
                            passwordTexFieldColor = AccountDataState.Empty
                        }
                        registerErrorText = false
                    },
                    onRegisterPressed = {
                        isLoading = true
                        controller?.hide()
                        try {
                            loginRegisterViewModel.auth.createUserWithEmailAndPassword(
                                email,
                                password
                            )
                                .addOnCompleteListener(activity) { task ->
                                    if (task.isSuccessful) {
                                        loginRegisterViewModel.createUserEntry(
                                            name = username,
                                            onSuccess = {
                                                loginRegisterViewModel.sendVerificationEmail {
                                                    if (it) {
                                                        openDialog = true
                                                        loginRegisterViewModel.auth.signOut()
                                                    } else {
                                                        loginRegisterViewModel.auth.currentUser?.delete()
                                                    }
                                                    isLoading = false
                                                }
                                            }
                                        )
                                    } else {
                                        isLoading = false
                                        registerErrorText = true
                                    }
                                }
                        } catch (e: Exception) {
                            isLoading = false
                            registerErrorText = true
                        }
                    },
                    onGoogleRegisterPressed = {
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
                RegisterBottomBarComponent {
                    navController.navigate(Screens.LoginScreen.route) {
                        popUpTo(Screens.RegisterScreen.route) {
                            inclusive = true
                        }
                    }
                }
            }
        )
    }
}