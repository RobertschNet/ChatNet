package at.htlhl.chatnet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.R
import at.htlhl.chatnet.viewmodels.SharedViewModel
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatsViewBottomSheetTopBar(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    sharedViewModel: SharedViewModel,
    onClick: () -> Unit,
) {
    val isSearchMode = remember {
        mutableStateOf(false)
    }
    TopAppBar(
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
        modifier =
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
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
                enabled = !(bottomSheetScaffoldState.bottomSheetState.isExpanded),
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
                    onClick.invoke()
                },
                enabled = !(bottomSheetScaffoldState.bottomSheetState.isExpanded),
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
