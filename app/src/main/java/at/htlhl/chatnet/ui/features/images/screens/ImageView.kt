package at.htlhl.chatnet.ui.features.images.screens

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.ui.features.images.components.HorizontalPagerIndicatorComponent
import at.htlhl.chatnet.ui.features.images.components.ImageElementComponent
import at.htlhl.chatnet.ui.features.images.components.formatImageTimestampComponent
import at.htlhl.chatnet.viewmodels.SharedViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ImageView {
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ImageViewScreen(sharedViewModel: SharedViewModel, navController: NavController) {
        val systemUiController = rememberSystemUiController()
        systemUiController.setStatusBarColor(color = Color.Black, darkIcons = false)

        val imageListData = sharedViewModel.imageList.value
        val pageCount = imageListData.size

        val friendDataState by sharedViewModel.friend.collectAsState(initial = InternalChatInstance())
        val userDataState by sharedViewModel.userData.collectAsState()

        val userData: FirebaseUser = userDataState
        val friendData: InternalChatInstance = friendDataState

        val pagerState =
            rememberPagerState(initialPage = sharedViewModel.imageStartPosition.intValue) { pageCount }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                beyondBoundsPageCount = 1,
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
            ) { imageIndex ->
                ImageElementComponent(imageListData[imageIndex].images[0])
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
                        modifier = Modifier.fillMaxSize(),
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
                        text = if (imageListData[pagerState.currentPage].sender == userData.id) "You" else friendData.personList.username["mixedcase"].toString(),
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        color=Color.White
                    )
                    Text(
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            formatImageTimestampComponent(timestampMillis = imageListData[pagerState.currentPage].timestamp.toDate().time)
                        } else {
                            imageListData[pagerState.currentPage].timestamp.toDate().toString()
                        }, fontSize = 14.sp, color = Color.Gray
                    )
                }
            }
            HorizontalPagerIndicatorComponent(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                targetPage = pagerState.targetPage,
                currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}