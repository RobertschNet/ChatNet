package at.htlhl.chatnet.util

import at.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.BottomSheetTagState
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance

fun generateBottomSheetItems(
    isChatMate: Boolean, friendData: InternalChatInstance, userData: FirebaseUser
): List<BottomSheetItem> {
    val markAsReadTitle =
        if (friendData.markedAsUnread || friendData.read > 0) "Mark as Read" else "Mark as Unread"
    val markAsReadIcon =
        if (friendData.markedAsUnread || friendData.read > 0) R.drawable.chat_bubble_svgrepo_com else R.drawable.chat_bubble_outline_badged_svgrepo_com
    val clearChatTitle = "Clear Chat"
    val clearChatIcon = R.drawable.comment_delete_svgrepo_com
    val muteUserTitle =
        if (userData.muted.contains(friendData.personList.id)) "Unmute User" else "Mute User"
    val muteUserIcon =
        if (userData.muted.contains(friendData.personList.id)) R.drawable.speaker_none_svgrepo_com else R.drawable.speaker_svgrepo_com
    val pinChatTitle = if (friendData.pinChat) "Unpin Chat" else "Pin Chat"
    val pinChatIcon =
        if (friendData.pinChat) R.drawable.pin_off_svgrepo_com else R.drawable.pin_svgrepo_com
    val deleteChatTitle = "Delete Chat"
    val deleteChatIcon = R.drawable.garbage_bin_recycle_bin_svgrepo_com

    return listOf(
        BottomSheetItem(
            title = markAsReadTitle, icon = markAsReadIcon, tag = BottomSheetTagState.UNREAD
        ),
        BottomSheetItem(
            title = clearChatTitle, icon = clearChatIcon, tag = BottomSheetTagState.CLEAR
        ),
        BottomSheetItem(title = pinChatTitle, icon = pinChatIcon, tag = BottomSheetTagState.PIN),
        if (isChatMate) BottomSheetItem(
            title = deleteChatTitle, icon = deleteChatIcon, tag = BottomSheetTagState.DELETE
        )
        else BottomSheetItem(
            title = muteUserTitle, icon = muteUserIcon, tag = BottomSheetTagState.MUTE
        ),
    )
}
