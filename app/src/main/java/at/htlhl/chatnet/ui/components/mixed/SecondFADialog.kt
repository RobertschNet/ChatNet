package at.htlhl.chatnet.ui.components.mixed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import at.chatnet.R
import coil.compose.SubcomposeAsyncImage

@Preview
@Composable
fun SecondFADialog(onDismiss: () -> Unit={}) {
    val contextForToast = LocalContext.current.applicationContext
    Dialog(
        onDismissRequest = {
            onDismiss()
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(color = Color(0xFF35898f)),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .padding(top = 16.dp, bottom = 16.dp)
                            .align(Alignment.Center),
                        model= R.drawable.email_8_svgrepo_com,
                        contentDescription = "2-Step Verification",
                    )
                }

                Text(
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    text = "2-Step Verification",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp
                    )
                )

                Text(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                    text = "Setup 2-Step Verification to add additional layer of security to your account.",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp
                    )
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp, start = 36.dp, end = 36.dp, bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35898f)),
                    onClick = {
                        onDismiss()
                        Toast.makeText(
                            contextForToast,
                            "Click: Setup Now",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    Text(
                        text = "Setup Now",
                        color = Color.White,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp
                        )
                    )
                }

                TextButton(
                    onClick = {
                        onDismiss()
                        Toast.makeText(
                            contextForToast,
                            "Click: I'll Do It Later",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    Text(
                        text = "I'll Do It Later",
                        color = Color(0xFF35898f),
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}