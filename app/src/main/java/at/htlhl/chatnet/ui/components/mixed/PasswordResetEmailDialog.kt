package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Preview
@Composable
fun PasswordResetEmailDialog(onDismiss: () -> Unit= {}) {
    Dialog(
        onDismissRequest = { onDismiss.invoke() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                .width(250.dp)
                .height(180.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "We sent you an email with a link to reset your password. Please check your inbox and spam folder.",
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 20.dp,
                    start = 10.dp,
                    end = 10.dp
                )
            )
            Divider(
                thickness = 0.3f.dp,
                color = Color.LightGray,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss.invoke() }
            ) {
                Text(
                    text = "Continue",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF00A0E8),
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
        }
    }
}