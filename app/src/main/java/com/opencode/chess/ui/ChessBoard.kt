package com.opencode.chess.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.chess.engine.Color as PieceColor
import com.opencode.chess.engine.Move
import com.opencode.chess.engine.Piece
import com.opencode.chess.engine.PieceType
import com.opencode.chess.ui.theme.AccentGold
import com.opencode.chess.ui.theme.BlackPiece
import com.opencode.chess.ui.theme.BoardBorder
import com.opencode.chess.ui.theme.BoardDark
import com.opencode.chess.ui.theme.BoardDarkSelected
import com.opencode.chess.ui.theme.BoardLight
import com.opencode.chess.ui.theme.BoardLightSelected
import com.opencode.chess.ui.theme.CheckRed
import com.opencode.chess.ui.theme.CoordinateText
import com.opencode.chess.ui.theme.LastMoveDark
import com.opencode.chess.ui.theme.LastMoveLight
import com.opencode.chess.ui.theme.MoveDot
import com.opencode.chess.ui.theme.WhitePiece

@Composable
fun ChessBoard(
    board: Array<Array<Piece?>>,
    selectedSquare: Int?,
    legalMoves: List<Int>,
    lastMove: Move?,
    checkSquares: List<Int>,
    playerColor: PieceColor,
    flipped: Boolean,
    onSquareClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    animatedFrom: Int? = null,
    animatedTo: Int? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(animatedFrom, animatedTo) {
        if (animatedFrom != null && animatedTo != null) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, animationSpec = tween(200))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(6.dp)
            .shadow(16.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(onSquareClick) {
                    detectTapGestures { offset ->
                        val sqSize = size.width / 8f
                        val col = (offset.x / sqSize).toInt().coerceIn(0, 7)
                        val row = (offset.y / sqSize).toInt().coerceIn(0, 7)
                        val displayRow = if (flipped) 7 - row else row
                        val displayCol = if (flipped) 7 - col else col
                        onSquareClick(displayRow * 8 + displayCol)
                    }
                }
        ) {
            val sqSize = size.width / 8f
            drawBoardBorder(sqSize)
            drawBoard(sqSize)
            drawHighlights(sqSize, selectedSquare, legalMoves, lastMove, checkSquares, flipped)
            drawCoordinates(sqSize, flipped, textMeasurer)
            drawPieces(sqSize, board, flipped, textMeasurer, animatedFrom, animatedTo, animProgress.value)
        }
    }
}

private fun DrawScope.drawBoardBorder(sqSize: Float) {
    val totalSize = sqSize * 8
    val border = sqSize * 0.12f
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(BoardBorder, Color(0xFF2D1810), BoardBorder)
        ),
        topLeft = Offset(-border, -border),
        size = Size(totalSize + border * 2, totalSize + border * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(AccentGold.copy(alpha = 0.3f), AccentGold.copy(alpha = 0.1f), AccentGold.copy(alpha = 0.3f))
        ),
        topLeft = Offset(-border * 0.3f, -border * 0.3f),
        size = Size(totalSize + border * 0.6f, totalSize + border * 0.6f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = border * 0.15f),
    )
}

private fun DrawScope.drawBoard(sqSize: Float) {
    for (row in 0..7) {
        for (col in 0..7) {
            val isLight = (row + col) % 2 == 0
            val x = col * sqSize
            val y = row * sqSize
            if (isLight) {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(BoardLight, BoardLight.copy(red = BoardLight.red * 0.95f)),
                        center = Offset(x + sqSize / 2, y + sqSize / 2),
                        radius = sqSize * 0.7f,
                    ),
                    topLeft = Offset(x, y),
                    size = Size(sqSize, sqSize),
                )
            } else {
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(BoardDark, BoardDark.copy(red = BoardDark.red * 0.9f)),
                        center = Offset(x + sqSize / 2, y + sqSize / 2),
                        radius = sqSize * 0.7f,
                    ),
                    topLeft = Offset(x, y),
                    size = Size(sqSize, sqSize),
                )
            }
        }
    }
}

private fun DrawScope.drawCoordinates(sqSize: Float, flipped: Boolean, textMeasurer: TextMeasurer) {
    val files = "abcdefgh"
    val ranks = "12345678"
    val pad = sqSize * 0.06f
    val style = TextStyle(fontSize = (sqSize * 0.18f).sp, color = CoordinateText, fontWeight = FontWeight.Normal)

    for (i in 0..7) {
        val file = if (flipped) files[7 - i] else files[i]
        val rank = if (flipped) ranks[i] else ranks[7 - i]
        val fl = textMeasurer.measure(AnnotatedString(file.toString()), style)
        val rl = textMeasurer.measure(AnnotatedString(rank.toString()), style)
        drawText(fl, topLeft = Offset(i * sqSize + sqSize - fl.size.width - pad, pad))
        drawText(rl, topLeft = Offset(pad, (i + 1) * sqSize - rl.size.height - pad))
    }
}

