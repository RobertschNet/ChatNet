package at.htlhl.chatnet.util

import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.TagCategoryState
import at.htlhl.chatnet.data.TagElement
import at.htlhl.chatnet.data.tags
import java.util.Locale

fun getPersonTagsList(personData: FirebaseUser): List<TagElement> {
    return if (personData.tags.isEmpty()) tags.filter { tag -> tag.category == TagCategoryState.EMPTY } else tags.filter { tag ->
        personData.tags.contains(tag.name.lowercase(Locale.ROOT))
    }
}