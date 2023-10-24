package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.R
import coil.compose.SubcomposeAsyncImage

@Composable
fun EmptyChatContent(onClicked: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(120.dp))
        Text(text = "No Friends to Chat!", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))
        SubcomposeAsyncImage(
            model = R.drawable.message_messaging_send_svgrepo_com,
            contentDescription = null,
            modifier = Modifier.size(200.dp),
            loading = {
                CircularProgressIndicator()
            })
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Your Friendlist appears to be empty, in order to start a chat send other people Friend requests.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = { onClicked.invoke() },
            modifier = Modifier
                .width(200.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00A0E8),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = "Get Started now!", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}