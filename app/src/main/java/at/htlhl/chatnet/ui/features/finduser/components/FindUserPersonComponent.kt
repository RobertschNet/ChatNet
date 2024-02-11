package at.htlhl.chatnet.ui.features.finduser.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.PersonType
import at.htlhl.chatnet.data.tags
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.util.highlightSearchedText
import coil.compose.SubcomposeAsyncImage


@Composable
fun FindUserPersonComponent(
    isFrontLayer: Boolean,
    person: FirebaseUser,
    deleteAble: Boolean,
    personType: PersonType,
    searchedText: String,
    onPersonClicked: (FirebaseUser) -> Unit,
    onFriendActionClicked: (FirebaseUser, Boolean) -> Unit,
    onDenyFriendRequestClicked: (FirebaseUser) -> Unit
) {
    val filteredPersonTags =
        if (person.tags.isEmpty()) tags.filter { tag -> tag.category == "Empty" } else tags.filter { tag ->
            person.tags.contains(tag.name)
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(
                if (isFrontLayer) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
            .clickable {
                onPersonClicked.invoke(person)
            }
            .padding(top = 10.dp, bottom = 10.dp, start = 15.dp, end = 10.dp)
    ) {
        Box(
            modifier = Modifier.size(50.dp)
        ) {
            SubcomposeAsyncImage(
                contentDescription = null,
                model = person.image,
                modifier = Modifier
                    .clip(CircleShape)
                    .shimmerEffect()
                    .size(50.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
            )
            if (personType != PersonType.SEARCHED_PERSON) {
                Box(
                    modifier = Modifier
                        .size(16.5f.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    if (person.online) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF08C008), Color(0xFF08C008)),
                                        start = Offset(0f, 0f),
                                        end = Offset(14.dp.value, 14.dp.value)
                                    )
                                )
                                .align(Alignment.Center)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.Gray, Color(0xFF808080)),
                                        start = Offset(0f, 0f),
                                        end = Offset(14.dp.value, 14.dp.value)
                                    )
                                )
                                .align(Alignment.Center)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = highlightSearchedText(
                        person.username["mixedcase"].toString(),
                        searchedText
                    ),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 5.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                FindUserSmallTagComponent(filteredPersonTags = filteredPersonTags)
            }
        }
        Row {
            Button(
                onClick = {
                    onFriendActionClicked(person, personType == PersonType.PENDING_PERSON)
                },
                enabled = if (personType == PersonType.SEARCHED_PERSON) true else personType == PersonType.PENDING_PERSON,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledBackgroundColor = if (isSystemInDarkTheme()) Color.DarkGray else Color.LightGray,
                    backgroundColor = Color(0xFF00A0E8),
                )
            ) {
                Text(
                    text = if (personType == PersonType.SEARCHED_PERSON) "Follow" else if (personType == PersonType.PENDING_PERSON) "Add" else "Followed",
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (deleteAble) {
                IconButton(onClick = { onDenyFriendRequestClicked(person) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}