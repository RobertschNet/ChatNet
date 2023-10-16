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
    val tag: String,
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
data class FetchedUsers(
    val image: String,
    val username: Map<String,String>,
    val status: String,
    val id: String,
    val email: String,
    val color: String,
    val connection: String,
    val mutedFriend: Boolean,
    val statusFriend: String,
) {
    constructor() : this("", mapOf(), "","","", "", "", false, "")

    fun doesMatch(query: String): Boolean {
        val matchingCombinations = listOf(
            username["lowercase"],
            username["mixedcase"],
            "${username["lowercase"]} ${username["mixedcase"]}",
        )
        return matchingCombinations.any { it?.contains(query, ignoreCase = true) ?: false}
    }
}

data class ShownUsers(
    val personList: FetchedUsers,
    val timestampMessage: Timestamp,
    val lastMessage: Message,
    val pinChat: Boolean,
    val read: Int,
    val markedAsUnread: Boolean,
    ) {
    constructor() : this(FetchedUsers(), Timestamp.now(), Message(), false, 0, false)
}

/**
 * This data class is used to represent the content of a single specific user element from Firebase.
 */
data class Friend(
    val id: String,
    val muted: Boolean,
    val status: String,
) {
    constructor() : this("", false, "")
}

/**
 * This data class is used to represent the content of a message element from Firebase.
 */
data class Message(
    val sender: String,
    val type: String,
    val read: Boolean,
    val content: String,
    val timestamp: Timestamp,
    val visible: List<String>,
) {
    constructor() : this("", "", false, "", Timestamp.now(), arrayListOf())
}

/**
 * This data class is used to represent the content of a whole chat element from Firebase,
 * including a list of all messages sent between the two users.
 */
data class Chat(
    val members: List<String>,
    val pinned: List<String>,
    val unread: List<String>,
    val tab: String,
    val chatRoomID: String,
    val messages: List<Message>,
) {
    constructor() : this(arrayListOf(), arrayListOf(), arrayListOf(), "", "", listOf())
}