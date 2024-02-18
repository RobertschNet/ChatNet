package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.InternalMessageInstance
import java.util.Calendar

fun isDateSeparatorNeeded(
    currentMessage: InternalMessageInstance, previousMessage: InternalMessageInstance?
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

    return currentCalendar.get(Calendar.YEAR) != previousCalendar.get(Calendar.YEAR) || currentCalendar.get(
        Calendar.DAY_OF_YEAR
    ) != previousCalendar.get(Calendar.DAY_OF_YEAR)

}