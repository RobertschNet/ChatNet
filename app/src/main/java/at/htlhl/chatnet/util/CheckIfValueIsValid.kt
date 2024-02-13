package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.TextFieldTypeState

fun checkIfValueIsValid(type: TextFieldTypeState, value: String): Boolean {
    return when (type) {
        TextFieldTypeState.EMAIL -> {
            value.matches("^(?=.{1,320})(?!.*[+._-]{2})(?![+._-])[a-zA-Z0-9+._-]{1,64}(?<![+._-])@(?![+._-])[a-zA-Z0-9.-]*\\.[a-zA-Z]{2,63}(?<![+._-])$".toRegex())
        }

        TextFieldTypeState.USERNAME -> {
            value.matches("^(?!.*[._-]{2})(?![._-])[a-zA-Z0-9._-]{1,30}(?<![._-])$".toRegex())
        }

        TextFieldTypeState.PASSWORD -> {
            value.matches("^(?!.*\\s).{6,4096}$".toRegex())
        }

        else -> {
            false
        }
    }
}