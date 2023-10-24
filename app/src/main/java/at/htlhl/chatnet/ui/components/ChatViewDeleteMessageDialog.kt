package at.htlhl.chatnet.ui.components

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
fun DeleteMessageDialog(
    isUser: Boolean,
    onClose: (String) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { onClose.invoke("closed") },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(20.dp))
                .width(250.dp)
                .height(if (isUser) 240.dp else 200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Delete Message?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = if (!isUser) "This message can be deleted only for you, and not for everyone." else "This message can be deleted only for you, or for everyone in the chat.",
                fontWeight = FontWeight.Light,
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
                    .clickable { onClose.invoke("change") }
            ) {
                Text(
                    text = "Delete for me",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
            if (isUser) {
                Divider(
                    thickness = 0.3f.dp,
                    color = Color.LightGray,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClose.invoke("delete")
                        }
                ) {
                    Text(
                        text = "Delete for everyone",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                    )
                }
            }
            Divider(
                thickness = 0.3f.dp,
                color = Color.LightGray,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose.invoke("closed") }
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
        }
    }
}