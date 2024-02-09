package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagElement(element: String, color: Color, icon: ImageVector, smallSize: Boolean) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .border(1.dp, color.copy(alpha = 0.7f), CircleShape)
                .background(
                    color.copy(alpha = if (isSystemInDarkTheme()) 0.2f else 0.5f),
                    CircleShape
                )
        ) {
            Row(
                modifier = Modifier.padding(if (smallSize) 0.75f.dp else 1.5f.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(if (smallSize) 2.dp else 5.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    tint = if (isSystemInDarkTheme()) color else Color.White,
                    modifier = Modifier.size(if (smallSize) 15.dp else 20.dp)
                )
                Spacer(modifier = Modifier.size(if (smallSize) 2.dp else 5.dp))
                Text(
                    text = element,
                    color = if (isSystemInDarkTheme()) color else Color.White,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    fontSize = if (smallSize) 10.sp else 14.sp,
                    modifier = Modifier.offset(y = (-0.6f).dp),
                )
                Spacer(modifier = Modifier.size(if (smallSize) 2.dp else 5.dp))
            }
        }
    }

}