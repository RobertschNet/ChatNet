package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.components.mixed.CreateUserWithGoogle
import at.htlhl.chatnet.ui.components.mixed.PasswordResetEmailDialog
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

class ForgotPasswordView {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun ForgotPasswordScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val scope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString())
                .requestEmail()
                .build()
        }
        val googleSignInClient = GoogleSignIn.getClient(LocalContext.current, googleSignInOptions)
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
            bottomBar = { BottomScreen(navController) }
        )
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
                text = "Back to Login",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate(Screens.LoginScreen.route){
                        popUpTo(Screens.ForgotPasswordScreen.route){
                            inclusive = true
                        }
                    }
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
        sharedViewModel: SharedViewModel
    ) {
        val createAccountWithGoogleDialog = remember { mutableStateOf(false) }
        val forgotPasswordDialog = remember { mutableStateOf(false) }
        var registerErrorText by remember { mutableStateOf(false) }
        var passwordResetErrorText by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        var emailExists by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var emailTexFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        val emailColor =
            if (emailTexFieldColor == AccountDataState.Empty) Color.Gray else if (emailTexFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val controller = LocalSoftwareKeyboardController.current
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
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                account.email?.let { it ->
                                    checkIfUserExists(it) {
                                        println("Sign-in successful")
                                        if (it) {
                                            sharedViewModel.updateOnlineStatus("online")
                                            sharedViewModel.getUserData()
                                            sharedViewModel.fetchFriendsFromUser()
                                            sharedViewModel.fetchChatsWithMessages {
                                                sharedViewModel.fetchRandomFriendsFromFriend()
                                                navController.navigate(Screens.ChatsViewScreen.route)
                                            }
                                        } else {
                                            navController.navigate(Screens.RegisterWithGoogleScreen.route){
                                                popUpTo("LoginFlow"){
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 30.dp, end = 30.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 30.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Enter your email and we will send you a link to reset your password.",
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
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
                        if (email.isNotEmpty()) {
                            Column {
                                if (!checkIfValueIsValid(email)) {
                                    Text(
                                        text = "Email is invalid",
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        fontFamily = FontFamily.SansSerif,
                                    )
                                } else if (!emailExists) {
                                    Text(
                                        text = "Email is not registered",
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        fontFamily = FontFamily.SansSerif,
                                    )
                                }
                            }
                        }
                    },
                    onValueChange = {
                        email = it
                        checkIfEmailExists(email = email) { success ->
                            emailExists = success
                            emailTexFieldColor =
                                if (!success || !checkIfValueIsValid(email)) {
                                    AccountDataState.Invalid
                                } else {
                                    AccountDataState.Valid
                                }
                            if (email.isEmpty()) {
                                emailTexFieldColor = AccountDataState.Empty
                            }
                        }
                        registerErrorText = false
                        passwordResetErrorText = false
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = "Email") },
                )
                Button(
                    onClick = {
                        isLoading = true
                        controller?.hide()
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    forgotPasswordDialog.value = true
                                } else {
                                    isLoading = false
                                    passwordResetErrorText = true
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    enabled = emailTexFieldColor == AccountDataState.Valid && !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(35.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Reset Password",
                            color = Color.White,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                }
                if (registerErrorText) {
                    Text(
                        text = "Registration failed please try again later!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                if (passwordResetErrorText) {
                    Text(
                        text = "Password reset failed please try again later!",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
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
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
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
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        enabled = !isLoading,
                        onClick = {
                            isLoading = true
                            scope.launch {
                                val signInIntent = googleSignInClient.signInIntent
                                signInLauncher.launch(signInIntent)
                            }
                        }) {
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
                                text = "Sign up with Google",
                                color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
            }
        }
        if (forgotPasswordDialog.value) {
            PasswordResetEmailDialog(
                onDismiss = {
                    isLoading = false
                    forgotPasswordDialog.value = false
                    navController.navigate(Screens.LoginScreen.route)
                }
            )
        }
        if (createAccountWithGoogleDialog.value) {
            CreateUserWithGoogle(
                onClose = {
                    isLoading = false
                    createAccountWithGoogleDialog.value = false
                    if (it != "") {
                        createUserEntry(auth, it) {
                            sharedViewModel.updateOnlineStatus("online")
                            sharedViewModel.getUserData()
                            sharedViewModel.fetchFriendsFromUser()
                            sharedViewModel.fetchChatsWithMessages {
                                sharedViewModel.fetchRandomFriendsFromFriend()
                                navController.navigate(Screens.ChatsViewScreen.route)
                            }
                        }
                    } else {
                        googleSignInClient.signOut()
                        FirebaseAuth.getInstance().signOut()
                    }
                },
            )
        }
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

    private fun createUserEntry(
        account: FirebaseAuth,
        name: String,
        onSuccess: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(account.currentUser?.uid.toString())
        val userData = hashMapOf(
            "blocked" to emptyList<String>(),
            "pinned" to emptyList<String>(),
            "color" to "",
            "connected" to false,
            "email" to account.currentUser?.email.toString(),
            "id" to account.currentUser?.uid.toString(),
            "image" to "https://www.w3schools.com/howto/img_avatar2.png",
            "status" to "online",
            "username" to mapOf(
                "lowercase" to name.lowercase(Locale.ROOT),
                "mixedcase" to name,
            ),
        )
        userRef.set(userData)
            .addOnSuccessListener {
                println("User successfully created")
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                println("Error creating user: $e")
            }
    }

    private fun checkIfEmailExists(
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .limit(1)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val usernameField = documentSnapshot.get("username.lowercase")
                    if (usernameField is String) {
                        println("Retrieved value: $usernameField")
                        callback(true)
                    } else {
                        println("Field 'username.value' is not a String")
                        callback(false)
                    }
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

    private fun checkIfValueIsValid(value: String): Boolean {
        return value.matches("^(?=.{1,320})(?!.*[+._-]{2})(?![+._-])[a-zA-Z0-9+._-]{1,64}(?<![+._-])@(?![+._-])[a-zA-Z0-9.-]*\\.[a-zA-Z]{2,63}(?<![+._-])$".toRegex())
    }
}