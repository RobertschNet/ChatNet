package at.htlhl.testing.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.htlhl.testing.R
import at.htlhl.testing.data.BottomSheetItems
import at.htlhl.testing.viewmodels.SharedViewModel
import at.htlhl.testing.data.InternalChatInstances
import at.htlhl.testing.navigation.Screens
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BottomSheetContent(
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


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun BottomSheetTopBar(
    navController: NavController,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    sharedViewModel: SharedViewModel
) {
    val isSearchMode = remember {
        mutableStateOf(false)
    }
    TopAppBar(
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
        modifier =
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded || bottomSheetScaffoldState.bottomSheetState.isAnimationRunning) {
            Modifier
                .clickable { coroutineScope.launch { bottomSheetScaffoldState.bottomSheetState.collapse() } }
                .height(55.dp)
                .fillMaxWidth()
        } else {
            Modifier
                .height(55.dp)
                .fillMaxWidth()
        }
    ) {
        if (!isSearchMode.value) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row {
                    Text(
                        text = "ChatNet",
                        modifier = Modifier
                            .padding(top = 5.dp, start = 20.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        fontFamily = FontFamily.Cursive
                    )
                    IconButton(
                        onClick = {
                            isSearchMode.value = true

                        },
                        enabled = !(bottomSheetScaffoldState.bottomSheetState.isAnimationRunning || bottomSheetScaffoldState.bottomSheetState.isExpanded),
                        modifier = Modifier
                            .padding(start = 110.dp, top = 5.dp)
                    ) {
                        SubcomposeAsyncImage(
                            model = R.drawable.search_svgrepo_com_1_,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            loading = {
                                CircularProgressIndicator()
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate(Screens.InboxScreen.route)
                        },
                        enabled = !(bottomSheetScaffoldState.bottomSheetState.isAnimationRunning || bottomSheetScaffoldState.bottomSheetState.isExpanded),
                        modifier = Modifier
                            .padding(top = 5.dp, end = 10.dp)
                    ) {
                        SubcomposeAsyncImage(
                            model = R.drawable.inbox_svgrepo_com,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            loading = {
                                CircularProgressIndicator()
                            }
                        )
                    }
                }

            }
        } else {
            val (text, setText) = remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current
            val interactionSource = remember { MutableInteractionSource() }
            val focusRequester = remember { FocusRequester() }
            val textFieldModifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(0.3f.dp, Color.DarkGray, RoundedCornerShape(36.dp))
                .background(
                    Color.White,
                    RoundedCornerShape(36.dp)
                )

            Box(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = {
                        setText(it)
                        sharedViewModel.searchtext.value = it
                    },
                    interactionSource = interactionSource,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    singleLine = true,
                    modifier = textFieldModifier.focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.body1.copy(color = Color.Black),
                    cursorBrush = SolidColor(if (isSystemInDarkTheme()) Color.White else Color.Black),
                    decorationBox = { innerTextField: @Composable () -> Unit ->
                        Text(
                            text = if (sharedViewModel.searchtext.value != "") "" else "Search...",
                            modifier = Modifier.padding(top = 9.dp, start = 50.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isSearchMode.value = false
                                    sharedViewModel.searchtext.value = ""
                                },
                            ) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.back_svgrepo_com_1_,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp),
                                    loading = {
                                        CircularProgressIndicator()
                                    }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, end = 8.dp, start = 0.dp)
                                    .height(30.dp)
                            ) {
                                innerTextField()
                            }

                        }
                    }
                )
            }

            DisposableEffect(Unit) {
                if (isSearchMode.value) {
                    keyboardController?.show()
                    focusRequester.requestFocus()
                }
                onDispose { }
            }
        }


    }
    Divider(
        thickness = 1.dp,
        color = if (isSystemInDarkTheme()) Color.DarkGray else Color.Transparent
    )
}
