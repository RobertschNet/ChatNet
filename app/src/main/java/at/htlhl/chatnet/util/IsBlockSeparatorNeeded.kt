package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance

fun isBlockSeparatorNeeded(
    userData: FirebaseUser, friendData: InternalChatInstance
): Boolean {
    return userData.blocked.contains(friendData.personList.id)
}