package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.FirebaseChat
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import com.google.firebase.firestore.FirebaseFirestore

fun deleteChatRoom(
    friendData: InternalChatInstance = InternalChatInstance(),
    chatData: List<FirebaseChat> = emptyList(),
    publicUser: FirebaseUser = FirebaseUser()

) {
    var chatRoomId = friendData.chatRoomID
    if (chatData.isNotEmpty()) {
        chatRoomId = chatData.find {
            it.members.contains(publicUser.id)
        }?.chatRoomID ?: ""
    }
    val subCollectionRef = FirebaseFirestore.getInstance().collection("chats").document(chatRoomId)
        .collection("messages")

    subCollectionRef.get().addOnCompleteListener { messagesTask ->
        if (messagesTask.isSuccessful) {
            for (document in messagesTask.result!!) {
                subCollectionRef.document(document.id).delete().addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
            }

            FirebaseFirestore.getInstance().collection("chats").document(chatRoomId).delete()
                .addOnCompleteListener {}
        } else {
            messagesTask.exception?.printStackTrace()
        }
    }


}