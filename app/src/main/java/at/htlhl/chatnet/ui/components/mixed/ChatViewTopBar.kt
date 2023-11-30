package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

@Composable
fun MessageTopBar(
    chatInstance: InternalChatInstance,
    sharedViewModel: SharedViewModel,
    onClick: (String) -> Unit
) {
    var offsetState by remember { mutableStateOf(Offset(0f, 0f)) }
    val offset by animateOffsetAsState(targetValue = offsetState, label = "")
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
        ) {
            IconButton(onClick = { onClick.invoke("return") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.primary,
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
                if (!chatInstance.personList.blocked.contains(sharedViewModel.auth.currentUser?.uid.toString())) {
                    SubcomposeAsyncImage(
                        contentDescription = null,
                        model = chatInstance.personList.image,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(45.dp)
                            .shimmerEffect(),
                        contentScale = ContentScale.Crop,
                    )
                }else{
                    SubcomposeAsyncImage(
                        contentDescription = null,
                        model = R.drawable.default_user,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(45.dp),
                        contentScale = ContentScale.Crop,
                    )
                }

                if (chatInstance.personList.id == "ChatMate") {
                    Column(
                        modifier = Modifier.offset(y = -offset.y.dp)
                    ) {
                        Text(
                            text = chatInstance.personList.username["mixedcase"].toString(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                        if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                            Text(
                                text = "thinking...",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8f.dp)
                            )
                        }
                    }
                } else {
                    Column {
                        Text(
                            text = chatInstance.personList.username["mixedcase"].toString(),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                        Text(
                            text = chatInstance.personList.status,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 7.dp)
                        )
                        Spacer(modifier = Modifier.size(5.dp))
                    }

                }
            }
            LaunchedEffect(sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                offsetState =
                    if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                        Offset(0f, 5f)
                    } else {
                        Offset(0f, 0f)
                    }
            }
            IconButton(onClick = {
               onClick.invoke("block")
            }) {
                SubcomposeAsyncImage(
                    model = R.drawable.person_block_svgrepo_com,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .size(30.dp),
                )
            }
        }
    }
}