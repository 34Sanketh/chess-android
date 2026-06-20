package com.opencode.chess.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0C0C1A)
val SurfaceColor = Color(0xFF1A1A2E)
val AccentGold = Color(0xFFFFD700)
val AccentGoldDark = Color(0xFFB8960C)
val BoardLight = Color(0xFFF0D9B5)
val BoardDark = Color(0xFF946F51)
val BoardLightSelected = Color(0xFFF6F669)
val BoardDarkSelected = Color(0xFFC8C830)
val LastMoveLight = Color(0xFFCDD26A)
val LastMoveDark = Color(0xFFAAA23A)
val CheckRed = Color(0xFFFF4444)
val MoveDot = Color(0x80000000)
val WhitePiece = Color(0xFFFFFEF7)
val BlackPiece = Color(0xFF1A1A1A)
val BoardBorder = Color(0xFF3E2723)
val CoordinateText = Color(0xFFD4C5A9)

private val PremiumColorScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = Color(0xFF1A1A2E),
    secondary = AccentGoldDark,
    onSecondary = Color.White,
    surface = SurfaceColor,
    onSurface = Color(0xFFE0D5C1),
    background = DarkBackground,
    onBackground = Color(0xFFE0D5C1),
)

@Composable
fun ChessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PremiumColorScheme,
        content = content
    )
}
