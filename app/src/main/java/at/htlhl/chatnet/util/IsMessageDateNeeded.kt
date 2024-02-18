package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.InternalMessageInstance
import java.util.Calendar
fun isMessageDateNeeded(
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