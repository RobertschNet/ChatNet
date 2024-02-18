package at.htlhl.chatnet.util

import android.os.Build
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDateOfMessage(timestamp: Timestamp): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
    } else {
        return timestamp.toDate().toString().substring(0, 10)
    }
}