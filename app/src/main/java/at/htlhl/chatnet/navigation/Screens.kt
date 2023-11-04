package at.htlhl.chatnet.navigation

sealed class Screens(val route: String) {
    object LoadingScreen : Screens("LoadingScreen")
    object ChatsViewScreen : Screens("ChatsViewScreen")
    object CameraViewScreen : Screens("CameraViewScreen")
    object CameraPhotoScreen : Screens("CameraPhotoScreen")
    object DropInScreen : Screens("DropInScreen")
    object ChatViewScreen : Screens("ChatViewScreen")
    object ChatMateScreen : Screens("ChatMateScreen")
    object RandChatScreen : Screens("RandChatScreen")
    object ProfileScreen : Screens("ProfileScreen")
    object LoginScreen : Screens("LoginScreen")
    object RegisterScreen : Screens("RegisterScreen")
    object FindUserScreen : Screens("FindUserScreen")
}
