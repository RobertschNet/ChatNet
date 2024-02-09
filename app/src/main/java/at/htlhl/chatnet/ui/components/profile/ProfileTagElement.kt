package at.htlhl.chatnet.ui.components.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.TagElement
import at.htlhl.chatnet.ui.components.mixed.TagElement

@Composable
fun ProfileTagElement(tags: List<TagElement>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        when {
            tags.size >= 2 -> {
                TagElement(
                    element = tags[0].name,
                    color = tags[0].color,
                    icon = tags[0].icon,
                    smallSize = false
                )
                TagElement(
                    element = tags[1].name,
                    color = tags[1].color,
                    icon = tags[1].icon,
                    smallSize = false
                )
            }

            tags.size == 1 -> {
                TagElement(
                    element = tags[0].name,
                    color = tags[0].color,
                    icon = tags[0].icon,
                    smallSize = false
                )
            }
        }
    }
    if (tags.size > 2) {
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            when {
                tags.size >= 4 -> {
                    TagElement(
                        element = tags[2].name,
                        color = tags[2].color,
                        icon = tags[2].icon,
                        smallSize = false
                    )
                    TagElement(
                        element = tags[3].name,
                        color = tags[3].color,
                        icon = tags[3].icon,
                        smallSize = false
                    )
                }

                tags.size == 3 -> {
                    TagElement(
                        element = tags[2].name,
                        color = tags[2].color,
                        icon = tags[2].icon,
                        smallSize = false
                    )
                }
            }
        }
    }
    if (tags.size > 4) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "+${tags.size - 4} more",
                color = Color.DarkGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.SansSerif
            )
        }
    }

}