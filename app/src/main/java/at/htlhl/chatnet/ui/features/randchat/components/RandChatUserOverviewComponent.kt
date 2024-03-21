package at.htlhl.chatnet.ui.features.randchat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.TagElement
import at.htlhl.chatnet.ui.theme.shimmerEffect
import coil.compose.SubcomposeAsyncImage

@Composable
fun RandChatUserOverviewComponent(
    userData: FirebaseUser,
    filteredUserTags: List<TagElement>,
    onUserProfilePictureClicked: () -> Unit,
    onStartRandChatPressed: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        SubcomposeAsyncImage(model = userData.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable {
                    onUserProfilePictureClicked()
                }
                .shimmerEffect())
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = userData.username["mixedcase"].toString(),
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.SansSerif,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        RandChatUserTagElementsComponent(tags = filteredUserTags)
        Spacer(modifier = Modifier.height(10.dp))
        Button(shape = CircleShape, onClick = {
            onStartRandChatPressed()
        }) {
            Text(text = if (!userData.connected) "Start RandChat" else "Continue to Chat with User")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

}