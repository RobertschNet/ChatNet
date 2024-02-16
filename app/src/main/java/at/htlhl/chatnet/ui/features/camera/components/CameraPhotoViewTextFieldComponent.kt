package at.htlhl.chatnet.ui.features.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.BottomAppBar
import androidx.compose.material.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.chatnet.R
import coil.compose.SubcomposeAsyncImage

@Composable
fun CameraPhotoViewTextFieldComponent(
    currentTextFieldText: String,
    isLoading: Boolean,
    onRetakePhotoClicked: () -> Unit,
    onMessageSentPressed: (String) -> Unit,
    onValueChange: (String) -> Unit
) {
    var badgeCount by remember { mutableIntStateOf(0) }
    BottomAppBar(
        elevation = 0.dp,
        modifier = Modifier
            .height(70.dp + badgeCount.dp)
            .padding(bottom = 10.dp, top = 10.dp),
        backgroundColor = Color.Transparent,
    ) {
        BasicTextField(
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                onMessageSentPressed(currentTextFieldText)
            }),
            value = currentTextFieldText,
            onTextLayout = { textLayoutResult ->
                when {
                    textLayoutResult.lineCount >= 4 -> {
                        badgeCount = 36
                    }

                    textLayoutResult.lineCount == 3 -> {
                        badgeCount = 24
                    }

                    textLayoutResult.lineCount == 2 -> {
                        badgeCount = 12
                    }

                    textLayoutResult.lineCount == 1 -> {
                        badgeCount = 0
                    }
                }
            },
            maxLines = 4,
            cursorBrush = Brush.linearGradient(
                listOf(
                    Color.White, Color.White
                ), Offset.Zero, Offset.Infinite, TileMode.Clamp
            ),
            onValueChange = { changedText ->
                onValueChange(changedText)
            },
            textStyle = LocalTextStyle.current.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp + badgeCount.dp)
                .padding(start = 10.dp, end = 10.dp)
                .background(
                    Color(0xFF2B2D30).copy(alpha = 0.6f), RoundedCornerShape(26.dp)
                ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(45.dp)
                    ) {
                        IconButton(onClick = {
                            onRetakePhotoClicked()
                        }) {
                            SubcomposeAsyncImage(
                                model = R.drawable.image_redo_svgrepo_com,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                    }
                    Box(Modifier.padding(start = 10.dp, end = 70.dp)) {
                        if (currentTextFieldText.isEmpty()) {
                            Text(
                                text = "Add Caption ...",
                                textAlign = TextAlign.Start,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal,
                            )
                        }
                        innerTextField()
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF00A0E8),
                            strokeWidth = 4.dp,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(end = 2.dp)
                        )
                    } else {
                        Text(text = "Send",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF00A0E8),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                onMessageSentPressed(currentTextFieldText)

                            })
                    }
                }
            },
        )
    }
}