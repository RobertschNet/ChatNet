package at.htlhl.testing.navigation

sealed class Screens(val Route: String) {
    object DropInScreen : Screens("DropInScreen")
    object ChatScreen : Screens("ChatScreen")
    object ChatMateScreen : Screens("ChatMateScreen")
    object RandChatScreen : Screens("RandChatScreen")
    object ProfileScreen : Screens("ProfileScreen")
    object LoginScreen : Screens("LoginScreen")
    object RegisterScreen : Screens("RegisterScreen")
    object SearchViewScreen : Screens("SearchViewScreen")
    object LoadingScreen : Screens("LoadingScreen")

}
