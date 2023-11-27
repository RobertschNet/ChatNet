package at.htlhl.chatnet.ui.components.randchat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.theme.shimmerEffect
import coil.compose.SubcomposeAsyncImage

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LoadingChat(navController: NavController) {
    Scaffold(topBar = {
        TopAppBar(
            backgroundColor = Color.White,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                IconButton(onClick = { navController.navigate(Screens.RandChatStartScreen.route) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        tint = Color.Black,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .weight(1f)
                ) {

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(45.dp)
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(26))
                            .shimmerEffect()
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
                IconButton(onClick = {
                    //TODO: Block User
                }) {
                    SubcomposeAsyncImage(
                        model = R.drawable.person_block_svgrepo_com,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                    )
                }
                IconButton(onClick = {
                    //TODO: Search Messages
                }) {
                    SubcomposeAsyncImage(
                        model = R.drawable.search_svgrepo_com_1_,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                    )
                }
                IconButton(onClick = {
                    //TODO: Info
                }) {
                    SubcomposeAsyncImage(
                        model = R.drawable.info_svgrepo_com,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp),
                    )
                }
            }
        }

    }, content = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 15.dp, bottom = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 10.dp)
                    .width(200.dp)
                    .height(35.dp)
                    .clip(RoundedCornerShape(26))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .align(Alignment.Start)
                    .clip(RoundedCornerShape(26))
                    .height(130.dp)
                    .width(180.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(26))
                    .height(35.dp)
                    .width(180.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(26))
                    .height(35.dp)
                    .width(100.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .align(Alignment.Start)
                    .clip(RoundedCornerShape(26))
                    .height(35.dp)
                    .width(200.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(26))
                    .height(180.dp)
                    .width(145.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .align(Alignment.Start)
                    .clip(RoundedCornerShape(26))
                    .height(35.dp)
                    .width(90.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .align(Alignment.Start)
                    .clip(RoundedCornerShape(26))
                    .height(35.dp)
                    .width(160.dp)
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(26))
                    .height(35.dp)
                    .width(185.dp)
                    .shimmerEffect()
            )
        }

    },
        bottomBar = {
            BottomAppBar(
                elevation = 10.dp,
                modifier = Modifier.height(70.dp),
                backgroundColor = Color.White
            ) {
                Column {
                    BasicTextField(
                        value = "",
                        enabled = false,
                        maxLines = 4,
                        cursorBrush = Brush.linearGradient(
                            listOf(
                                Color(0xFF00A0E8), Color(0xFF00A0E8), Color(
                                    0xFF0CB0FA
                                ), Color.White
                            ), Offset.Zero, Offset.Infinite, TileMode.Clamp
                        ),
                        onValueChange = { },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            color = Color.Gray,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(start = 10.dp, end = 10.dp)
                            .background(
                                if (isSystemInDarkTheme()) Color.Black else Color.White,
                                RoundedCornerShape(26.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(26.dp),
                            ),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF00A0E8), Color(0xFF00A0E8), Color(
                                                        0xFF0CB0FA
                                                    ), Color.White
                                                )
                                            ), RoundedCornerShape(24.dp)
                                        )
                                ) {

                                    IconButton(
                                        enabled = false,
                                        onClick = { }
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = R.drawable.camera_svgrepo_com_5_,
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(Color.White),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Box(Modifier.padding(start = 10.dp, end = 70.dp)) {
                                    Text(
                                        text = "Message...",
                                        textAlign = TextAlign.Start,
                                        fontSize = 18.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Normal,
                                    )

                                    innerTextField()
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    enabled = false,
                                    onClick = {

                                    }) {
                                    SubcomposeAsyncImage(
                                        model = R.drawable.gallery_svgrepo_com,
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                        },
                    )
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .weight(1f)
                )
            }

        }
    )
}
