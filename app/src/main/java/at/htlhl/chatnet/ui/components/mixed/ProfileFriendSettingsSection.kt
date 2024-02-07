package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileFriendSettingsSection(
    isChatMateChat: Boolean,
    user: FirebaseUser,
    friend: InternalChatInstance,
    onBlockAction: () -> Unit,
    onRemoveUserAction: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        elevation = 10.dp,
        shape = RoundedCornerShape(25.dp)
    ) {
        Column(modifier = Modifier.background(Color.White)) {
            Spacer(modifier = Modifier.height(7.5f.dp))
            Text(
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 15.dp),
                text = "Friend Settings",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color.Gray
            )
            if (!isChatMateChat) {
                Row(
                    modifier = Modifier
                        .height(50.dp)
                        .clickable {
                            onBlockAction.invoke()
                        }
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(25.dp))
                    SubcomposeAsyncImage(
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(if (user.blocked.contains(friend.personList.id)) Color.Black else Color.Red),
                        model = R.drawable.person_block_svgrepo_com,
                        modifier = Modifier.size(30.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = if (user.blocked.contains(friend.personList.id)) "Unblock ${friend.personList.username["mixedcase"]}" else "Block ${friend.personList.username["mixedcase"]}",
                        fontSize = 16.sp,
                        color = if (user.blocked.contains(friend.personList.id)) Color.Black else Color.Red,
                    )
                    Spacer(modifier = Modifier.width(25.dp))
                }
            }
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .clickable {
                        onRemoveUserAction.invoke()
                    }
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(25.dp))
                SubcomposeAsyncImage(
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.Red),
                    model = R.drawable.garbage_bin_recycle_bin_svgrepo_com,
                    modifier = Modifier.size(30.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    text = "Remove ${friend.personList.username["mixedcase"]}",
                    fontSize = 16.sp,
                    color = Color.Red,
                )
                Spacer(modifier = Modifier.width(25.dp))
            }
        }
    }
}