private fun DrawScope.drawHighlights(
    sqSize: Float,
    selectedSquare: Int?,
    legalMoves: List<Int>,
    lastMove: Move?,
    checkSquares: List<Int>,
    flipped: Boolean,
) {
    if (lastMove != null) {
        val lrFrom = if (flipped) 7 - lastMove.fromRow else lastMove.fromRow
        val lcFrom = if (flipped) 7 - lastMove.fromCol else lastMove.fromCol
        val lrTo = if (flipped) 7 - lastMove.toRow else lastMove.toRow
        val lcTo = if (flipped) 7 - lastMove.toCol else lastMove.toCol
        val isLightFrom = (lrFrom + lcFrom) % 2 == 0
        val isLightTo = (lrTo + lcTo) % 2 == 0
        drawRect(color = if (isLightFrom) LastMoveLight else LastMoveDark, topLeft = Offset(lcFrom * sqSize, lrFrom * sqSize), size = Size(sqSize, sqSize))
        drawRect(color = if (isLightTo) LastMoveLight else LastMoveDark, topLeft = Offset(lcTo * sqSize, lrTo * sqSize), size = Size(sqSize, sqSize))
    }

    if (selectedSquare != null) {
        val row = if (flipped) 7 - (selectedSquare / 8) else selectedSquare / 8
        val col = if (flipped) 7 - (selectedSquare % 8) else selectedSquare % 8
        val isLight = (row + col) % 2 == 0
        drawRect(color = if (isLight) BoardLightSelected else BoardDarkSelected, topLeft = Offset(col * sqSize, row * sqSize), size = Size(sqSize, sqSize))
        drawRect(color = AccentGold.copy(alpha = 0.4f), topLeft = Offset(col * sqSize, row * sqSize), size = Size(sqSize, sqSize), style = Stroke(width = sqSize * 0.04f))
    }

    for (sq in legalMoves) {
        val row = if (flipped) 7 - (sq / 8) else sq / 8
        val col = if (flipped) 7 - (sq % 8) else sq % 8
        drawCircle(AccentGold.copy(alpha = 0.5f), sqSize * 0.12f, Offset(col * sqSize + sqSize / 2, row * sqSize + sqSize / 2))
    }

    for (sq in checkSquares) {
        val row = if (flipped) 7 - (sq / 8) else sq / 8
        val col = if (flipped) 7 - (sq % 8) else sq % 8
        val cx = col * sqSize + sqSize / 2
        val cy = row * sqSize + sqSize / 2
        drawCircle(CheckRed.copy(alpha = 0.3f), sqSize * 0.45f, Offset(cx, cy), style = Fill)
        drawCircle(CheckRed, sqSize * 0.45f, Offset(cx, cy), style = Stroke(width = sqSize * 0.06f))
    }
}

private fun DrawScope.drawPieces(
    sqSize: Float,
    board: Array<Array<Piece?>>,
    flipped: Boolean,
    textMeasurer: TextMeasurer,
    animatedFrom: Int?,
    animatedTo: Int?,
    animProgress: Float,
) {
    for (row in 0..7) {
        for (col in 0..7) {
            val displayRow = if (flipped) 7 - row else row
            val displayCol = if (flipped) 7 - col else col
            val piece = board[displayRow][displayCol] ?: continue
            val sqIndex = displayRow * 8 + displayCol

            if (sqIndex == animatedTo && animatedFrom != null) {
                val fromRow = if (flipped) 7 - (animatedFrom / 8) else animatedFrom / 8
                val fromCol = if (flipped) 7 - (animatedFrom % 8) else animatedFrom % 8
                val fromX = fromCol * sqSize + sqSize / 2
                val fromY = fromRow * sqSize + sqSize / 2
                val toX = col * sqSize + sqSize / 2
                val toY = row * sqSize + sqSize / 2
                val cx = fromX + (toX - fromX) * animProgress
                val cy = fromY + (toY - fromY) * animProgress
                drawPiece(piece, cx, cy, sqSize * 0.43f, textMeasurer)
            } else if (sqIndex != animatedFrom || animatedTo == null) {
                val cx = col * sqSize + sqSize / 2
                val cy = row * sqSize + sqSize / 2
                drawPiece(piece, cx, cy, sqSize * 0.43f, textMeasurer)
            }
        }
    }
}

private fun DrawScope.drawPiece(piece: Piece, cx: Float, cy: Float, r: Float, textMeasurer: TextMeasurer) {
    val bg = if (piece.color == PieceColor.WHITE) WhitePiece else BlackPiece
    val fg = if (piece.color == PieceColor.WHITE) Color(0xFF2D2D2D) else Color(0xFFE8E0D0)
    val shadow = Color(0x55000000)
    val highlight = if (piece.color == PieceColor.WHITE) Color(0xFFFFFFFF) else Color(0xFF3D3D3D)

    drawCircle(shadow, r, Offset(cx + r * 0.06f, cy + r * 0.06f))
    drawCircle(
        brush = Brush.radialGradient(listOf(highlight, bg, bg.copy(red = bg.red * 0.85f)), center = Offset(cx - r * 0.2f, cy - r * 0.2f), radius = r),
        radius = r, center = Offset(cx, cy),
    )
    drawCircle(fg.copy(alpha = 0.2f), r, Offset(cx, cy), style = Stroke(width = r * 0.05f))

    val symbol = if (piece.color == PieceColor.WHITE) {
        when (piece.type) {
            PieceType.KING -> "\u2654"
            PieceType.QUEEN -> "\u2655"
            PieceType.ROOK -> "\u2656"
            PieceType.BISHOP -> "\u2657"
            PieceType.KNIGHT -> "\u2658"
            PieceType.PAWN -> "\u2659"
        }
    } else {
        when (piece.type) {
            PieceType.KING -> "\u265A"
            PieceType.QUEEN -> "\u265B"
            PieceType.ROOK -> "\u265C"
            PieceType.BISHOP -> "\u265D"
            PieceType.KNIGHT -> "\u265E"
            PieceType.PAWN -> "\u265F"
        }
    }

    val tl = textMeasurer.measure(
        AnnotatedString(symbol),
        TextStyle(fontSize = (r * 1.5f).sp, fontWeight = FontWeight.Bold, color = fg),
    )
    drawText(tl, topLeft = Offset(cx - tl.size.width / 2, cy - tl.size.height / 2))
}
