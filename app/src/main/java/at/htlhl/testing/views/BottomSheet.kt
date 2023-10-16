package at.htlhl.testing.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.testing.R
import at.htlhl.testing.data.BottomSheetItem
import at.htlhl.testing.data.ShownUsers
import at.htlhl.testing.navigation.Screens
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BottomSheetContent(
    bottomSheetItems: List<BottomSheetItem>,
    onItemClicked: (BottomSheetItem) -> Unit,
    friend: ShownUsers
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
                Image(
                    contentDescription = null,
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = friend.personList.image)
                            .apply(block = fun ImageRequest.Builder.() {
                                placeholder(R.drawable.user_circle_svgrepo_com)
                            }).build()
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
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
                        Icon(
                            bottomSheetItems[it].icon,
                            bottomSheetItems[it].title,
                            tint = Color.Black,
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetTopBar(
    navController: NavController,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope
) {
    TopAppBar(
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
        modifier =
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded || bottomSheetScaffoldState.bottomSheetState.isAnimationRunning) {
            Modifier
                .clickable { coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() } }
                .height(70.dp)
                .fillMaxWidth()
        } else {
            Modifier
                .height(70.dp)
                .fillMaxWidth()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = "ChatNet",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                fontFamily = FontFamily.Cursive
            )
            Icon(imageVector = Icons.Outlined.PersonAddAlt1,
                contentDescription = "AddFriend",
                modifier =
                if (bottomSheetScaffoldState.bottomSheetState.isAnimationRunning || bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp, top = 5.dp)
                        .size(30.dp)
                } else {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 20.dp, top = 5.dp)
                        .size(30.dp)
                        .clickable {
                            navController.navigate(Screens.SearchViewScreen.route)
                        }
                }
            )
            IconButton(
                onClick = {
                    navController.navigate(Screens.InboxScreen.route)
                },
                enabled = !(bottomSheetScaffoldState.bottomSheetState.isAnimationRunning || bottomSheetScaffoldState.bottomSheetState.isExpanded),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 60.dp, top = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
    Divider(
        thickness = 1.dp,
        color = if (isSystemInDarkTheme()) Color.DarkGray else Color.Transparent
    )
}