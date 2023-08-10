package at.htlhl.testing.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp


enum class LoadingState {
    Loading,
    Authenticated,
    NotAuthenticated,
    Error
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
    val color: Color
)

data class PersonList(
    val userID: String,
    val name: String,
    val image: String,
) {
    constructor() : this("", "", "")
}

data class Friend(
    val userID: String,
    val status: String,
    val lastMessage: String,
    val lastMessageTimestamp: Timestamp,
) {
    constructor() : this("", "", "", Timestamp.now())
}

data class Message(
    val sender: String,
    val content: String,
    val timestamp: Timestamp,
) {
    constructor() : this("", "", Timestamp.now())
}

data class User(
    val username: String,
) {
    constructor() : this("")
}

data class Person(
    val userID: String,
    val name: String,
    val image: String,
    val lastMessage: String,
    val timestamp: Timestamp,
) {
    fun doesMatch(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            "${name.first()}",
        )
        return matchingCombinations.any { it.contains(query, ignoreCase = true) }
    }
}

