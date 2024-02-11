package at.htlhl.chatnet.ui.features.login_register.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.htlhl.chatnet.navigation.Screens

@Composable
fun LoginBottomBarComponent(
    onNavigateToRegister: () -> Unit

) {
    Divider(
        thickness = 0.3f.dp,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(bottom = 50.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Don't have an account?",
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = " Sign up.",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
                onNavigateToRegister()
            }
        )
    }
}