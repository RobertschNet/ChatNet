package at.htlhl.chatnet.ui.components

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.R
import at.htlhl.chatnet.data.FirebaseMessages
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChatViewMessageComponent(
    isUser: Boolean,
    context: Context,
    message: FirebaseMessages,
    onLongPress: () -> Unit,
    previousMessage: FirebaseMessages?
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedTime =
        message.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
            .format(formatter)
    val backgroundColor = if (isUser) {
        if (isSystemInDarkTheme()) Color.DarkGray else Color(0xFF00A0E8)
    } else {
        if (isSystemInDarkTheme()) Color.Black else Color.White
    }
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    Column {
        if (isDateSeparatorNeeded(message, previousMessage)) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .background(
                        Color(0xFFF5F5F5),
                        RoundedCornerShape(30)
                    )
                    .align(CenterHorizontally)
            ) {
                Text(
                    text = formatDateForSeparator(message.timestamp),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = alignment,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier =
                if (message.type == "text") {
                    Modifier
                        .padding(
                            start = if (isUser) 80.dp else 10.dp,
                            end = if (isUser) 10.dp else 80.dp,
                            top = 25.dp,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onLongPress.invoke()
                                }
                            )
                        }
                        .border(
                            if (isUser) 0.dp else 0.5f.dp,
                            if (isUser) Color.White else Color.Black,
                            RoundedCornerShape(24.dp)
                        )
                        .background(backgroundColor, shape = RoundedCornerShape(24.dp))
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                } else {
                    Modifier
                        .padding(
                            start = if (isUser) 80.dp else 10.dp,
                            end = if (isUser) 10.dp else 80.dp,
                            top = 25.dp,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onLongPress.invoke()
                                }
                            )
                        }
                }
            ) {
                val messageContent = message.content
                val maxLineLength = 30
                val words = messageContent.split("\\s+".toRegex())
                val lines = StringBuilder()
                var currentLine = StringBuilder()

                for (word in words) {
                    if (word.length > maxLineLength) {
                        if (currentLine.isNotEmpty()) {
                            lines.append('\n')
                        }
                        for (i in word.indices step maxLineLength) {
                            val endIndex = (i + maxLineLength).coerceAtMost(word.length)
                            val subWord = word.substring(i, endIndex)
                            if (currentLine.isNotEmpty()) {
                                currentLine.append(' ')
                            }
                            currentLine.append(subWord)
                            if (currentLine.length >= maxLineLength) {
                                lines.append(currentLine)
                                currentLine = StringBuilder()
                            }
                        }
                    } else if (currentLine.isNotEmpty() && currentLine.length + word.length + 1 <= maxLineLength) {
                        currentLine.append(' ')
                        currentLine.append(word)
                    } else if (currentLine.isNotEmpty()) {
                        lines.append(currentLine)
                        lines.append('\n')
                        currentLine = StringBuilder(word)
                    } else {
                        currentLine = StringBuilder(word)
                    }
                }

                if (currentLine.isNotEmpty()) {
                    lines.append(currentLine)
                }

                if (message.type == "text") {
                    Text(
                        text = lines.toString(),
                        fontSize = 14.sp,
                        color = if (isUser) Color.White else Color.Black,
                        modifier = Modifier
                            .padding(8.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(24.dp)),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        if (message.type == "image") {
            SubcomposeAsyncImage(
                model = message.content,
                alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = if (isUser) 0.dp else 10.dp, end = if (isUser) 10.dp else 0.dp)
                    .aspectRatio(1024f / 720f),
                loading = {
                    CircularProgressIndicator()
                },
            )
        }
    }

    Row(
        horizontalArrangement = alignment,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isUser) {
            SubcomposeAsyncImage(
                model = if (message.read) R.drawable.eye_1_svgrepo_com else R.drawable.eye_hide_1_svgrepo_com,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 5.dp),
                loading = {
                    CircularProgressIndicator()
                },
            )
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Start,
                modifier =
                Modifier.padding(end = 15.dp)
            )
        } else {
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                textAlign = TextAlign.Start,
                modifier =
                Modifier.padding(start = 15.dp)
            )
        }
    }
}

// Function to check if a date separator is needed
private fun isDateSeparatorNeeded(
    currentMessage: FirebaseMessages,
    previousMessage: FirebaseMessages?
): Boolean {
    if (previousMessage == null) {
        return false
    }

    val currentTime = currentMessage.timestamp.toDate().time
    val previousTime = previousMessage.timestamp.toDate().time
    val timeDifference = currentTime - previousTime

    // Check if the time difference is more than 24 hours (in milliseconds)
    val oneDayInMillis = 24 * 60 * 60 * 1000
    return timeDifference > oneDayInMillis
}

// Function to format the date for the separator
@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateForSeparator(timestamp: Timestamp): String {
    val currentInstant = Instant.now()
    val currentZone = ZoneId.systemDefault()
    val messageInstant = timestamp.toDate().toInstant()

    return when {
        messageInstant.atZone(currentZone).toLocalDate() == currentInstant.atZone(currentZone).toLocalDate() -> "Today"
        messageInstant.atZone(currentZone).toLocalDate() == currentInstant.atZone(currentZone).toLocalDate().minusDays(1) -> "Yesterday"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
            formatter.format(messageInstant.atZone(currentZone))
        }
    }
}
