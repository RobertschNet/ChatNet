package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.CommentsDisabled
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.InternalChatInstances
import coil.compose.SubcomposeAsyncImage

@Composable
fun MessageTopBar(chatInstance: InternalChatInstances, onClick: () -> Unit) {
    var favorite by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf(false) }
    TopAppBar(
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
        title = {
            Text(
                text = chatInstance.personList.username["mixedcase"].toString(),
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                fontSize = 22.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .padding(start = 5.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        actions = {
            IconButton(onClick = { favorite = !favorite }) {
                Icon(
                    imageVector = if (favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
            }
            IconButton(onClick = { pin = !pin }) {
                Icon(
                    imageVector = if (pin) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
            }
            IconButton(onClick = { comment = !comment }) {
                Icon(
                    imageVector = if (comment) Icons.Outlined.CommentsDisabled else Icons.Outlined.Comment,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
            }
        },
        navigationIcon = {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(25.dp)
                        .clickable { onClick.invoke() }
                )
                SubcomposeAsyncImage(
                    contentDescription = null,
                    model = chatInstance.personList.image,
                    modifier = Modifier
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically)
                        .size(40.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator()
                    },
                )
            }
        },
    )
}