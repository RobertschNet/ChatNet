package at.htlhl.chatnet.data

data class FirebaseFriends(
    val id: String, // id of the friend
    val muted: Boolean, // if the friend has been muted by the user
    val status: String, // the current status of the friend request (accepted, pending, initiated, declined)
) {
    constructor() : this("", false, "") // default constructor for Firebase
}
