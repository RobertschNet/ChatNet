package at.htlhl.chatnet.ui.features.login_register.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import at.htlhl.chatnet.util.checkIfValueIsValid
import coil.compose.SubcomposeAsyncImage

@Composable
fun LoginContentComponent(
    email: String,
    emailTexFieldColor: AccountDataState,
    emailIsNotVerifiedText: Boolean,
    emailColor: Color,
    wrongEmailText: Boolean,
    password: String,
    passwordTexFieldColor: AccountDataState,
    passwordColor: Color,
    wrongPasswordText: Boolean,
    accountDisabledOrNotFoundText: Boolean,
    loginErrorText: Boolean,
    isLoading: Boolean,
    onNavigate: () -> Unit,
    onEmailValueChange: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onSignInPressed: () -> Unit,
    onGoogleSignInPressed: () -> Unit
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
                    text = "ChatNet",
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 45.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
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
                    if (!checkIfValueIsValid(
                            type = "email", value = email
                        ) && email.isNotEmpty()
                    ) {
                        Text(
                            text = "Email is invalid",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.SansSerif,
                        )
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
                value = password,
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
                    onSignInPressed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                enabled = passwordTexFieldColor == AccountDataState.Valid && emailTexFieldColor == AccountDataState.Valid && !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF05C205))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, modifier = Modifier.size(35.dp)
                    )
                } else {
                    Text(
                        text = "Sign In",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(7.dp)
                    )
                }
            }
            if (emailIsNotVerifiedText) {
                Text(
                    text = "Please verify your email first!",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            if (wrongEmailText) {
                Text(
                    text = "Email does not exist!",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            if (wrongPasswordText) {
                Text(
                    text = "Wrong password!",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            if (loginErrorText) {
                Text(
                    text = "Login failed please try again later!",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            if (accountDisabledOrNotFoundText) {
                Text(
                    text = "Account disabled or not found!",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Forgot your password?",
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                Text(text = " Reset password.",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        onNavigate()
                    }
                )
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
                    .fillMaxWidth()
                    .padding(top = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(enabled = !isLoading, onClick = {
                    onGoogleSignInPressed()
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
                            text = "Sign in with Google",
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