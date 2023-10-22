package at.htlhl.testing.navigation

sealed class Screens(val route: String) {
    object Chats : Screens("ChatsScreen")
    object DropInScreen : Screens("DropInScreen")
    object ChatViewScreen : Screens("ChatViewScreen")
    object ChatMateScreen : Screens("ChatMateScreen")
    object RandChatScreen : Screens("RandChatScreen")
    object ProfileScreen : Screens("ProfileScreen")
    object LoginScreen : Screens("LoginScreen")
    object RegisterScreen : Screens("RegisterScreen")
    object SearchViewScreen : Screens("SearchViewScreen")
    object InboxScreen : Screens("InboxScreen")
}
