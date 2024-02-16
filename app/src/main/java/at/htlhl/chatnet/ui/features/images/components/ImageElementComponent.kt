package at.htlhl.chatnet.ui.features.images.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage

@Composable
fun ImageElementComponent(image: String) {
    SubcomposeAsyncImage(
        model = image,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        contentDescription = null,
    )
}