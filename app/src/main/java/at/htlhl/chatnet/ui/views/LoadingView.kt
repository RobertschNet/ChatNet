package at.htlhl.chatnet.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.chatnet.R
import coil.compose.SubcomposeAsyncImage

class LoadingView {
    @Composable
    fun LoadingScreen() {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            SubcomposeAsyncImage(
                model = R.drawable.logo__1_,
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )

        }
    }
}