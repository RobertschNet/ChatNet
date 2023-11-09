package at.htlhl.chatnet.data

data class FirebaseChat(
    val members: List<String>, // list of the members of the chat (FirebaseUsers ids)
    val unread: List<String>, // list of the users that have set unread messages in the chat (FirebaseUsers ids)
    val tab: String, // the tab the chat is in (chats, dropIn, randchat, chatmate)
    val chatRoomID: String, // the id of the chat
    var messages: List<FirebaseMessage>, // list of all messages sent in the chat (FirebaseMessages)
) {
    constructor() : this(
        arrayListOf(),
        arrayListOf(),
        "",
        "",
        listOf()
    ) // default constructor for Firebase
}
