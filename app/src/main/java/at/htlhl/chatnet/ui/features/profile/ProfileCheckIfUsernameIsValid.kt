package at.htlhl.chatnet.ui.features.profile


fun profileCheckIfUsernameIsValid(value: String): Boolean {
    return value.matches("^(?!.*[._-]{2})(?![._-])[a-zA-Z0-9._-]{1,30}(?<![._-])$".toRegex())
}
