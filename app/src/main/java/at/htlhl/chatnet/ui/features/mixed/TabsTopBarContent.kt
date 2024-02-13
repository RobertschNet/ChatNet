package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.chatnet.R
import at.htlhl.chatnet.data.CurrentTab
import at.htlhl.chatnet.data.FirebaseUser
import coil.compose.SubcomposeAsyncImage
import java.util.Locale


@Composable
fun TabsTopBarContent(
    tab: CurrentTab,
    dropInState: Boolean,
    onActivateSearchMode: () -> Unit,
    friendRequests: List<FirebaseUser>,
    onActionClicked: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = tab.toString().lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            fontWeight = FontWeight.Medium,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(start = 10.dp)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        if (tab != CurrentTab.PROFILE && tab != CurrentTab.RANDCHAT) {
            IconButton(
                onClick = { onActivateSearchMode() },
                modifier = Modifier.padding(start = 108.dp, top = 5.dp)
            ) {
                SubcomposeAsyncImage(
                    model = R.drawable.search_svgrepo_com_1_,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
            IconButton(
                onClick = { onActionClicked() },
                modifier = Modifier.padding(top = 5.dp, end = 10.dp)
            ) {
                Box(modifier = Modifier.size(50.dp)) {
                    if (tab == CurrentTab.DROPIN) {
                        Icon(
                            imageVector = if (dropInState) Icons.Outlined.LocationOff else Icons.Outlined.LocationOn,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(35.dp)
                                .align(Alignment.Center),
                            contentDescription = null
                        )
                    } else {
                        SubcomposeAsyncImage(
                            model = if (tab == CurrentTab.CHATS) R.drawable.add_user_social_svgrepo_com_1_ else if (tab == CurrentTab.CHATMATE) R.drawable.chat_add_svgrepo_com_1_ else Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier
                                .size(35.dp)
                                .align(Alignment.Center),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                    if (friendRequests.isNotEmpty() && tab == CurrentTab.CHATS) {
                        Box(
                            modifier = Modifier
                                .padding(end = 7f.dp)
                                .size(12f.dp)
                                .zIndex(1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.Red, Color.Red),
                                        start = Offset(0f, 0f),
                                        end = Offset(0.dp.value, 0.dp.value)
                                    )
                                )
                                .align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = friendRequests.size.toString(),
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
        if (tab == CurrentTab.RANDCHAT) {
            IconButton(
                onClick = { onActivateSearchMode() },
                modifier = Modifier.padding(top = 5.dp, end = 10.dp)
            ) {
                SubcomposeAsyncImage(
                    model = R.drawable.search_svgrepo_com_1_,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}