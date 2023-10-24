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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.R
import at.htlhl.chatnet.data.FirebaseMessages
import coil.compose.SubcomposeAsyncImage
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ChatViewMessageComponent(
    isUser: Boolean,
    context: Context,
    message: FirebaseMessages,
    onLongPress: () -> Unit
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
                alignment= if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
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
