package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import at.chatnet.R
import at.htlhl.chatnet.data.FirebaseUsers
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatsViewTopBar(
    availableUsers: List<FirebaseUsers>,
    coroutineScope: CoroutineScope,
    sharedViewModel: SharedViewModel,
    onClick: () -> Unit,
) {
    val isSearchMode = remember { mutableStateOf(false) }
    TopAppBar(
        backgroundColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isSearchMode.value) {
            Text(
                text = "ChatNet",
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                fontFamily = FontFamily.Cursive,
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            IconButton(
                onClick = { isSearchMode.value = true },
                modifier = Modifier.padding(start = 108.dp, top = 5.dp)
            ) {
                SubcomposeAsyncImage(
                    model = R.drawable.search_svgrepo_com_1_,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                )
            }
            IconButton(
                onClick = { onClick.invoke() },
                modifier = Modifier.padding(top = 5.dp, end = 10.dp)
            ) {
                Box(modifier = Modifier.size(50.dp)) {
                    SubcomposeAsyncImage(
                        model = R.drawable.add_user_social_svgrepo_com_1_,
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.Center),
                    )
                    if (availableUsers.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(end = 7f.dp)
                                .size(12f.dp)
                                .zIndex(1f)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = if (!isSystemInDarkTheme()) listOf(
                                            Color.Red, Color.Red
                                        ) else listOf(Color(0xF1161616), Color(0xF1161616)),
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
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
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
                .background(Color.White, RoundedCornerShape(36.dp))
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
                    modifier = textFieldModifier.focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.body1.copy(color = Color.Black),
                    cursorBrush = SolidColor(if (isSystemInDarkTheme()) Color.White else Color.Black),
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
