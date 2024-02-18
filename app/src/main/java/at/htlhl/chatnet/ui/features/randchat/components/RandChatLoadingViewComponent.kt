package at.htlhl.chatnet.ui.features.randchat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import at.chatnet.R
import coil.compose.SubcomposeAsyncImage

@Composable
fun RandChatLoadingViewComponent(navController: NavController) {
    Scaffold(backgroundColor = MaterialTheme.colorScheme.background, topBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = { navController.navigateUp() }, modifier = Modifier.size(45.dp)
            ) {
                SubcomposeAsyncImage(
                    model = R.drawable.back_svgrepo_com_1_,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }, content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Searching for User...",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.SansSerif
            )
        }
    })
}