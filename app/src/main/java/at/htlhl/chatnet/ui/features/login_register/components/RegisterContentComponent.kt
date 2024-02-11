package at.htlhl.chatnet.ui.features.login_register.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import at.chatnet.R
import at.htlhl.chatnet.data.AccountDataState
import at.htlhl.chatnet.ui.features.dialogs.SecondFADialog
import at.htlhl.chatnet.util.checkIfValueIsValid
import coil.compose.SubcomposeAsyncImage

@Composable
fun RegisterContentComponent(
    username: String,
    email: String,
    password: String,
    usernameTexFieldColor: AccountDataState,
    emailTexFieldColor: AccountDataState,
    passwordTexFieldColor: AccountDataState,
    usernameColor: Color,
    emailColor: Color,
    passwordColor: Color,
    usernameExists: Boolean,
    emailExists: Boolean,
    registerErrorText: Boolean,
    isLoading: Boolean,
    openDialog: Boolean,
    onNavigate: () -> Unit,
    onUsernameValueChange: (String) -> Unit,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onRegisterPressed: () -> Unit,
    onGoogleRegisterPressed: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp, start = 30.dp, end = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create an Account",
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 34.sp,
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
                supportingText = {
                    if (username.isNotEmpty()) {
                        Column {
                            if (usernameExists) {
                                Text(
                                    text = "Username already exists",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            } else if (!checkIfValueIsValid(
                                    type = "username", value = username
                                )
                            ) {
                                Text(
                                    text = "Username is invalid",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            }
                        }
                    }
                },
                value = username,
                onValueChange = {
                    onUsernameValueChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Username") },
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = emailColor,
                    focusedBorderColor = emailColor,
                    unfocusedBorderColor = emailColor,
                    focusedLabelColor = emailColor,
                    unfocusedLabelColor = emailColor,
                ),
                value = email,
                supportingText = {
                    if (email.isNotEmpty()) {
                        Column {
                            if (emailExists) {
                                Text(
                                    text = "Email is already in use",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = FontFamily.SansSerif,
                                )
                            } else if (!checkIfValueIsValid(type = "email", value = email)) {
                                Text(
                                    text = "Email is invalid",
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
                    onEmailValueChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Email") },
            )
            OutlinedTextField(
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = passwordColor,
                    focusedBorderColor = passwordColor,
                    unfocusedBorderColor = passwordColor,
                    focusedLabelColor = passwordColor,
                    unfocusedLabelColor = passwordColor,
                ),
                supportingText = {
                    if (!checkIfValueIsValid(
                            type = "password", value = password
                        ) && password.isNotEmpty()
                    ) {
                        Text(
                            text = "Password is invalid",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.SansSerif,
                        )
                    }
                },
                value = password,
                onValueChange = {
                    onPasswordValueChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                label = { Text(text = "Password") },
            )
            Button(
                onClick = {
                    onRegisterPressed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = usernameTexFieldColor == AccountDataState.Valid && emailTexFieldColor == AccountDataState.Valid && passwordTexFieldColor == AccountDataState.Valid && !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05C205))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(35.dp), color = Color.White
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(7.dp)
                    )
                }
            }
            if (registerErrorText) {
                Text(
                    text = "Registration failed please try again later!",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            if (openDialog) {
                SecondFADialog {
                    onNavigate()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Divider(
                    thickness = 0.3f.dp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .weight(1f)
                )
                Text(
                    text = "OR",
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                )
                Divider(
                    thickness = 0.3f.dp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(enabled = !isLoading, onClick = {
                    onGoogleRegisterPressed()
                }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = R.drawable.icon_google,
                            contentDescription = "Google",
                            modifier = Modifier.size(45.dp)
                        )
                        Text(
                            text = "Sign up with Google",
                            color = if (isSystemInDarkTheme()) Color.White else Color.DarkGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}