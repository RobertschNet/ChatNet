package at.htlhl.chatnet.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import at.htlhl.chatnet.ui.features.mixed.findAllOccurrences
import java.util.Locale

fun highlightSearchedText(content: String, searchedText: String): AnnotatedString {
    val lowercase = searchedText.lowercase(Locale.getDefault())
    val occurrences = if (searchedText.isNotEmpty()) {
        findAllOccurrences(content.lowercase(Locale.getDefault()), lowercase)
    } else {
        emptyList()
    }

    return buildAnnotatedString {
        var lastIndex = 0
        occurrences.forEach { ottoIndex ->
            append(content.substring(lastIndex, ottoIndex))
            if (searchedText.isNotEmpty()) {
                withStyle(style = SpanStyle(background = Color.Yellow, color = Color.Black)) {
                    val ottoLength = searchedText.length
                    append(content.substring(ottoIndex, ottoIndex + ottoLength))
                }
            }
            lastIndex = ottoIndex + searchedText.length
        }
        if (lastIndex < content.length) {
            append(content.substring(lastIndex))
        }
    }
}