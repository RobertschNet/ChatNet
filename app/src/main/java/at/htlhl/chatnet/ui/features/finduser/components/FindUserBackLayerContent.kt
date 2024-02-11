package at.htlhl.chatnet.ui.features.finduser.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.ui.features.mixed.LoadingUserElement


@Composable
fun FindUserBackLayerContent(
    friendList: List<FirebaseUser>,
    searchedPersons: List<FirebaseUser>,
    isSearching: Boolean,
    searchUserText: String,
    searchedText: String,
    onPersonClicked: (FirebaseUser) -> Unit,
    onFriendActionClicked: (FirebaseUser, Boolean) -> Unit,
    onDenyFriendRequestClicked: (FirebaseUser) -> Unit,
) {
    Divider(thickness = 0.25f.dp, color = MaterialTheme.colorScheme.outline)
    if (isSearching) {
        LazyColumn(content = {
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            items(10) {
                LoadingUserElement(true)
            }
        })
    }
    if (searchUserText.isNotEmpty()) {
        Text(
            text = "Results for \"$searchUserText\"",
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 20.dp, top = 10.dp)
                .fillMaxWidth()

        )
    }
    searchedPersons.forEach { person ->
        if (person.doesMatchUsername(searchUserText) && searchUserText.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            )
            {
                items(searchedPersons) { person ->
                    val specificUser = friendList.find { it.id == person.id }
                    FindUserPersonComponent(
                        isFrontLayer = false,
                        person = person,
                        deleteAble = false,
                        searchedText = searchedText,
                        personType = if (specificUser == null) PersonType.SEARCHED_PERSON else if (specificUser.statusFriend == "pending") PersonType.PENDING_PERSON else PersonType.ACCEPTED_PERSON,
                        onPersonClicked = {
                            onPersonClicked(it)
                        },
                        onFriendActionClicked = { clickedPerson, add ->
                            onFriendActionClicked(clickedPerson, add)
                        },
                        onDenyFriendRequestClicked = { clickedPerson ->
                            onDenyFriendRequestClicked(clickedPerson)
                        }
                    )
                }
            }
        }
    }
}