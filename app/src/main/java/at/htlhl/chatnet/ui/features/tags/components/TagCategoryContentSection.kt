package at.htlhl.chatnet.ui.features.tags.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.TagElement

@Composable
fun TagCategorySection(
    context: Context,
    tags: List<TagElement>,
    selectedTags: List<TagElement>,
    onTagSelectionChanged: (List<TagElement>) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.padding(10.dp))
        Text(
            text = tags.firstOrNull()?.category.toString().lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            modifier = Modifier.padding(10.dp)
        )
        tags.forEach { tag ->
            TagContentComponent(tag = tag, isSelected = selectedTags.contains(tag), onTagClick = {
                val updatedSelection = if (selectedTags.contains(tag)) {
                    selectedTags - tag
                } else {
                    if (selectedTags.size < 5) {
                        selectedTags + tag
                    } else {
                        Toast.makeText(
                            context, "You can only select up to 5 tags", Toast.LENGTH_SHORT
                        ).show()
                        selectedTags
                    }
                }
                onTagSelectionChanged(updatedSelection)
            })
        }
    }
}