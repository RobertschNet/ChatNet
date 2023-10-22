package at.htlhl.testing.data

import com.google.firebase.Timestamp

data class InternalChatInstances(
    val personList: FirebaseUsers, // instance of the person the chat is with (FirebaseUsers)
    val timestampMessage: Timestamp, // timestamp of the last message sent in the chat (FirebaseMessages timestamp)
    val lastMessage: FirebaseMessages,  // last message sent in the chat (FirebaseMessages)
    val pinChat: Boolean, // if the chat has been pinned by the user (FirebaseChats pinned)
    val read: Int, // number of unread messages in the chat (FirebaseMessages read)
    val markedAsUnread: Boolean, // if the chat has been marked as unread by the user (FirebaseChats unread)
) {
    constructor() : this(
        FirebaseUsers(),
        Timestamp.now(),
        FirebaseMessages(),
        false,
        0,
        false
    ) // default constructor for Firebase
}