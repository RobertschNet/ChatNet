package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TagElement(element: String, color: Color, icon:ImageVector, smallSize:Boolean) {
    Box(modifier = Modifier.background(Color.White)) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.5f), CircleShape)
        ) {
            Row(
                modifier = Modifier.padding(if (smallSize) 0.5f.dp else 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(if (smallSize) 2.dp else 5.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier.size(if (smallSize) 15.dp else 25.dp)
                )
                Spacer(modifier = Modifier.size(if (smallSize) 2.dp else 5.dp))
                Text(
                    text = element,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    fontSize = if (smallSize) 10.sp else 18.sp,
                    modifier = Modifier.offset(y = (-0.6f).dp),
                )

                Spacer(modifier = Modifier.size(if (smallSize) 2.dp else 5.dp))
            }
        }
    }

}