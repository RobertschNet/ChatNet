package at.htlhl.chatnet.ui.views

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

class ImageView {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun ImageViewScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(color = Color.Black, darkIcons =false)
        val chatPartnerState = sharedViewModel.friend.collectAsState(initial = InternalChatInstance())
        val chatPartner: InternalChatInstance = chatPartnerState.value
        HorizontalPager(sharedViewModel, navController, chatPartner)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun HorizontalPager(
        sharedViewModel: SharedViewModel,
        navController: NavController,
        chatPartner: InternalChatInstance
    ) {
        Log.println(Log.INFO, "ImageView", sharedViewModel.imageList.value.toString())
        val pageCount = sharedViewModel.imageList.value.size

        val pagerState =
            rememberPagerState(initialPage = sharedViewModel.imagePosition.intValue) { pageCount }

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)) {
            HorizontalPager(
                beyondBoundsPageCount = 1,
                modifier = Modifier
                    .fillMaxSize(),
                state = pagerState,
            ) {
                ImageItem(sharedViewModel.imageList.value[it].images[0])
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f)
                    )
                    .align(Alignment.TopStart)
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(35.dp)
                        .padding(5.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_svgrepo_com_1_),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Column(
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp)
                        .fillMaxSize(),
                ) {
                    Text(
                        text = if (sharedViewModel.imageList.value[pagerState.currentPage].sender == sharedViewModel.auth.currentUser!!.uid) "You" else chatPartner.personList.username["mixedcase"].toString(),
                        fontSize = 18.sp
                    )
                    Text(
                        text = formatTimestamp(sharedViewModel.imageList.value[pagerState.currentPage].timestamp.toDate().time),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )


                }
            }
            HorizontalPagerIndicator(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                targetPage = pagerState.targetPage,
                currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun formatTimestamp(timestampMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM, HH:mm")
        val instant = Instant.ofEpochMilli(timestampMillis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val now = LocalDateTime.now()
        val differenceInMinutes = ChronoUnit.MINUTES.between(localDateTime, now)
        val differenceInHours = ChronoUnit.HOURS.between(localDateTime, now)

        return when {
            differenceInMinutes < 1 -> "Just now"
            differenceInMinutes < 60 -> "$differenceInMinutes minutes ago"
            differenceInHours < 24 && localDateTime.toLocalDate() == now.toLocalDate() -> {
                "Today, ${formatter.format(localDateTime)}"
            }

            differenceInHours < 48 -> {
                "Yesterday, ${formatter.format(localDateTime)}"
            }

            else -> formatter.format(localDateTime)
        }
    }

    @Composable
    fun ImageItem(image: String) {
        SubcomposeAsyncImage(
            model = image,
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }

    @Composable
    private fun HorizontalPagerIndicator(
        pageCount: Int,
        currentPage: Int,
        targetPage: Int,
        currentPageOffsetFraction: Float,
        modifier: Modifier = Modifier,
        indicatorColor: Color = Color.White,
        unselectedIndicatorSize: Dp = 8.dp,
        selectedIndicatorSize: Dp = 10.dp,
        indicatorCornerRadius: Dp = 2.dp,
        indicatorPadding: Dp = 2.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentSize()
                .height(selectedIndicatorSize + indicatorPadding * 2)
        ) {
            repeat(pageCount) { page ->
                val (color, size) =
                    if (currentPage == page || targetPage == page) {
                        val pageOffset =
                            ((currentPage - page) + currentPageOffsetFraction).absoluteValue
                        val offsetPercentage = 1f - pageOffset.coerceIn(0f, 1f)
                        val size =
                            unselectedIndicatorSize + ((selectedIndicatorSize - unselectedIndicatorSize) * offsetPercentage)
                        indicatorColor.copy(
                            alpha = offsetPercentage
                        ) to size
                    } else {
                        indicatorColor.copy(alpha = 0.1f) to unselectedIndicatorSize
                    }
                Box(
                    modifier = Modifier
                        .padding(
                            horizontal = ((selectedIndicatorSize + indicatorPadding * 2) - size) / 2,
                            vertical = size / 4
                        )
                        .clip(RoundedCornerShape(indicatorCornerRadius))
                        .background(color)
                        .width(size)
                        .height(size / 2)
                )
            }
        }
    }
}