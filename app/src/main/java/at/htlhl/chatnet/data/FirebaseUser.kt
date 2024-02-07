package at.htlhl.chatnet.data


data class FirebaseUser(
    val blocked: List<String>, // list of ids of users that have been blocked by the user
    val image: String, // url of the users profile picture
    val username: Map<String, String>, // username of the user in different cases (lowercase, mixedcase)
    val online: Boolean, // the current status of the user (online, offline)
    val id: String, // id of the user
    val email: String, // email of the user
    val pinned: List<String>, // list of ids of chats that have been pinned by the user
    val color: String, // color picked by the user for graphical elements
    val connected: Boolean, // the current connection-status for the randchat feature (matched, pending, offline)
    val muted: List<String>, // if the friend has been muted by the user (from FirebaseFriends)
    val statusFriend: String, // the current status of the friend request (from FirebaseFriends)
    val tags: List<String> // list of tags the user has selected
) {
    constructor() : this(
        listOf(),
        "",
        mapOf(),
        false,
        "",
        "",
        listOf(),
        "",
        false,
        listOf(),
        "",
        listOf("No Tags")
    ) // default constructor for Firebase

    /**
     * This function is used to check if the username of the user matches the query.
     *
     * @param query the query to check against the username
     * @return true if the username matches the query, false otherwise
     */
    fun doesMatchUsername(query: String): Boolean {
        val matchingCombinations = listOf(
            username["lowercase"],
            username["mixedcase"],
            "${username["lowercase"]} ${username["mixedcase"]}",
        )
        return matchingCombinations.any { it?.contains(query, ignoreCase = true) ?: false }
    }
}
