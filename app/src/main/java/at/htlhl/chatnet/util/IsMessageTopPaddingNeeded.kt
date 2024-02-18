package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.InternalMessageInstance
import java.util.Calendar
fun isMessageTopPaddingNeeded(
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