package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItems
import at.htlhl.chatnet.data.InternalChatInstances
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun ChatsViewBottomSheetContent(
    bottomSheetItems: List<BottomSheetItems>,
    onItemClicked: (BottomSheetItems) -> Unit,
    friend: InternalChatInstances
) {
    Column(
        content = {
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
            Spacer(modifier = Modifier.padding(6.dp))
            Row {
                SubcomposeAsyncImage(
                    contentDescription = null,
                    model = friend.personList.image,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    loading = {
                        CircularProgressIndicator()
                    }
                )
                Text(
                    text = friend.personList.username["mixedcase"].toString(),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.padding(6.dp))
            Divider(
                thickness = 0.25f.dp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            LazyColumn(userScrollEnabled = false) {
                items(bottomSheetItems.size, itemContent = {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemClicked.invoke(bottomSheetItems[it])
                            },
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(data = bottomSheetItems[it].icon)
                                    .apply(block = fun ImageRequest.Builder.() {
                                        placeholder(R.drawable.user_circle_svgrepo_com)
                                    }).build()
                            ),
                            bottomSheetItems[it].title,
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
                        )
                        Text(
                            text = bottomSheetItems[it].title,
                            color = Color.Black,
                            modifier = Modifier.padding(
                                start = 12.dp,
                                top = 14.dp,
                                bottom = 14.dp
                            ),
                        )
                    }

                })
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                color = Color.White,
            )
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    )
}


