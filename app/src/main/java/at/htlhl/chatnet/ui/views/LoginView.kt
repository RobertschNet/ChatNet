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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily.Companion.Cursive
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Light
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.chatnet.R
import at.htlhl.chatnet.viewmodels.SharedViewModel
import at.htlhl.chatnet.navigation.Screens
import coil.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class LoginView {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        sharedViewModel.bottomBarState.value = false
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
                                sharedViewModel.startListeningForFriends()
                                sharedViewModel.startListeningForMessagesForPairs(
                                    {},
                                    {})
                                sharedViewModel.gpsState.value = false
                                navController.navigate(Screens.ChatsViewScreen.route)
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
                    signInLauncher,
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


    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    @Composable
    fun ContentView(
        navController: NavController,
        scope: CoroutineScope,
        context: Context,
        googleSignInOptions: GoogleSignInOptions,
        signInLauncher: ActivityResultLauncher<Intent>,
        sharedViewModel: SharedViewModel
    ) {
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        val activity = LocalContext.current as Activity
        val controller = LocalSoftwareKeyboardController.current
        auth = Firebase.auth
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier.padding(top = 100.dp),
                text = "ChatNet",
                fontWeight = Bold,
                fontFamily = Cursive,
                fontSize = 45.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 165.dp, start = 30.dp, end = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Email/Username") },
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
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
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(activity) { task ->
                                if (task.isSuccessful) {
                                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                                    if (!isUserEmailVerified()) {
                                        sharedViewModel.startListeningForFriends()
                                        sharedViewModel.startListeningForMessagesForPairs(
                                            {},
                                            {})
                                        sharedViewModel.gpsState.value = false
                                        navController.navigate(Screens.ChatsViewScreen.route)
                                    } else {
                                        Toast.makeText(
                                            activity,
                                            "Please verify your email.",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        auth.signOut()
                                    }
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
                                }
                            }
                    } catch (e: Exception) {
                        Toast.makeText(
                            activity,
                            e.toString(),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = email.isNotEmpty() && password.isNotEmpty(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "Sign In", color = Color.White, modifier = Modifier.padding(7.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
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
                    textAlign = TextAlign.Center
                )
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

    private fun isUserEmailVerified(): Boolean {
        val user = auth.currentUser
        println(user)
        return user?.isEmailVerified == true
    }
}