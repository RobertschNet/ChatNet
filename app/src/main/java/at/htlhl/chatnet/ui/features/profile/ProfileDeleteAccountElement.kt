package at.htlhl.chatnet.ui.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileDeleteAccountElement(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(start = 20.dp, top = 20.dp)
            .clickable {
                onClick.invoke()
            }
            .fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Top)
                .padding(top = 5.dp)
                .size(30.dp)
        )
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(start = 15.dp)
        ) {
            Text(
                text = "Delete Account",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .padding(bottom = 3.dp)
            )
            Text(
                text = "Delete your account and all your data. This action is irreversible!",
                color = MaterialTheme.colorScheme.secondary,
                overflow = TextOverflow.Clip,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Light,
                lineHeight = 16.sp,
                modifier = Modifier
                    .padding(bottom = 10.dp, end = 20.dp)
            )

            Divider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 0.3.dp,
                modifier = Modifier
                    .padding(end = 20.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}