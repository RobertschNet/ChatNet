package at.htlhl.chatnet.ui.features.finduser.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType

@Composable
fun FindUserFrontLayerContent(
    pendingFriendsList: List<FirebaseUser>,
    filteredSuggestedFriendList: List<FirebaseUser>,
    searchedText: String,
    onPersonClicked: (FirebaseUser) -> Unit,
    onFriendActionClicked: (FirebaseUser, Boolean) -> Unit,
    onDenyFriendRequestClicked: (FirebaseUser) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        Canvas(
            modifier = Modifier
                .width(50.dp)
                .height(10.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            drawRoundRect(
                color = Color.LightGray,
                size = size.copy(height = 2.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(2.dp.toPx())
            )
        }
        Text(
            text = "Users who follow you:",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 15.dp, top = 8.dp)
        )
        LazyColumn(
            Modifier
                .fillMaxSize()
        ) {
            if (pendingFriendsList.isEmpty()) {
                item {
                    Text(
                        text = "There are currently no users who follow you.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 15.dp, top = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(pendingFriendsList) { person ->
                FindUserPersonComponent(
                    isFrontLayer = true,
                    person = person,
                    deleteAble = true,
                    personType = PersonType.PENDING_PERSON,
                    searchedText = searchedText,
                    onPersonClicked = { clickedPerson ->
                        onPersonClicked(clickedPerson)
                    },
                    onFriendActionClicked = { clickedPerson, _ ->
                        onFriendActionClicked(clickedPerson, true)
                    },
                    onDenyFriendRequestClicked = { clickedPerson ->
                        onDenyFriendRequestClicked(clickedPerson)
                    }
                )
            }
            if (filteredSuggestedFriendList.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggestions for you:",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 15.dp, top = 20.dp)
                    )
                }
            }
            items(filteredSuggestedFriendList) { person ->
                FindUserPersonComponent(
                    isFrontLayer = true,
                    person = person,
                    deleteAble = false,
                    personType = PersonType.SEARCHED_PERSON,
                    searchedText = searchedText,
                    onPersonClicked = {clickedPerson ->
                        onPersonClicked(clickedPerson)
                    },
                    onFriendActionClicked = { clickedPerson, _ ->
                        onFriendActionClicked(clickedPerson, false)
                    },
                    onDenyFriendRequestClicked = { clickedPerson ->
                        onDenyFriendRequestClicked(clickedPerson)
                    }
                )
            }
        }
    }
}