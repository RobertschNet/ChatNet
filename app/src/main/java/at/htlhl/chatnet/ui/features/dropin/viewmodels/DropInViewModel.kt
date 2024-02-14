package at.htlhl.chatnet.ui.features.dropin.viewmodels

import androidx.lifecycle.ViewModel
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.data.LocationUserInstance

class DropInViewModel : ViewModel() {
    fun filterDropInPersonsList(
        searchedValue: String, completeDopInChatList: List<InternalChatInstance>
    ): List<InternalChatInstance> {
        return if (searchedValue != "") completeDopInChatList.filter {
            it.personList.username["mixedcase"]?.contains(
                searchedValue, ignoreCase = true
            ) ?: false || it.lastMessage.text.contains(
                searchedValue, ignoreCase = true
            )
        } else completeDopInChatList
    }

    fun filterDropInNearbyUsersList(
        searchedValue: String, dropInUsersNearbyList: List<LocationUserInstance>
    ): List<LocationUserInstance> {
        return if (searchedValue != "") dropInUsersNearbyList.filter {
            it.username["mixedcase"]?.contains(
                searchedValue, ignoreCase = true
            ) ?: false || it.location.contains(
                searchedValue, ignoreCase = true
            )
        } else dropInUsersNearbyList
    }

    fun filterDropInNearbyUser(
        searchedValue: String, dropInUserWithLocationInformation: LocationUserInstance?
    ): Boolean {
        return java.lang.String("You").contains(
            searchedValue, ignoreCase = true
        ) || dropInUserWithLocationInformation!!.location.contains(
            searchedValue, ignoreCase = true
        )
    }
}