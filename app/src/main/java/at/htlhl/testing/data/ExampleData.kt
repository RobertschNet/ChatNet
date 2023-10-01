package at.htlhl.testing.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

/**
 * Created by Tobias Brandl.
 *
 * This data class is used to represent the state of the loading process and other data classes,
 * needed for Firebase communication.
 */

/**
 * This enum class is used to represent the state of the loading process.
 */
enum class LoadingState {
    Loading,
    Authenticated,
    NotAuthenticated,
    Error
}

/**
 * This data class is used to represent the content of the bottom-context-menu.
 */
data class BottomSheetItem(
    val title: String,
    val icon: ImageVector
)

/**
 * This data class is used to represent the content of the bottom-navigation-bar.
 */
data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector,
    val color: Color
)

/**
 * This data class is used to represent the content of a user instance from Firebase.
 */
data class PersonList(
    val image: String,
    val name: String,
    val online: String,
    val userID: String,
    val randChat: String,
    val timestamp: Timestamp,
    val local: Boolean,
    val status: String,
) {
    constructor() : this("", "", "", "", "", Timestamp.now(), false, "")
}

/**
 * This data class is used to represent the content of a single specific user element from Firebase.
 */
data class Friend(
    val userID: String,
    val status: String,
    val local: Boolean,
) {
    constructor() : this("", "", false)
}

/**
 * This data class is used to represent the content of a message element from Firebase.
 */
data class Message(
    val sender: String,
    val content: String,
    val timestamp: Timestamp,
) {
    constructor() : this("", "", Timestamp.now())
}

/**
 * This data class is used to represent the content of a whole chat element from Firebase,
 * including a list of all messages sent between the two users.
 */
data class Chat(
    val participants: List<String>,
    val chatRoomID: String,
    val messages: List<Message>,
) {
    constructor() : this(arrayListOf(), "", listOf())
}

/**
 * This data class is used to represent the username of the User who registered,
 * based on the Firebase username element.
 */
data class User(
    val username: String,
) {
    constructor() : this("")
}

/**
 * This data class is used to store a list of specific user values from the Firebase user entry.
 */
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

