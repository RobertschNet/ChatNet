package at.htlhl.chatnet.ui.features.dropin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.LocationUserInstance
import at.htlhl.chatnet.util.highlightSearchedText
import coil.compose.SubcomposeAsyncImage

@Composable
fun DropInUserComponent(
    userData: FirebaseUser,
    userDataWithLocationInformation: LocationUserInstance?,
    searchedValue: String,
    onUserImageClicked: () -> Unit
) {
    Spacer(modifier = Modifier.width(10.dp))
    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier.width(80.dp)
        ) {
            SubcomposeAsyncImage(
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { onUserImageClicked.invoke() },
                model = userData.image,
                contentDescription = null
            )
            Box(
                modifier = Modifier
                    .size(16.5f.dp)
                    .offset((-5).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            ), start = Offset(0f, 0f), end = Offset(14.dp.value, 14.dp.value)
                        )
                    )
                    .align(Alignment.BottomEnd)
            ) {
                if (userData.online) {
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
        Text(
            text = highlightSearchedText(
                "You", searchedValue
            ),
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp),
            fontSize = 12.sp,
            maxLines = 1,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = highlightSearchedText(
                userDataWithLocationInformation?.location ?: "Unknown", searchedValue
            ),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light
        )
    }
}