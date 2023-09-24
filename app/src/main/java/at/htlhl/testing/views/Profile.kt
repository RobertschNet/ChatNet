package at.htlhl.testing.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.testing.R
import at.htlhl.testing.data.PersonList
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class Profile {

    @Composable
    fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        val userState = sharedViewModel.user.collectAsState()
        val userData: PersonList = userState.value
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
                googleSignInClient.signOut()
                    .addOnCompleteListener {}
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(200.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(userData.image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(60f.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00A0E8), Color(0xFF00A0E8))
                            )
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(imageVector = Icons.Outlined.Cached,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(45.dp)
                            .clickable {
                                sharedViewModel.imageCall.value = true
                            }
                            .align(Alignment.Center)
                            .clip(CircleShape)
                    )
                }

            }
            Button(
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Cyan,
                    containerColor = Color.Black
                ),
                onClick = {
                    sharedViewModel.updateOnlineStatus("Offline")
                    logout()
                    sharedViewModel.reset()
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
}