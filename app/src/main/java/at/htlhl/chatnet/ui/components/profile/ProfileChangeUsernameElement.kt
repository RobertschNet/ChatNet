package at.htlhl.chatnet.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileChangeUsernameElement(
    userData: FirebaseUser,
    usernameText: String,
    usernameTextFieldColor: Color,
    focusRequester: FocusRequester,
    isLoading: Boolean,
    usernameAlreadyExists: Boolean,
    changeUsernameException: Boolean,
    onValueChange: (String) -> Unit,
    onSavePressed: () -> Unit,
    onDismissModelBottomSheet: () -> Unit,
) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {},
        shape = RectangleShape,
        windowInsets = WindowInsets(0, 0, 0, 0),
        onDismissRequest = { onDismissModelBottomSheet.invoke() },
    ) {
        Column {
            Text(
                text = "Enter your new username",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.SansSerif,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 20.dp, start = 20.dp)
            )
            TextField(
                value = usernameText,
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = usernameText.length.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Start,
                        fontFamily = FontFamily.SansSerif,
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                onValueChange = {
                    onValueChange.invoke(it)
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 18.sp,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif
                ),
                placeholder = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = userData.username["mixedcase"].toString(),
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Start,
                            fontFamily = FontFamily.SansSerif,
                        )
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = Color.Transparent,
                    focusedIndicatorColor = usernameTextFieldColor,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester)
                    .padding(start = 20.dp, end = 40.dp)
            )
            if (!profileCheckIfUsernameIsValid(usernameText)) {
                Text(
                    text = "Username is invalid.",
                    color = Color.Red,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 40.dp,
                        top = 5.dp
                    )
                )
            }
            if (usernameAlreadyExists) {
                Text(
                    text = "Username already exists.",
                    color = Color.Red,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 40.dp,
                        top = 5.dp
                    )
                )
            }
            if (changeUsernameException) {
                Text(
                    text = "An error occurred while changing your username.",
                    color = Color.Red,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 40.dp,
                        top = 5.dp
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Cancel",
                    textAlign = TextAlign.End,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF00A0E8),
                    modifier = Modifier
                        .clickable {
                            onDismissModelBottomSheet.invoke()
                        }
                        .padding(end = 40.dp)
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .size(25.dp),
                        color = Color(0xFF00A0E8)
                    )
                } else {
                    Text(
                        text = "Save",
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.End,
                        color = if (usernameTextFieldColor != Color.Red) Color(
                            0xFF00A0E8
                        ) else Color.Gray,
                        modifier = Modifier
                            .clickable(enabled = usernameTextFieldColor != Color.Red) {
                                onSavePressed.invoke()
                            }
                            .padding(end = 20.dp)
                    )
                }
            }
        }
    }
}