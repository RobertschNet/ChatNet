package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.util.getPersonTagsList
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileFriendsFromFriendsSection(
    friendsFromFriendsList: List<FirebaseUser>,
    friendsFromFriendsListIsLoading: Boolean,
    onUserClicked: (FirebaseUser) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        elevation = 10.dp,
        shape = RoundedCornerShape(25.dp),
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Spacer(modifier = Modifier.height(7.5f.dp))
                Text(
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    text = "Friends in common",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 15.dp),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(2.5f.dp))
                Column(content = {
                    Spacer(modifier = Modifier.height(5.dp))
                    if (friendsFromFriendsListIsLoading && friendsFromFriendsList.isEmpty()) {
                        for (i in 0..3) {
                            LoadingUserElement(false)
                        }
                    }
                    friendsFromFriendsList.forEach {
                        val filteredTags = getPersonTagsList(personData = it)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onUserClicked.invoke(it) }) {
                            Spacer(modifier = Modifier.width(10.dp))
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                                    .padding(5.dp)
                                    .clip(CircleShape),
                                model = it.image,
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    textAlign = TextAlign.Start,
                                    overflow = TextOverflow.Ellipsis,
                                    text = it.username["mixedcase"].toString(),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    when {
                                        filteredTags.size >= 2 -> {
                                            TagElement(
                                                element = filteredTags[0].name,
                                                color = filteredTags[0].color,
                                                icon = filteredTags[0].icon,
                                                smallSize = true
                                            )
                                            TagElement(
                                                element = filteredTags[1].name,
                                                color = filteredTags[1].color,
                                                icon = filteredTags[1].icon,
                                                smallSize = true
                                            )
                                        }

                                        filteredTags.size == 1 -> {
                                            TagElement(
                                                element = filteredTags[0].name,
                                                color = filteredTags[0].color,
                                                icon = filteredTags[0].icon,
                                                smallSize = true
                                            )
                                        }
                                    }
                                    if (filteredTags.size > 2) {
                                        Text(
                                            text = "+${filteredTags.size - 2}",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                })
            }
        }
    }
}