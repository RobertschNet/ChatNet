package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.ui.theme.shimmerEffect
import coil.compose.SubcomposeAsyncImage

@Composable
fun OnlineIndicatorComponent(person: FirebaseUser) {
    Box(
        modifier = Modifier.size(50.dp)
    ) {
        SubcomposeAsyncImage(
            contentDescription = null,
            model = person.image,
            modifier = Modifier
                .clip(CircleShape)
                .shimmerEffect()
                .size(50.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        )


        Box(
            modifier = Modifier
                .size(16.5f.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(14.dp.value, 14.dp.value)
                    )
                )
                .align(Alignment.BottomEnd)
        ) {
            if (person.online) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF08C008), Color(0xFF08C008)),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.Center)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.Gray, Color(0xFF808080)),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}