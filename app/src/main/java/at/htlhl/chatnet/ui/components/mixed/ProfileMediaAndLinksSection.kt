package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.InternalMessageInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

@Composable
fun ProfileMediaAndLinksSection(
    imageList: List<InternalMessageInstance>,
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp),
        shape = RoundedCornerShape(25.dp),
        elevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.White),
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Spacer(modifier = Modifier.height(7.5f.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        text = "Media and links",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Text(
                        modifier = Modifier
                            .clickable {
                                sharedViewModel.imagePosition.intValue = 0
                                navController.navigate(Screens.ImageViewScreen.route)
                            },
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )
                            ) {
                                append(imageList.size.toString())
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 14.sp
                                )
                            ) {
                                append(" >")
                            }
                        },
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Spacer(modifier = Modifier.height(2.5f.dp))
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        item {
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        items(imageList.size) {
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .clickable {
                                        sharedViewModel.imagePosition.intValue = it
                                        navController.navigate(Screens.ImageViewScreen.route)
                                    }
                                    .height(100.dp)
                                    .width(100.dp)
                                    .padding(5.dp)
                                    .border(
                                        width = 2.dp,
                                        color = Color.White,
                                    )
                                    .clip(RoundedCornerShape(16.dp)),
                                model = imageList[it].images[0],
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (imageList.isNotEmpty()) {
                            item {
                                IconButton(onClick = {
                                    sharedViewModel.imagePosition.intValue = 0
                                    navController.navigate(Screens.ImageViewScreen.route)
                                }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.arrow_right_svgrepo_com,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.Gray),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

