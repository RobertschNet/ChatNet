package at.htlhl.testing.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

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
    val icon: Int,
    val color: Color
)

/**
 * This data class is used to represent the content of a user instance from Firebase.
 */
data class PersonList(
    val image: String,
    val username: String,
    val status: String,
    val id: String,
    val connection: String,
    val timestamp: Timestamp,
    val local: Boolean,
    val statusIntern: String,
) {
    constructor() : this("", "", "", "", "", Timestamp.now(), false, "")

    fun doesMatch(query: String): Boolean {
        val matchingCombinations = listOf(
            username,
            "${username.first()}",
        )
        return matchingCombinations.any { it.contains(query, ignoreCase = true) }
    }
}

/**
 * This data class is used to represent the content of a single specific user element from Firebase.
 */
data class Friend(
    val id: String,
    val status: String,
) {
    constructor() : this("", "")
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
    val members: List<String>,
    val tab: String,
    val chatRoomID: String,
    val messages: List<Message>,
) {
    constructor() : this(arrayListOf(),"", "", listOf())
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