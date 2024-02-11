package at.htlhl.chatnet.util

fun checkIfValueIsValid(type: String, value: String): Boolean {
    return when (type) {
        "email" -> {
            value.matches("^(?=.{1,320})(?!.*[+._-]{2})(?![+._-])[a-zA-Z0-9+._-]{1,64}(?<![+._-])@(?![+._-])[a-zA-Z0-9.-]*\\.[a-zA-Z]{2,63}(?<![+._-])$".toRegex())
        }

        "username" -> {
            value.matches("^(?!.*[._-]{2})(?![._-])[a-zA-Z0-9._-]{1,30}(?<![._-])$".toRegex())
        }

        "password" -> {
            value.matches("^(?!.*\\s).{6,4096}$".toRegex())
        }

        else -> {
            false
        }
    }
}