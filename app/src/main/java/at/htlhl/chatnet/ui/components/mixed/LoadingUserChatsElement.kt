package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.htlhl.chatnet.ui.theme.shimmerEffect

@Composable
fun LoadingUserChatsElement() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(Color.White)
            .padding(top = 10.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.CenterVertically)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .fillMaxSize(), verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .shimmerEffect()
                    .size(100.dp, 18.dp)
            )
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .shimmerEffect()
                    .size(140.dp, 18.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(35.dp, 12.dp)
                .shimmerEffect()
                .align(Alignment.Top)
        )
        Spacer(modifier = Modifier.width(5.dp))
    }
}