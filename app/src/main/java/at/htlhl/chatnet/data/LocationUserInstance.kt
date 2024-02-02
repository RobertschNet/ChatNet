package at.htlhl.chatnet.data

data class LocationUserInstance(
    val blocked: List<String>, // list of ids of users that have been blocked by the user
    val image: String, // url of the users profile picture
    val username: Map<String, String>, // username of the user in different cases (lowercase, mixedcase)
    val online: Boolean, // the current status of the user (online, offline)
    val id: String, // id of the user
    val muted: List<String>, // if the friend has been muted by the user
    val location:String // location of the user
) {
    constructor() : this(
        listOf(),
        "",
        mapOf(),
        false,
        "",
        listOf(),
        ""
    ) // default constructor for Firebase
}
