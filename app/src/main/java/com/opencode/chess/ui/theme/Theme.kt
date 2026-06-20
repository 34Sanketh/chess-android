package com.opencode.chess.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF1A1A2E)
val SurfaceColor = Color(0xFF16213E)
val AccentColor = Color(0xFFE94560)
val BoardLight = Color(0xFFF0D9B5)
val BoardDark = Color(0xFFB58863)
val BoardLightSelected = Color(0xFFF6F669)
val BoardDarkSelected = Color(0xFFDADA44)
val LastMoveLight = Color(0xFFCDD26A)
val LastMoveDark = Color(0xFFAAA23A)
val CheckRed = Color(0xFFFF4444)
val MoveDot = Color(0x80000000)

private val ChessColorScheme = darkColorScheme(
    primary = AccentColor,
    onPrimary = Color.White,
    secondary = Color(0xFF533483),
    onSecondary = Color.White,
    surface = SurfaceColor,
    onSurface = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
)

@Composable
fun ChessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChessColorScheme,
        content = content
    )
}
