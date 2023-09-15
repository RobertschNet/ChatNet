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
data class BottomSheetItem(
    val title: String,
    val icon: ImageVector
)

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
    val status: String,
    val timestamp: Timestamp,
    val local: Boolean,
) {
    constructor() : this("", "", "","", Timestamp.now(), false)
}

data class Friend(
    val userID: String,
    val status: String,
) {
    constructor() : this("", "")
}

data class Message(
    val sender: String,
    val content: String,
    val timestamp: Timestamp,
) {
    constructor() : this("", "", Timestamp.now())
}

data class Chat(
    val participants: List<String>,
    val chatRoomID: String,
    val messages: List<Message>,
) {
    constructor() : this(arrayListOf(), "", listOf())
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
) {
    fun doesMatch(query: String): Boolean {
        val matchingCombinations = listOf(
            name,
            "${name.first()}",
        )
        return matchingCombinations.any { it.contains(query, ignoreCase = true) }
    }
}

