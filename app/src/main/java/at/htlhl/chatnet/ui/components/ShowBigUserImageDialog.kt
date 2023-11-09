package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import at.htlhl.chatnet.data.InternalChatInstance
import coil.compose.SubcomposeAsyncImage

@Composable
fun ShowBigUserImageDialog(
    userData: InternalChatInstance,
    onDismiss: (String) -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss("closed") },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .height(295.dp)
                .width(250.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .background(Color.Gray.copy(alpha = 0.4f))
            ) {
                Text(
                    text = userData.personList.username["mixedcase"].toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines= 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .zIndex(1f)
                        .padding(start = 5.dp, top = 5.dp, bottom = 5.dp),
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SubcomposeAsyncImage(model = userData.personList.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(shape = RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator()
                    })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { onDismiss.invoke("message") }) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Message",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { onDismiss.invoke("block") }) {
                        Icon(
                            imageVector = Icons.Outlined.Block,
                            contentDescription = "Block",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { onDismiss.invoke("image") }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { onDismiss.invoke("info") }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF00A0E8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}