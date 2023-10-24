package at.htlhl.chatnet.data

import com.google.firebase.Timestamp

data class FirebaseMessages(
    val sender: String, // id of the sender (FirebaseUsers id)
    val type: String, // type of the message (text, image)
    val read: Boolean, // if the message has been read by the receiver
    val content: String, // content of the message (text, url of the image)
    val timestamp: Timestamp, // timestamp when the message was sent
    val visible: List<String>, // list of the users that can see the Message (FirebaseUsers ids)
) {
    constructor() : this(
        "",
        "",
        false,
        "",
        Timestamp.now(),
        arrayListOf()
    ) // default constructor for Firebase
}
