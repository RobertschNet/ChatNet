package at.htlhl.chatnet.ui.components.mixed

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.ui.theme.shimmerEffect
import coil.compose.SubcomposeAsyncImage
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChatViewMessageComponent(
    isUser: Boolean,
    context: Context,
    message: InternalMessageInstance,
    chatMateChat: Boolean,
    onLongPress: () -> Unit,
    previousMessage: InternalMessageInstance?,
    nextMessage: InternalMessageInstance?,
    onClick: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val formattedTime =
        message.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
            .format(formatter)
    val backgroundColor = if (isUser) {
        if (isSystemInDarkTheme()) Color(0xFF00A0E8) else Color(0xFF00A0E8)
    } else {
        if (isSystemInDarkTheme()) Color.DarkGray else Color.White
    }
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    Column {
        if (isDateSeparatorNeeded(message, previousMessage)) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .background(
                        if (isSystemInDarkTheme()) Color.DarkGray else Color(0xFFF5F5F5),
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
                    color = MaterialTheme.colorScheme.secondary
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
                if (message.image.isEmpty()) {
                    Modifier
                        .padding(
                            start = if (isUser) 80.dp else 10.dp,
                            end = if (isUser) 10.dp else 80.dp,
                            top = if (isTopPaddingNeeded(message, previousMessage)) 25.dp else 5.dp,
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
                            RoundedCornerShape(20.dp)
                        )
                        .background(backgroundColor, shape = RoundedCornerShape(20.dp))
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)
                } else {
                    Modifier
                        .padding(
                            start = if (isUser) 90.dp else 10.dp,
                            end = if (isUser) 10.dp else 90.dp,
                            top = if (isTopPaddingNeeded(message, previousMessage)) 25.dp else 5.dp,
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
                if (message.image.isEmpty()) {
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
                if (message.image.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = message.image,
                        alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .heightIn(min = 50.dp, max = 250.dp)
                            .widthIn(min = 75.dp, max = 250.dp)
                            .clickable { onClick.invoke(message.image) }
                            .shimmerEffect()
                    )
                }
            }
        }
    }
    if (isDateNeeded(message, nextMessage)) {
        Row(
            horizontalArrangement = alignment,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUser) {
                if (!chatMateChat) {
                    SubcomposeAsyncImage(
                        model = if (message.read) R.drawable.eye_1_svgrepo_com else R.drawable.eye_hide_1_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 5.dp),
                    )
                }
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    modifier =
                    Modifier.padding(end = 15.dp)
                )
            } else {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    modifier =
                    Modifier.padding(start = 15.dp)
                )
            }
        }
    }
}

private fun isDateSeparatorNeeded(
    currentMessage: InternalMessageInstance,
    previousMessage: InternalMessageInstance?
): Boolean {
    if (previousMessage == null) {
        return true
    }

    val currentCalendar = Calendar.getInstance().apply {
        timeInMillis = currentMessage.timestamp.toDate().time
    }

    val previousCalendar = Calendar.getInstance().apply {
        timeInMillis = previousMessage.timestamp.toDate().time
    }

    return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) ||
            currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDateForSeparator(timestamp: Timestamp): String {
    val currentInstant = Instant.now()
    val currentZone = ZoneId.systemDefault()
    val messageInstant = timestamp.toDate().toInstant()

    return when {
        messageInstant.atZone(currentZone).toLocalDate() == currentInstant.atZone(currentZone)
            .toLocalDate() -> "Today"

        messageInstant.atZone(currentZone).toLocalDate() == currentInstant.atZone(currentZone)
            .toLocalDate().minusDays(1) -> "Yesterday"

        else -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)
            formatter.format(messageInstant.atZone(currentZone))
        }
    }
}

private fun isTopPaddingNeeded(
    currentMessage: InternalMessageInstance,
    previousMessage: InternalMessageInstance?,
): Boolean {
    if (previousMessage == null) {
        return true
    }

    val currentCalendar = Calendar.getInstance().apply {
        timeInMillis = currentMessage.timestamp.toDate().time
    }

    val previousCalender = Calendar.getInstance().apply {
        timeInMillis = previousMessage.timestamp.toDate().time
    }

    return currentCalendar.get(Calendar.MINUTE) != previousCalender.get(Calendar.MINUTE)
}

private fun isDateNeeded(
    currentMessage: InternalMessageInstance,
    nextMessage: InternalMessageInstance?,
): Boolean {
    if (nextMessage == null) {
        return true
    }

    val currentCalendar = Calendar.getInstance().apply {
        timeInMillis = currentMessage.timestamp.toDate().time
    }

    val nextCalendar = Calendar.getInstance().apply {
        timeInMillis = nextMessage.timestamp.toDate().time
    }
    return currentCalendar.get(Calendar.MINUTE) != nextCalendar.get(Calendar.MINUTE)
}