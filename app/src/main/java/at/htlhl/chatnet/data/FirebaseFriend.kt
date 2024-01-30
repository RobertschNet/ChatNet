package at.htlhl.chatnet.data

data class FirebaseFriend(
    val id: String, // id of the friend
    val status: String, // the current status of the friend request (accepted, pending, initiated, declined)
) {
    constructor() : this("","") // default constructor for Firebase
}
