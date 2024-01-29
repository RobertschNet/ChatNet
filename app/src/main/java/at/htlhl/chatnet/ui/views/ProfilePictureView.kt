package at.htlhl.chatnet.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ProfilePictureView {
    @Composable
    fun ProfilePictureViewScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(color = Color.Black, darkIcons = false)
        val userDataState = sharedViewModel.user.collectAsState(initial = FirebaseUser())
        val userData: FirebaseUser = userDataState.value
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            SubcomposeAsyncImage(
                model = userData.image,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f)
                    )
                    .align(Alignment.TopStart)
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(35.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_svgrepo_com_1_),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        tint = Color.White
                    )
                }
            }
        }
    }
}