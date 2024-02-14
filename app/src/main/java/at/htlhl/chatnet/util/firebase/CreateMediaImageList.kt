package at.htlhl.chatnet.util.firebase

import at.htlhl.chatnet.data.InternalMessageInstance

fun createMediaImageList(
    userID: String,
    messages: List<InternalMessageInstance>,
): List<InternalMessageInstance> {
    val imageList = arrayListOf<InternalMessageInstance>()
    messages.forEach {
        if (it.images.isNotEmpty()) {
            it.images.forEach { image ->
                if (it.visible.contains(userID)) {
                    imageList.add(
                        InternalMessageInstance().copy(images = arrayListOf(image))
                    )
                }
            }
        }
    }
    return imageList
}