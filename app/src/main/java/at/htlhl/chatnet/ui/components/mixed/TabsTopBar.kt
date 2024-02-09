package at.htlhl.chatnet.ui.components.mixed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUser
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TabsTopBar(
    tab: String,
    availableUsers: List<FirebaseUser>,
    sharedViewModel: SharedViewModel,
    onClick: () -> Unit = {},
) {
    val isSearchMode = remember { mutableStateOf(false) }
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isSearchMode.value) {
            Text(
                text = tab,
                fontWeight = FontWeight.Medium,
                fontSize = 30.sp,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            if (tab != "Profile" && tab != "RandChat") {
                IconButton(
                    onClick = { isSearchMode.value = true },
                    modifier = Modifier.padding(start = 108.dp, top = 5.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = R.drawable.search_svgrepo_com_1_,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
                IconButton(
                    onClick = { onClick.invoke() },
                    modifier = Modifier.padding(top = 5.dp, end = 10.dp)
                ) {
                    Box(modifier = Modifier.size(50.dp)) {
                        if (tab == "DropIn") {
                            Icon(
                                imageVector = if (sharedViewModel.gpsState.value) Icons.Outlined.LocationOff else Icons.Outlined.LocationOn,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(35.dp)
                                    .align(Alignment.Center),
                                contentDescription = null
                            )
                        } else {
                            SubcomposeAsyncImage(
                                model = if (tab == "Chats") R.drawable.add_user_social_svgrepo_com_1_ else if (tab == "ChatMate") R.drawable.chat_add_svgrepo_com_1_ else Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(35.dp)
                                    .align(Alignment.Center),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        }
                        if (availableUsers.isNotEmpty() && tab == "Chats") {
                            Box(
                                modifier = Modifier
                                    .padding(end = 7f.dp)
                                    .size(12f.dp)
                                    .zIndex(1f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color.Red, Color.Red),
                                            start = Offset(0f, 0f),
                                            end = Offset(0.dp.value, 0.dp.value)
                                        )
                                    )
                                    .align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = availableUsers.size.toString(),
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
            if (tab == "RandChat") {
                IconButton(
                    onClick = { isSearchMode.value = true },
                    modifier = Modifier.padding(top = 5.dp, end = 10.dp)
                ) {
                    SubcomposeAsyncImage(
                        model = R.drawable.search_svgrepo_com_1_,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }
            }

        } else {
            val (text, setText) = remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current
            val interactionSource = remember { MutableInteractionSource() }
            val focusRequester = remember { FocusRequester() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
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
                           Dp.Hairline,
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(36.dp)
                        )
                        .background(
                            MaterialTheme.colorScheme.background,
                            RoundedCornerShape(36.dp)
                        )
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField: @Composable () -> Unit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 10.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
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
                            innerTextField()
                        }
                    }
                )
            }
            DisposableEffect(Unit) {
                if (isSearchMode.value) {
                    keyboardController?.show()
                    focusRequester.requestFocus()
                }
                onDispose { sharedViewModel.searchValue.value = "" }
            }
        }
    }
}
