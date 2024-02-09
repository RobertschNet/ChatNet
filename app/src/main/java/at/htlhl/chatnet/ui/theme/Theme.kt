package at.htlhl.chatnet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val darkColorScheme = darkColorScheme(
    background = Color(0xFF101016),
    primary = Color.White,
    secondary = Color.LightGray,
    tertiary = Color(0xFFD0D0D3),
    outline = Color(0xFFDAD7D7),
    onBackground = Color(0xFF141419)
)

private val lightColorScheme = lightColorScheme(
    background = Color.White,
    primary = Color.Black,
    secondary = Color.Gray,
    tertiary = Color.Gray,
    outline = Color(0xFFB6B6B6),
    onBackground = Color.White
)

@Composable
fun ChatNetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) darkColorScheme else lightColorScheme
        }

        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (!darkTheme) Color.White.toArgb() else Color(0xFF101016).toArgb()
            window.navigationBarColor = if (!darkTheme) Color.White.toArgb() else Color(0xFF101016).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )

}