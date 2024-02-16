package at.htlhl.chatnet.ui.features.images.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun formatImageTimestampComponent(timestampMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM, HH:mm")
    val instant = Instant.ofEpochMilli(timestampMillis)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

    val now = LocalDateTime.now()
    val differenceInMinutes = ChronoUnit.MINUTES.between(localDateTime, now)
    val differenceInHours = ChronoUnit.HOURS.between(localDateTime, now)

    return when {
        differenceInMinutes < 1 -> "Just now"
        differenceInMinutes < 60 -> "$differenceInMinutes minutes ago"
        differenceInHours < 24 && localDateTime.toLocalDate() == now.toLocalDate() -> {
            "Today, ${formatter.format(localDateTime)}"
        }

        differenceInHours < 48 -> {
            "Yesterday, ${formatter.format(localDateTime)}"
        }

        else -> formatter.format(localDateTime)
    }
}