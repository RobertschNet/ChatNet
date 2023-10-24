package at.htlhl.chatnet.ui.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import at.htlhl.chatnet.R
import at.htlhl.chatnet.navigation.Screens
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

class RegisterView {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RegisterScreen(navController: NavController) {
        val authentication = FirebaseAuth.getInstance()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("967262210750-50ehlubm3783euk8ovqnjr0kf2mrcetb.apps.googleusercontent.com")
                .requestEmail()
                .build()
        }
        val signInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    authentication.signInWithCredential(credential)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                println("Sign-in successful")
                            } else {
                                println("Sign-in failed")
                            }
                        }
                }
            } catch (e: ApiException) {
                e.stackTrace
            }
        }
        Scaffold(
            containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
            content = {
                ContentView(
                    navController,
                    scope,
                    context,
                    googleSignInOptions,
                    signInLauncher
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
                text = "Already have an account?",
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                textAlign = TextAlign.Center
            )
            Text(
                text = " Sign in.",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate(Screens.LoginScreen.route)
                }
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun ContentView(
        navController: NavController,
        scope: CoroutineScope,
        context: Context,
        googleSignInOptions: GoogleSignInOptions,
        signInLauncher: ActivityResultLauncher<Intent>
    ) {
        var name by rememberSaveable { mutableStateOf("") }
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var openDialog by remember { mutableStateOf(false) }
        val regexPattern = "[A-Za-z0-9]+".toRegex()
        var usernameTexFieldColor by remember { mutableStateOf(Color.Gray) }
        var addressTexFieldColor by remember { mutableStateOf(Color.Gray) }
        val activity = LocalContext.current as Activity
        val toastContext = LocalContext.current
        val controller = LocalSoftwareKeyboardController.current
        auth = Firebase.auth
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(top = 100.dp),
                text = "Create an Account",
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                fontSize = 30.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 160.dp, start = 30.dp, end = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = usernameTexFieldColor,
                    unfocusedBorderColor = usernameTexFieldColor,
                    cursorColor = usernameTexFieldColor,
                    focusedLabelColor = usernameTexFieldColor,
                    unfocusedLabelColor = usernameTexFieldColor,
                ),
                value = name,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || regexPattern.matches(newValue)) {
                        name = newValue
                        if (name.isEmpty()) {
                            usernameTexFieldColor = Color.Gray
                        } else {
                            retrieveMessages(
                                name = name,
                                contextForToast = toastContext
                            ) { success, value ->
                                usernameTexFieldColor = if (success) {
                                    println("Retrieved value: $value")
                                    Color.Red
                                } else {
                                    println("Failed to retrieve value")
                                    Color.Green
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Username") },
            )
            OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = addressTexFieldColor,
                    unfocusedBorderColor = addressTexFieldColor,
                    cursorColor = addressTexFieldColor,
                    focusedLabelColor = addressTexFieldColor,
                    unfocusedLabelColor = addressTexFieldColor,
                ),
                value = email,
                onValueChange = {
                    email = it
                    addressTexFieldColor = Color.Gray
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Email") },
            )
            OutlinedTextField(
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = addressTexFieldColor,
                    unfocusedBorderColor = addressTexFieldColor,
                    cursorColor = addressTexFieldColor,
                    focusedLabelColor = addressTexFieldColor,
                    unfocusedLabelColor = addressTexFieldColor,
                ),
                value = password,
                onValueChange = {
                    password = it
                    addressTexFieldColor = Color.Gray
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Password") },
            )
            Button(
                onClick = {
                    controller?.hide()
                    try {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity) { task ->
                                if (task.isSuccessful) {
                                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                                    Toast.makeText(
                                        activity,
                                        "Authentication Successful.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    openDialog = true
                                    //sendVerificationEmail()
                                    createUserEntry(name)
                                    //navController.navigate(Screens.DropInScreen.Route)
                                } else {
                                    Log.w(
                                        ContentValues.TAG,
                                        "createUserWithEmail:failure",
                                        task.exception
                                    )
                                    Toast.makeText(
                                        activity,
                                        task.exception.toString(),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    addressTexFieldColor = Color.Red
                                }
                            }
                    } catch (e: Exception) {
                        Toast.makeText(
                            activity,
                            e.toString(),
                            Toast.LENGTH_SHORT,
                        ).show()
                        addressTexFieldColor = Color.Red
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = email.isNotEmpty() && password.isNotEmpty() && usernameTexFieldColor == Color.Green,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "Sign Up", color = Color.White, modifier = Modifier.padding(7.dp))
            }
            if (openDialog) {
                DialogBox2FA({ openDialog = false }, navController)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = {
                    scope.launch {
                        val googleSignInClient =
                            GoogleSignIn.getClient(context, googleSignInOptions)
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
                            modifier = Modifier.size(45.dp),
                            loading = {
                                CircularProgressIndicator()
                            }
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

    @Composable
    fun DialogBox2FA(onDismiss: () -> Unit, navController: NavController) {
        val contextForToast = LocalContext.current.applicationContext
        Dialog(
            onDismissRequest = {
                onDismiss()
                navController.navigate(Screens.LoginScreen.route)
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(color = Color(0xFF35898f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 16.dp)
                                .align(Alignment.Center),
                            imageVector = Icons.Default.Drafts,
                            contentDescription = "2-Step Verification",
                        )
                    }

                    Text(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        text = "2-Step Verification",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 20.sp
                        )
                    )

                    Text(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                        text = "Setup 2-Step Verification to add additional layer of security to your account.",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp
                        )
                    )

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp, start = 36.dp, end = 36.dp, bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35898f)),
                        onClick = {
                            onDismiss()
                            Toast.makeText(
                                contextForToast,
                                "Click: Setup Now",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                        Text(
                            text = "Setup Now",
                            color = Color.White,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 16.sp
                            )
                        )
                    }

                    TextButton(
                        onClick = {
                            onDismiss()
                            Toast.makeText(
                                contextForToast,
                                "Click: I'll Do It Later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                        Text(
                            text = "I'll Do It Later",
                            color = Color(0xFF35898f),
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
    /* TODO: Implement email verification

        private fun sendVerificationEmail() {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            user?.let {
                it.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            println("Email sent.")
                        } else {
                            println("Email not sent.")
                        }
                    }
            }
        }

     */

    private fun retrieveMessages(
        name: String,
        contextForToast: Context,
        callback: (Boolean, String?) -> Unit
    ) {
        val query = db.collection("usernames")
            .whereEqualTo("username", name)
            .limit(1)
        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val value = documentSnapshot.getString("username")
                    println("Retrieved value: $value")
                    Toast.makeText(
                        contextForToast,
                        "Username already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(true, value)
                } else {
                    println("Document not found")
                    callback(false, null)
                }
            }
            .addOnFailureListener { exception ->
                println("Error retrieving document: ${exception.message}")
                callback(false, exception.message)
            }
    }

    private fun createUserEntry(name: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("user").document(user!!.uid)
        val userData = hashMapOf(
            "blocked" to emptyList<String>(),
            "color" to "",
            "connection" to "",
            "email" to user.email,
            "id" to user.uid,
            "image" to "https://www.w3schools.com/howto/img_avatar2.png",
            "status" to "online",
            "username.lowercase" to name.lowercase(Locale.ROOT),
            "username.mixedcase" to name,
        )
        userRef.set(userData)
            .addOnSuccessListener {
                println("User successfully created")
            }
            .addOnFailureListener { e ->
                println("Error creating user: $e")
            }
    }

}