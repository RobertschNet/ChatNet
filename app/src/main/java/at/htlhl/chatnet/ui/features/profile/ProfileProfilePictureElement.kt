package at.htlhl.chatnet.ui.features.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.ui.theme.shimmerEffect
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay

@Composable
fun ProfileProfilePictureElement(
    userData: FirebaseUser,
    updateProfilePictureLoading: Boolean,
    updateProfilePictureException: Boolean,
    onImageClicked: () -> Unit,
    onProfileChangeClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp)
            .size(180.dp)
    ) {
        SubcomposeAsyncImage(
            model = userData.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable {
                    onImageClicked.invoke()
                }
                .shimmerEffect()
        )
        Box(
            modifier = Modifier
                .size(50f.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00A0E8),
                            Color(0xFF00A0E8)
                        )
                    )
                )
                .align(Alignment.BottomEnd)
        ) {
            var rotationState by remember { mutableFloatStateOf(0f) }

            val rotation by animateFloatAsState(
                targetValue = rotationState,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                ), label = ""
            )
            LaunchedEffect(updateProfilePictureLoading) {
                while (true) {
                    if (updateProfilePictureLoading) {
                        rotationState += -360f
                        delay(1000)
                    } else {
                        break
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.Cached,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clickable(enabled = !updateProfilePictureLoading) {
                        onProfileChangeClicked.invoke()
                    }
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .graphicsLayer(
                        rotationZ = rotation,
                    )
            )
        }
    }
    if (updateProfilePictureException) {
        Text(
            text = "An error occurred while updating your profile picture.",
            color = Color.Red,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
        )
    }
}