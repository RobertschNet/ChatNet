package at.htlhl.chatnet.ui.features.tags.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.htlhl.chatnet.data.TagElement

@Composable
fun TagContentComponent(tag: TagElement, isSelected: Boolean, onTagClick: () -> Unit) {
    Spacer(modifier = Modifier.height(2.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onTagClick)
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Row(
            modifier = Modifier
                .background(
                    color = if (isSelected) {
                        tag.color.copy(alpha = 0.3f)
                    } else {
                        Color.Transparent
                    }, shape = RoundedCornerShape(24.dp)
                )
                .padding(10.dp)
        ) {
            Icon(
                imageVector = tag.icon,
                contentDescription = null,
                tint = tag.color,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = tag.name,
                color = tag.color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}