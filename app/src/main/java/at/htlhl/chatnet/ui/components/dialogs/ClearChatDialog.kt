package at.htlhl.chatnet.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ClearChatDialog(onDismiss: (String) -> Unit) {
    Dialog(
        onDismissRequest = { onDismiss.invoke("closed") },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                .width(250.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Clear Chat?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "All messages/images from both users will be deleted.",
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color= MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    top = 10.dp, bottom = 20.dp, start = 10.dp, end = 10.dp
                )
            )
            Divider(
                thickness = 0.3f.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss.invoke("clear") }) {
                Text(
                    text = "Clear",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
            Divider(
                thickness = 0.3f.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss.invoke("closed") }) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.SemiBold,
                    color= MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
        }
    }
}