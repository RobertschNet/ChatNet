package at.htlhl.chatnet.ui.components.mixed

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
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
    sharedViewModel: SharedViewModel,
    isUser: Boolean,
    message: InternalMessageInstance,
    chatMateChat: Boolean,
    onLongPress: () -> Unit,
    previousMessage: InternalMessageInstance?,
    nextMessage: InternalMessageInstance?,
    onClick: (String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val searchValue = sharedViewModel.searchValue.value
    var imageHeight by remember {
        mutableStateOf(true)
    }
    val formattedTime =
        message.timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
            .format(formatter)
    val backgroundColor = if (isUser) {
        if (isSystemInDarkTheme()) Color(0xFF00A0E8) else Color(0xFF00A0E8)
    } else {
        if (isSystemInDarkTheme()) Color.DarkGray else Color(0xFFFFFDFD)
    }
    val alignment = if (isUser) Arrangement.End else Arrangement.Start
    if (isDateNeeded(message, nextMessage)) {
        Row(
            horizontalArrangement = alignment,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUser) {
                if (!chatMateChat) {
                    SubcomposeAsyncImage(
                        model =
                        if (message.isFromCache) R.drawable.clock_svgrepo_com else if (message.read) R.drawable.eye_1_svgrepo_com else R.drawable.eye_hide_1_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 5.dp),
                    )
                }
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    modifier =
                    Modifier.padding(end = 15.dp)
                )
            } else {
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Start,
                    modifier =
                    Modifier.padding(start = 15.dp, top = 2.dp)
                )
            }
        }
    }
    Column {
        if (isDateSeparatorNeeded(message, previousMessage)) {
            Card(
                elevation = 10.dp,
                modifier = Modifier
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

        if (message.images.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = alignment,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .padding(
                            start = if (isUser) 90.dp else 10.dp,
                            end = if (isUser) 10.dp else 90.dp,
                            top = if (isTopPaddingNeeded(
                                    message,
                                    previousMessage
                                )
                            ) 20.dp else 5.dp,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onLongPress.invoke()
                                }
                            )
                        }
                )
                {
                    if (message.images.size >= 4) {
                        Card(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = backgroundColor,
                            modifier = Modifier
                                .width(270.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 2.dp)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = message.images[0],
                                        alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .padding(end = 2.dp, start = 4.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        onLongPress.invoke()
                                                    },
                                                    onTap = {
                                                        onClick.invoke(message.images[0])
                                                    }
                                                )
                                            }
                                            .clip(RoundedCornerShape(18.dp))
                                            .height(130.dp)
                                            .width(130.dp)
                                            .shimmerEffect()
                                    )
                                    SubcomposeAsyncImage(
                                        model = message.images[1],
                                        alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        onLongPress.invoke()
                                                    },
                                                    onTap = {
                                                        onClick.invoke(message.images[1])
                                                    }
                                                )
                                            }
                                            .clip(RoundedCornerShape(18.dp))
                                            .height(130.dp)
                                            .width(130.dp)
                                            .shimmerEffect()
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = if (message.text.isEmpty()) 4.dp else 2.dp)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = message.images[2],
                                        alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .padding(end = 2.dp, start = 4.dp)
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        onLongPress.invoke()
                                                    },
                                                    onTap = {
                                                        onClick.invoke(message.images[2])
                                                    }
                                                )
                                            }
                                            .clip(RoundedCornerShape(18.dp))
                                            .height(130.dp)
                                            .width(130.dp)
                                            .shimmerEffect()
                                    )
                                    if (message.images.size > 4) {
                                        Box(
                                            modifier = Modifier
                                                .height(130.dp)
                                                .width(130.dp)
                                                .padding(end = 4.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onLongPress = {
                                                            onLongPress.invoke()
                                                        },
                                                        onTap = {
                                                            onClick.invoke(message.images[3])
                                                        }
                                                    )
                                                }
                                                .clip(RoundedCornerShape(18.dp))

                                        ) {
                                            SubcomposeAsyncImage(
                                                model = message.images[3],
                                                alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .height(130.dp)
                                                    .width(130.dp)
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .alpha(0.6f)
                                            )
                                            Text(
                                                text = "+${message.images.size - 4}",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier.align(Alignment.Center),
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        SubcomposeAsyncImage(
                                            model = message.images[3],
                                            alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onLongPress = {
                                                            onLongPress.invoke()
                                                        },
                                                        onTap = {
                                                            onClick.invoke(message.images[3])
                                                        }
                                                    )
                                                }
                                                .clip(RoundedCornerShape(18.dp))
                                                .height(130.dp)
                                                .width(130.dp)
                                                .shimmerEffect()
                                        )
                                    }
                                }
                                if (message.text.isNotEmpty()) {
                                    Text(
                                        text = message.text,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (isUser) Color.White else Color.Black,
                                        modifier = Modifier.padding(
                                            bottom = 9.dp,
                                            start = 14.dp,
                                            end = 14.dp
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = backgroundColor,
                            modifier = Modifier
                                .width(if (imageHeight) 200.dp else 270.dp)
                        ) {
                            Column(modifier = Modifier.padding(5.dp)) {
                                message.images.forEachIndexed { index, image ->
                                    SubcomposeAsyncImage(
                                        model = image,
                                        onSuccess = { imageAsset ->
                                            val aspectRatio =
                                                imageAsset.result.drawable.intrinsicWidth.toFloat() / imageAsset.result.drawable.intrinsicHeight.toFloat()
                                            imageHeight = aspectRatio <= 1
                                        },
                                        alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        onLongPress.invoke()
                                                    },
                                                    onTap = {
                                                        onClick.invoke(image)
                                                    }
                                                )
                                            }
                                            .clip(RoundedCornerShape(18.dp))
                                            .height(if (imageHeight) 300.dp else 200.dp)
                                            .width(if (imageHeight) 200.dp else 270.dp)
                                            .shimmerEffect()
                                    )
                                    if (index != message.images.size - 1) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                                if (message.text.isNotEmpty()) {
                                    Text(
                                        text = message.text,
                                        color = if (isUser) Color.White else Color.Black,
                                        fontFamily = FontFamily.SansSerif,
                                        modifier = Modifier.padding(
                                            top = 4.dp,
                                            bottom = 4.dp,
                                            start = 10.dp,
                                            end = 10.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (message.text.isNotEmpty() && message.images.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = alignment,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .width(15.dp)
                        .background(Color.Transparent)
                )
                Card(
                    backgroundColor = backgroundColor,
                    contentColor = backgroundColor,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .widthIn(min = 100.dp)
                        .padding(
                            start = if (isUser) 80.dp else 0.dp,
                            end = if (isUser) 0.dp else 80.dp,
                            top = if (isTopPaddingNeeded(message, previousMessage)) 20.dp else 5.dp,
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    onLongPress.invoke()
                                }
                            )
                        }
                        .drawWithContent {
                            drawContent()
                            drawPath(
                                path = Path().apply {
                                    if (isUser) {
                                        moveTo(size.width, size.height)
                                        lineTo(size.width, size.height - 50f)
                                        lineTo(size.width - 50f, size.height)
                                        close()
                                    } else {
                                        moveTo(0f, size.height)
                                        lineTo(0f, size.height - 50f)
                                        lineTo(50f, size.height)
                                        close()
                                    }
                                },
                                color = backgroundColor,
                            )
                            drawPath(
                                path = Path().apply {
                                    if (isUser) {
                                        moveTo(size.width - 30f, size.height)
                                        lineTo(size.width + 15f, size.height + 15f)
                                        lineTo(size.width, size.height - 30f)
                                        close()
                                    } else {
                                        moveTo(30f, size.height)
                                        lineTo(-15f, size.height + 15f)
                                        lineTo(0f, size.height - 30f)
                                        close()
                                    }

                                },
                                color = backgroundColor,


                                )
                        }
                        .background(backgroundColor, shape = RoundedCornerShape(18.dp)),
                ) {
                    val messageContent = message.text
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
                    Text(
                        text = buildAnnotatedStringWithColorHighlight(
                            lines.toString(),
                            searchValue
                        ),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(12.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(18.dp)),
                        textAlign = TextAlign.Start,
                        color = if (isUser) Color.White else Color.Black
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(15.dp)
                        .background(Color.Transparent)
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

    return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) || currentCalendar.get(Calendar.DAY_OF_YEAR) != previousCalendar.get(Calendar.DAY_OF_YEAR)

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
    if (nextMessage.sender != currentMessage.sender) {
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


fun findAllOccurrences(main: String, sub: String): List<Int> {
    val indices = mutableListOf<Int>()
    var lastIndex = main.indexOf(sub, 0)
    while (lastIndex != -1) {
        indices.add(lastIndex)
        lastIndex = main.indexOf(sub, lastIndex + sub.length)
    }
    return indices
}

fun buildAnnotatedStringWithColorHighlight(content: String, text: String): AnnotatedString {
    val lowercase = text.lowercase(Locale.getDefault())
    val occurrences = if (text.isNotEmpty()) {
        findAllOccurrences(content.lowercase(Locale.getDefault()), lowercase)
    } else {
        emptyList()
    }

    return buildAnnotatedString {
        var lastIndex = 0
        occurrences.forEach { ottoIndex ->
            append(content.substring(lastIndex, ottoIndex))
            if (text.isNotEmpty()) {
                withStyle(style = SpanStyle(background = Color.Yellow, color = Color.Black)) {
                    val ottoLength = text.length
                    append(content.substring(ottoIndex, ottoIndex + ottoLength))
                }
            }
            lastIndex = ottoIndex + text.length
        }
        if (lastIndex < content.length) {
            append(content.substring(lastIndex))
        }
    }
}