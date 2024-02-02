package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import at.chatnet.R
import at.htlhl.chatnet.data.ChatMateResponseState
import at.htlhl.chatnet.data.InternalChatInstance
import at.htlhl.chatnet.navigation.Screens
import at.htlhl.chatnet.ui.theme.shimmerEffect
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatViewTopBar(
    navController: NavController,
    chatPartner: InternalChatInstance,
    sharedViewModel: SharedViewModel,
    onClick: (String) -> Unit
) {
    var offsetState by remember { mutableStateOf(Offset(0f, 0f)) }
    val isSearchMode = remember { mutableStateOf(false) }
    val offset by animateOffsetAsState(targetValue = offsetState, label = "")
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        if (!isSearchMode.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                IconButton(onClick = { onClick.invoke("return") }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        tint = MaterialTheme.colorScheme.primary,
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
                        .clickable {
                            onClick.invoke("profile")
                        }
                        .weight(1f)) {
                    if (!chatPartner.personList.blocked.contains(sharedViewModel.auth.currentUser?.uid.toString())) {
                        SubcomposeAsyncImage(
                            contentDescription = null,
                            model = chatPartner.personList.image,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(45.dp)
                                .shimmerEffect(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        SubcomposeAsyncImage(
                            contentDescription = null,
                            model = R.drawable.default_user,
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(45.dp),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    if (chatPartner.personList.id == "ChatMate") {
                        Column(
                            modifier = Modifier.offset(y = -offset.y.dp)
                        ) {
                            Text(
                                text = chatPartner.personList.username["mixedcase"].toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                            if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                                Text(
                                    text = "thinking...",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8f.dp)
                                )
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = chatPartner.personList.username["mixedcase"].toString(),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                            Text(
                                text = if (chatPartner.personList.online) "online" else "offline",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 7.dp)
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                        }

                    }
                }
                LaunchedEffect(sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                    offsetState =
                        if (sharedViewModel.chatMateResponseState.value == ChatMateResponseState.Loading) {
                            Offset(0f, 5f)
                        } else {
                            Offset(0f, 0f)
                        }
                }
                IconButton(onClick = {
                    isSearchMode.value = true
                }) {
                    SubcomposeAsyncImage(
                        model = R.drawable.search_svgrepo_com_1_,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(30.dp),
                    )
                }
                IconButton(onClick = {
                    navController.navigate(Screens.ProfileInfoScreen.route)
                }) {
                    SubcomposeAsyncImage(
                        model = R.drawable.info_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(30.dp),
                    )
                }
            }
        } else {
            val (text, setText) = remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current
            val interactionSource = remember { MutableInteractionSource() }
            val focusRequester = remember { FocusRequester() }
            Box(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = {
                        setText(it)
                        sharedViewModel.searchValue.value = it
                    },
                    interactionSource = interactionSource,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(
                            0.3f.dp,
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(36.dp)
                        )
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(36.dp))
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField: @Composable () -> Unit ->
                        Text(
                            text = if (sharedViewModel.searchValue.value != "") "" else "Search...",
                            modifier = Modifier.padding(top = 9.dp, start = 50.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material.IconButton(
                                onClick = {
                                    isSearchMode.value = false
                                    sharedViewModel.searchValue.value = ""
                                },
                            ) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.back_svgrepo_com_1_,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
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
}