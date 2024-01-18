package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Light
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class LoginView {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun LoginScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        sharedViewModel.bottomBarState.value = false
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1077068573755-8dqkdh2upl4h7rgkeab8slnv5dlps6c5.apps.googleusercontent.com")
                .requestEmail()
                .build()
        }

        val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
        Scaffold(
            containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            content = {
                ContentView(
                    navController,
                    scope,
                    googleSignInClient,
                    sharedViewModel
                )

            },
            bottomBar = { BottomScreen(navController) })
    }

    @Composable
    fun BottomScreen(navController: NavController) {
        Divider(
            thickness = 0.3f.dp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 50.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account?",
                fontWeight = Light,
                fontFamily = SansSerif,
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                textAlign = TextAlign.Center
            )
            Text(
                text = " Sign up.",
                fontWeight = Bold,
                fontFamily = SansSerif,
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate(Screens.RegisterScreen.route)
                }
            )
        }
    }


    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun ContentView(
        navController: NavController,
        scope: CoroutineScope,
        googleSignInClient: GoogleSignInClient,
        sharedViewModel: SharedViewModel,
    ) {
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
        val context = LocalContext.current
        val controller = LocalSoftwareKeyboardController.current
        val authentication = FirebaseAuth.getInstance()
        auth = Firebase.auth
        val signInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                isLoading = false
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                sharedViewModel.unfinishedGoogleRegistration.value = account.idToken.toString()
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    authentication.signInWithCredential(credential)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                account.email?.let { it ->
                                    checkIfUserExists(it) {
                                        println("Sign-in successful")
                                        if (it) {
                                            sharedViewModel.updateOnlineStatus("online")
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
                                            navController.navigate("MainFlow") {
                                                popUpTo("LoginFlow") {
                                                    inclusive = true
                                                }
                                            }
                                        } else {
                                            auth.signOut()
                                            navController.navigate(Screens.RegisterWithGoogleScreen.route)
                                            Log.println(Log.INFO, "User", "User does not exist")
                                        }
                                    }
                                }
                            } else {
                                Log.println(Log.ERROR, "User", "Sign-in failed")
                                isLoading = false
                                loginErrorText = true
                            }
                        }
                }
            } catch (e: ApiException) {
                Log.println(Log.ERROR, "User", e.status.toString())
                isLoading = false
                loginErrorText = true
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp, start = 30.dp, end = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ChatNet",
                        fontWeight = Medium,
                        fontFamily = SansSerif,
                        fontSize = 45.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                OutlinedTextField(
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = emailColor,
                        focusedBorderColor = emailColor,
                        unfocusedBorderColor = emailColor,
                        focusedLabelColor = emailColor,
                        unfocusedLabelColor = emailColor,
                    ),
                    value = email,
                    supportingText = {
                        if (!checkIfValueIsValid("email", email) && email.isNotEmpty()) {
                            Text(
                                text = "Email is invalid",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = Light,
                                fontFamily = SansSerif,
                            )
                        }
                    },
                    onValueChange = {
                        email = it
                        emailTexFieldColor = if (checkIfValueIsValid("email", email)) {
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = "Email") },
                )
                OutlinedTextField(
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = passwordColor,
                        focusedBorderColor = passwordColor,
                        unfocusedBorderColor = passwordColor,
                        focusedLabelColor = passwordColor,
                        unfocusedLabelColor = passwordColor,
                    ),
                    value = password,
                    supportingText = {
                        if (!checkIfValueIsValid("password", password) && password.isNotEmpty()) {
                            Text(
                                text = "Password is invalid",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = Light,
                                fontFamily = SansSerif,
                            )
                        }
                    },
                    onValueChange = {
                        password = it
                        passwordTexFieldColor = if (checkIfValueIsValid("password", password)) {
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = "Password") },
                )
                Button(
                    onClick = {
                        isLoading = true
                        controller?.hide()
                        try {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(activity) { task ->
                                    if (task.isSuccessful) {
                                        Log.d(ContentValues.TAG, "createUserWithEmail:success")
                                        if (!isUserEmailVerified()) {
                                            sharedViewModel.updateOnlineStatus("online")
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
                                            navController.navigate("MainFlow") {
                                                popUpTo("LoginFlow") {
                                                    inclusive = true
                                                }
                                            }

                                        } else {
                                            isLoading = false
                                            emailIsNotVerifiedText = true
                                            auth.signOut()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    enabled = passwordTexFieldColor == AccountDataState.Valid && emailTexFieldColor == AccountDataState.Valid && !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05C205))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(35.dp)
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontFamily = SansSerif,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                }
                if (emailIsNotVerifiedText) {
                    Text(
                        text = "Please verify your email first!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = Light,
                        fontFamily = SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                if (wrongEmailText) {
                    Text(
                        text = "Email does not exist!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = Light,
                        fontFamily = SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                if (wrongPasswordText) {
                    Text(
                        text = "Wrong password!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = Light,
                        fontFamily = SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                if (loginErrorText) {
                    Text(
                        text = "Login failed please try again later!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = Light,
                        fontFamily = SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                if (accountDisabledOrNotFoundText) {
                    Text(
                        text = "Account disabled or not found!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = Light,
                        fontFamily = SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Forgot your password?",
                        fontWeight = Light,
                        fontFamily = SansSerif,
                        fontSize = 14.sp,
                        color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = " Reset password.",
                        fontWeight = Bold,
                        fontFamily = SansSerif,
                        fontSize = 14.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable {
                            navController.navigate(Screens.ForgotPasswordScreen.route)
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Divider(
                        thickness = 0.3f.dp,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .weight(1f)
                    )
                    Text(
                        text = "OR",
                        fontWeight = Medium,
                        fontFamily = SansSerif,
                        fontSize = 14.sp,
                        color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                    )
                    Divider(
                        thickness = 0.3f.dp,
                        color = Color.DarkGray,
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .weight(1f)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        enabled = !isLoading,
                        onClick = {
                            isLoading = true
                            googleSignInClient.signOut()
                            scope.launch {
                                val signInIntent = googleSignInClient.signInIntent
                                signInLauncher.launch(signInIntent)
                            }
                        }
                    )
                    {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SubcomposeAsyncImage(
                                model = R.drawable.icon_google,
                                contentDescription = "Google",
                                modifier = Modifier.size(45.dp)
                            )
                            Text(
                                text = "Sign in with Google",
                                color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
                                fontSize = 16.sp,
                                fontWeight = Medium,
                                fontFamily = SansSerif,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isUserEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    private fun checkIfUserExists(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .limit(1)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    println("Document found")
                    callback(true)
                } else {
                    println("Document not found")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                println("Error retrieving document: ${exception.message}")
                callback(false)
            }
    }

    private fun checkIfValueIsValid(type: String, value: String): Boolean {
        return when (type) {
            "email" -> {
                value.matches("^(?=.{1,320})(?!.*[+._-]{2})(?![+._-])[a-zA-Z0-9+._-]{1,64}(?<![+._-])@(?![+._-])[a-zA-Z0-9.-]*\\.[a-zA-Z]{2,63}(?<![+._-])$".toRegex())
            }

            "password" -> {
                value.matches("^(?!.*\\s).{6,4096}$".toRegex())
            }

            else -> {
                false
            }
        }
    }

    private fun loadImage(context: Context, imageUrl: String) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()
        context.imageLoader.enqueue(request)
    }
}