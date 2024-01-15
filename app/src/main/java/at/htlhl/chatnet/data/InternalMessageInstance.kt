package at.htlhl.chatnet.data

import com.google.firebase.Timestamp

data class InternalMessageInstance(
    val isFromCache: Boolean, // if the message is from the cache
    val id: String, // id of the message
    val sender: String, // id of the sender (FirebaseUsers id)
    val images: List<String>, // amount of images in the message
    val read: Boolean, // if the message has been read by the receiver
    val text: String, // text of the message
    val timestamp: Timestamp, // timestamp when the message was sent
    val visible: List<String>, // list of the users that can see the Message (FirebaseUsers ids)
) {
    constructor() : this(
        false,
        "",
        "",
        arrayListOf(),
        false,
        "",
        Timestamp.now(),
        arrayListOf()
    ) // default constructor for Firebase
}
