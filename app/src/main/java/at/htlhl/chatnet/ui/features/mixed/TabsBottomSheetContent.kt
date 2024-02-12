package at.htlhl.chatnet.ui.features.mixed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.data.BottomSheetItem
import at.htlhl.chatnet.data.BottomSheetTagState
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.ui.theme.shimmerEffect
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun TabsBottomSheetContent(
    friendData: InternalChatInstance,
    bottomSheetItems: List<BottomSheetItem>,
    onItemClicked: (BottomSheetTagState) -> Unit
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
                    model = friendData.personList.image,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp)
                        .shimmerEffect(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                )
                Text(
                    text = friendData.personList.username["mixedcase"].toString(),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.padding(6.dp))
            Divider(
                thickness = 0.25f.dp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            LazyColumn(userScrollEnabled = false) {
                items(bottomSheetItems.size, itemContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemClicked(bottomSheetItems[it].tag)
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
                            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = bottomSheetItems[it].title,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Start,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(
                                start = 10.dp,
                            ),
                        )
                    }

                })
            }
            Spacer(modifier = Modifier.height(10.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    )
}


