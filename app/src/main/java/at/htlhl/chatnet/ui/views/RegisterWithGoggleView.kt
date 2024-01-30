package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import androidx.core.view.accessibility.AccessibilityViewCommand.ScrollToPositionArguments
import androidx.navigation.NavController
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class RegisterWithGoggleView {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun RegisterWithGoggleScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1077068573755-8dqkdh2upl4h7rgkeab8slnv5dlps6c5.apps.googleusercontent.com")
                .requestEmail()
                .build()
        }

        val googleSignInClient = GoogleSignIn.getClient(LocalContext.current, googleSignInOptions)
        Scaffold(
            containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            content = {
                ContentView(
                    navController,
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
                text = "Already have an account?",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate("LoginFlow") {
                        popUpTo(Screens.RegisterWithGoogleScreen.route) {
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
        googleSignInClient: GoogleSignInClient,
        sharedViewModel: SharedViewModel
    ) {
        var username by remember { mutableStateOf("") }
        var registerWithGoggleErrorText by remember { mutableStateOf(false) }
        var usernameExists by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var usernameTextFieldColor by remember { mutableStateOf(AccountDataState.Empty) }
        val usernameColor = if (usernameTextFieldColor == AccountDataState.Empty) Color.Gray else if (usernameTextFieldColor == AccountDataState.Valid) MaterialTheme.colorScheme.primary else Color.Red
        val controller = LocalSoftwareKeyboardController.current
        val context = LocalContext.current
        auth = Firebase.auth
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
                        text = "Create Account",
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
                        text = "Enter a username in the field below and click 'Create Account' to create a new account.",
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 16.sp,
                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                OutlinedTextField(
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = usernameColor,
                        focusedBorderColor = usernameColor,
                        unfocusedBorderColor = usernameColor,
                        focusedLabelColor = usernameColor,
                        unfocusedLabelColor = usernameColor,
                    ),
                    value = username,
                    supportingText = {
                        if (username.isNotEmpty()) {
                            Column {
                                if (!checkIfValueIsValid(username)) {
                                    Text(
                                        text = "Username is not valid",
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                        fontFamily = FontFamily.SansSerif,
                                    )
                                } else if (usernameExists) {
                                    Text(
                                        text = "Username already exists",
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
                        username = it
                        checkIfUsernameExists(name = username) { success ->
                            usernameExists = success
                            usernameTextFieldColor =
                                if (success || !checkIfValueIsValid(username)) {
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    label = { Text(text = "Username") },
                )
                Button(
                    onClick = {
                        isLoading = true
                        controller?.hide()
                        auth.signInWithCredential( GoogleAuthProvider.getCredential(sharedViewModel.unfinishedGoogleRegistration.value, null))
                            .addOnCompleteListener{
                                if (it.isSuccessful) {
                                   Log.println(Log.INFO, "Google", "Success")
                                    createUserEntry(auth, username, {
                                        sharedViewModel.updateOnlineStatus("online")
                                        sharedViewModel.getUserData{
                                            loadImage(context = context , imageUrl = sharedViewModel.user.value.image)
                                        }
                                        sharedViewModel.fetchFriendsFromUser{
                                            for (friend in sharedViewModel.friendListData.value) {
                                                loadImage(context, friend.image)
                                            }
                                        }
                                        sharedViewModel.fetchChatsWithMessages()
                                            sharedViewModel.fetchRandomFriendsFromFriend()
                                            navController.navigate("MainFlow") {
                                                popUpTo(Screens.RegisterWithGoogleScreen.route) {
                                                    inclusive = true
                                                }
                                            }

                                    }, {
                                        isLoading = false
                                        googleSignInClient.signOut()
                                        auth.signOut()
                                        navController.navigate("LoginFlow") {
                                            popUpTo(Screens.RegisterWithGoogleScreen.route) {
                                                inclusive = true
                                            }
                                        }
                                    })
                                } else {
                                    isLoading = false
                                    registerWithGoggleErrorText = true
                                    Log.println(Log.INFO, "Google", "Error: ${it.exception?.message}")
                                }
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                registerWithGoggleErrorText = true
                                Log.println(Log.INFO, "Google", "Error: ${exception.message}")
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    enabled = usernameTextFieldColor == AccountDataState.Valid && !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05C205))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(35.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                }
                if (registerWithGoggleErrorText) {
                    Text(
                        text = "Couldn't create account. Try again later.",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

            }
        }
    }

    private fun checkIfUsernameExists(
        name: String,
        callback: (Boolean) -> Unit
    ) {
        val query = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("username.lowercase", name.lowercase(Locale.ROOT))
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


    private fun createUserEntry(
        account: FirebaseAuth,
        name: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(account.currentUser?.uid.toString())
        val userData = hashMapOf(
            "blocked" to emptyList<String>(),
            "pinned" to emptyList<String>(),
            "color" to "",
            "connected" to false,
            "muted" to emptyList<String>(),
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
                onFailure.invoke()
            }
    }

    private fun checkIfValueIsValid(value: String): Boolean {
        return value.matches("^(?!.*[._-]{2})(?![._-])[a-zA-Z0-9._-]{1,30}(?<![._-])$".toRegex())
    }
    private fun loadImage(context: Context, imageUrl: String) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .build()
        context.imageLoader.enqueue(request)
    }
}