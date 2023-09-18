package at.htlhl.testing.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.testing.R
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Screens
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class Profile {
    private lateinit var auth: FirebaseAuth

    @Composable
    fun ProfileScreen(navController: NavController,sharedViewModel: SharedViewModel) {
        auth = Firebase.auth
        val currentUser = auth.currentUser?.uid
        var name by remember { mutableStateOf("") }
        var lastMessage by remember { mutableStateOf("") }
        var image by remember { mutableStateOf("") }
        val auth = FirebaseAuth.getInstance()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val googleSignInOptions = remember {
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.web_client_id.toString())
                .requestEmail()
                .build()
        }

        val logout = {
            scope.launch {
                val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
                auth.signOut()
                googleSignInClient.signOut()
                    .addOnCompleteListener {
                    }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Input") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Green,
                    unfocusedBorderColor = Yellow
                ),
                placeholder = { Text(text = "name", color = Color.Cyan) },
            )
            OutlinedTextField(
                value = lastMessage,
                onValueChange = { lastMessage = it },
                label = { Text("Input") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Green,
                    unfocusedBorderColor = Yellow
                ),
                placeholder = { Text(text = "lastMessage", color = Color.Cyan) },
            )
            OutlinedTextField(
                value = image,
                onValueChange = { image = it },
                label = { Text("Input") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Green,
                    unfocusedBorderColor = Yellow
                ),
                placeholder = { Text(text = "Image", color = Color.Cyan) },
            )
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Cyan,
                    containerColor = Color.Black
                ),
                onClick = {
                    image = ""
                    currentUser?.let {
                        PersonList(
                            it, name, "", image, Timestamp.now(),false,""
                        )
                    }?.let { saveSubscribed(it) }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Profile")
            }
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Cyan,
                    containerColor = Color.Black
                ),
                onClick = {
                    sharedViewModel.updateOnlineStatus("Offline")
                    Firebase.auth.signOut()
                    logout()
                    sharedViewModel.auth.signOut()
                    sharedViewModel.gpsState.value = true
                    navController.navigate(Screens.LoginScreen.Route)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(text = "Sign Out", color = Color.Cyan, modifier = Modifier.padding(7.dp))
            }
        }
    }

    private fun saveSubscribed(person: PersonList) {
        val collectionRef = FirebaseFirestore.getInstance().collection("user")
        val documentRef = collectionRef.document(auth.currentUser!!.uid)
        documentRef.set(person)
            .addOnSuccessListener {}
            .addOnFailureListener { exception -> exception.printStackTrace() }
    }
}