package at.htlhl.chatnet.data

enum class LoadingState {
    Loading, // loading the current authenticated state of the user from Firebase
    Authenticated, // user is logged in and authenticated with Firebase
    NotAuthenticated, // user is not logged in and not authenticated with Firebase
    Error // an error occurred while loading the current authenticated state of the user from Firebase
}