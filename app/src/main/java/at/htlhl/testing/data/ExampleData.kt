package at.htlhl.testing.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


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
    val lastMessage: String,
    val time: Long,
) {
    constructor() : this("", "", "", "", 0)
}
data class Message(
    val userID: String,
    val content: String,
    val timestamp: Long,
) {
    constructor() : this("", "", 0)
}

data class User(
    val username: String,
) {
    constructor() : this("")
}


