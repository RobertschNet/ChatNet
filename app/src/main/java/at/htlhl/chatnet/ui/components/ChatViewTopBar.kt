package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.R
import at.htlhl.chatnet.data.InternalChatInstances
import coil.compose.SubcomposeAsyncImage

@Composable
fun MessageTopBar(chatInstance: InternalChatInstances, onClick: () -> Unit) {
    TopAppBar(
        backgroundColor = Color.White,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            IconButton(onClick = { onClick.invoke() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    tint = Color.Black,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable {
                        //TODO: Open Profile
                    }
                    .weight(1f)) {
                SubcomposeAsyncImage(
                    contentDescription = null,
                    model = chatInstance.personList.image,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(45.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator()
                    },
                )
                Text(
                    text = chatInstance.personList.username["mixedcase"].toString(),
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
            IconButton(onClick = {
                //TODO: Block User
            }) {
                SubcomposeAsyncImage(
                    model = R.drawable.person_block_svgrepo_com,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                )
            }
            IconButton(onClick = {
                //TODO: Search Messages
            }) {
                SubcomposeAsyncImage(
                    model = R.drawable.search_svgrepo_com_1_,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                )
            }
            IconButton(onClick = {
                //TODO: Info
            }) {
                SubcomposeAsyncImage(
                    model = R.drawable.info_svgrepo_com,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp),
                )
            }
        }
    }
}