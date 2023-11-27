package at.htlhl.chatnet.ui.components.chatmate

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatMateTopBar(
    sharedViewModel: SharedViewModel,
    onClick: () -> Unit,
) {
    val isSearchMode = remember { mutableStateOf(false) }
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isSearchMode.value) {
            Text(
                text = "ChatMate",
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                fontFamily = FontFamily.Cursive,
                color = MaterialTheme.colorScheme.primary,
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
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(30.dp),
                )
            }
            IconButton(
                onClick = { onClick.invoke() },
                modifier = Modifier.padding(top = 5.dp, end = 10.dp)
            ) {
                Box(modifier = Modifier.size(50.dp)) {
                    SubcomposeAsyncImage(
                        model = R.drawable.add_circle_svgrepo_com,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
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
                            IconButton(
                                onClick = {
                                    isSearchMode.value = false
                                    sharedViewModel.searchValue.value = ""
                                },
                            ) {
                                SubcomposeAsyncImage(
                                    model = R.drawable.back_svgrepo_com_1_,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
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
}