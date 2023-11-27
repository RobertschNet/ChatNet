package at.htlhl.chatnet.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel

class RandChatStartView {
    @Composable
    fun RandChatStartScreen(navController: NavController, sharedViewModel: SharedViewModel) {
        if (sharedViewModel.isConnected.value&&navController.previousBackStackEntry?.destination?.route!=Screens.RandChatScreen.route&& navController.currentDestination?.route==Screens.RandChatStartScreen.route ) {
            LaunchedEffect(Unit) {
                navController.navigate(Screens.RandChatScreen.route)
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(50.dp),
                onClick = {
                    sharedViewModel.getRandChat(sharedViewModel, false, navController) {}
                    navController.navigate(Screens.RandChatScreen.route)
                }
            ) {
                Text(text = if (!sharedViewModel.isConnected.value) "Start RandChat" else "Continue to Chat with User")
            }
        }
    }
}