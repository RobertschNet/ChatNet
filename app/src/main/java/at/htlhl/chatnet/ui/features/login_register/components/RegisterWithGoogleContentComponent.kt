package at.htlhl.chatnet.ui.features.login_register.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.util.checkIfValueIsValid

@Composable
fun RegisterWithGoogleContentComponent(
    username: String,
    usernameColor: Color,
    usernameExists: Boolean,
    usernameTextFieldColor: AccountDataState,
    isLoading: Boolean,
    registerWithGoggleErrorText: Boolean,
    onUsernameValueChange: (String) -> Unit,
    onRegisterWithGooglePressed: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create Account",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Enter a username in the field below and click 'Create Account' to create a new account.",
                    fontWeight = FontWeight.ExtraLight,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = usernameColor,
                    focusedBorderColor = usernameColor,
                    unfocusedBorderColor = usernameColor,
                    focusedLabelColor = usernameColor,
                    unfocusedLabelColor = usernameColor,
                ),
                value = username,
                supportingText = {
                    if (username.isNotEmpty()) {
                        Column {
                            if (!checkIfValueIsValid(type = "username", value = username)) {
                                Text(
                                    text = "Username is not valid",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            } else if (usernameExists) {
                                Text(
                                    text = "Username already exists",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            }
                        }
                    }
                },
                onValueChange = {
                    onUsernameValueChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Username") },
            )
            Button(
                onClick = {
                    onRegisterWithGooglePressed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = usernameTextFieldColor == AccountDataState.Valid && !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05C205))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(35.dp), color = Color.White
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(7.dp)
                    )
                }
            }
            if (registerWithGoggleErrorText) {
                Text(
                    text = "Couldn't create account. Try again later.",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}