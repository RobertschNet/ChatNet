package at.htlhl.chatnet.ui.features.dialogs

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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun DeleteAllMessagesDialog(
    isChatMateChat: Boolean,
    onClose: (String) -> Unit = {}
) {
    var selected by remember { mutableStateOf(true) }
    Dialog(
        onDismissRequest = { onClose.invoke("closed") },
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
                text = "Delete All Messages?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "Do you really want to delete all messages from this chat? This action cannot be undone.",
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 10.dp,
                    start = 10.dp,
                    end = 10.dp
                )
            )
            if (!isChatMateChat) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = true }) {
                    Spacer(modifier = Modifier.width(20.dp))
                    RadioButton(selected = selected, onClick = { selected = true })
                    Text(
                        text = "Delete for me",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                    )

                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = false }) {
                    Spacer(modifier = Modifier.width(20.dp))
                    RadioButton(selected = !selected, onClick = { selected = false })
                    Text(
                        text = "Delete for everyone",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                    )

                }
            }
            Divider(
                thickness = 0.3f.dp,
                color = MaterialTheme.colorScheme.outline,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClose.invoke(
                            if (isChatMateChat || !selected) "everyone" else "me"
                        )
                    }
            ) {
                Text(
                    text = "Delete Messages",
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose.invoke("closed") }
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)
                )
            }
        }
    }
}