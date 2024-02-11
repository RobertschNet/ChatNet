package at.htlhl.chatnet.ui.features.finduser.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.TagElement
import at.htlhl.chatnet.ui.features.mixed.TagElement

@Composable
fun FindUserSmallTagComponent(filteredPersonTags: List<TagElement>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            filteredPersonTags.size >= 2 -> {
                TagElement(
                    element = filteredPersonTags[0].name,
                    color = filteredPersonTags[0].color,
                    icon = filteredPersonTags[0].icon,
                    smallSize = true
                )
                TagElement(
                    element = filteredPersonTags[1].name,
                    color = filteredPersonTags[1].color,
                    icon = filteredPersonTags[1].icon,
                    smallSize = true
                )
            }

            filteredPersonTags.size == 1 -> {
                TagElement(
                    element = filteredPersonTags[0].name,
                    color = filteredPersonTags[0].color,
                    icon = filteredPersonTags[0].icon,
                    smallSize = true
                )
            }
        }
        if (filteredPersonTags.size > 2) {
            Text(
                text = "+${filteredPersonTags.size - 2}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}