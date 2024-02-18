package at.htlhl.chatnet.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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

fun findAllOccurrences(main: String, sub: String): List<Int> {
    val indices = mutableListOf<Int>()
    var lastIndex = main.indexOf(sub, 0)
    while (lastIndex != -1) {
        indices.add(lastIndex)
        lastIndex = main.indexOf(sub, lastIndex + sub.length)
    }
    return indices
}