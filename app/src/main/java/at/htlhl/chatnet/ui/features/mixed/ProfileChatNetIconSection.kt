package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileChatNetIconSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubcomposeAsyncImage(
            model = R.drawable.logo__1_,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "ChatNet",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}