package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.util.getPersonTagsList
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileInfoUserHeader(
    navController: NavController,
    friend: FirebaseUser,
    onImageClick: () -> Unit
) {
    val friendUserTags = getPersonTagsList(personData = friend)
    Card(modifier = Modifier.fillMaxWidth(), elevation = 10.dp, backgroundColor = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SubcomposeAsyncImage(model = R.drawable.back_svgrepo_com_1_,
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .clickable {
                        navController.navigateUp()
                    }
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .size(30.dp))
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SubcomposeAsyncImage(
                    model = friend.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(150.dp)
                        .clickable {
                            onImageClick()
                        }
                        .clip(CircleShape)
                )
                Text(
                    text = friend.username["mixedcase"].toString(),
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(5.dp))
                ProfileUserInfoHeaderTags(friendUserTags)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}