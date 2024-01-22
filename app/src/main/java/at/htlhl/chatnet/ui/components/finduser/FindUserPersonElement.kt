package at.htlhl.chatnet.ui.components.finduser

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FindUserPersonElement(
    person: FirebaseUser,
    deleteAble: Boolean,
    sharedViewModel: SharedViewModel,
    searchedUser: String,
    onClick: (FirebaseUser, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100))
            .background(Color.White)
            .clickable { }
            .padding(top = 10.dp, bottom = 10.dp, start = 15.dp, end = 10.dp)
    ) {
        val isOnline = person.status
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
            if (searchedUser != "searchedUser") {
                Box(
                    modifier = Modifier
                        .size(16.5f.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (!isSystemInDarkTheme()) listOf(
                                    Color.White,
                                    Color.White
                                ) else listOf(Color(0xF1161616), Color(0xF1161616)),
                                start = Offset(0f, 0f),
                                end = Offset(14.dp.value, 14.dp.value)
                            )
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    when (isOnline) {
                        "online" -> {
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
                        }

                        "offline" -> {
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

                        "idle" -> {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFFFC107), Color(0xFFFFC107)),
                                            start = Offset(0f, 0f),
                                            end = Offset(14.dp.value, 14.dp.value)
                                        )
                                    )
                                    .align(Alignment.Center)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.2f.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .align(Alignment.TopStart)
                                )
                            }
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
                    text = person.username["mixedcase"].toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(start = 5.dp)
                )
                Text(
                    text = "Last Message",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier
                        .padding(start = 5.dp)
                )
            }
        }
        Row {
            Button(
                onClick = { onClick.invoke(person, searchedUser == "pending") },
                enabled = if (searchedUser == "searchedUser") true else searchedUser == "pending",
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF00A0E8),
                )
            ) {
                Text(
                    text = if (searchedUser == "searchedUser") "Follow" else if (searchedUser == "pending") "Add" else "Followed",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (deleteAble) {
                IconButton(onClick = {
                    sharedViewModel.deleteFriendFromFriendList()
                }) {
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