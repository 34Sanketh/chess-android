package com.opencode.chess.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.opencode.chess.engine.Color as PieceColor
import com.opencode.chess.engine.Move
import com.opencode.chess.engine.Piece
import com.opencode.chess.engine.PieceType
import com.opencode.chess.ui.theme.BoardDark
import com.opencode.chess.ui.theme.BoardDarkSelected
import com.opencode.chess.ui.theme.BoardLight
import com.opencode.chess.ui.theme.BoardLightSelected
import com.opencode.chess.ui.theme.CheckRed
import com.opencode.chess.ui.theme.LastMoveDark
import com.opencode.chess.ui.theme.LastMoveLight
import com.opencode.chess.ui.theme.MoveDot

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
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .shadow(12.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(onSquareClick) {
                    detectTapGestures { offset ->
                        val size = this.size
                        val sqSize = size.width / 8f
                        val col = (offset.x / sqSize).toInt().coerceIn(0, 7)
                        val row = (offset.y / sqSize).toInt().coerceIn(0, 7)
                        val displayRow = if (flipped) 7 - row else row
                        val displayCol = if (flipped) 7 - col else col
                        val index = displayRow * 8 + displayCol
                        onSquareClick(index)
                    }
                }
        ) {
            val sqSize = size.width / 8f

            drawBoard(sqSize)
            drawHighlights(sqSize, selectedSquare, legalMoves, lastMove, checkSquares, flipped)
            drawPieces(sqSize, board, flipped)
        }
    }
}

private fun DrawScope.drawBoard(sqSize: Float) {
    for (row in 0..7) {
        for (col in 0..7) {
            val isLight = (row + col) % 2 == 0
            val x = col * sqSize
            val y = row * sqSize
            drawRect(
                color = if (isLight) BoardLight else BoardDark,
                topLeft = Offset(x, y),
                size = Size(sqSize, sqSize),
            )
        }
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
    val rank = if (flipped) 7 else 0

    if (lastMove != null) {
        val lrFrom = if (flipped) 7 - lastMove.fromRow else lastMove.fromRow
        val lcFrom = if (flipped) 7 - lastMove.fromCol else lastMove.fromCol
        val lrTo = if (flipped) 7 - lastMove.toRow else lastMove.toRow
        val lcTo = if (flipped) 7 - lastMove.toCol else lastMove.toCol
        val isLightFrom = (lrFrom + lcFrom) % 2 == 0
        val isLightTo = (lrTo + lcTo) % 2 == 0
        drawRect(
            color = if (isLightFrom) LastMoveLight else LastMoveDark,
            topLeft = Offset(lcFrom * sqSize, lrFrom * sqSize),
            size = Size(sqSize, sqSize),
        )
        drawRect(
            color = if (isLightTo) LastMoveLight else LastMoveDark,
            topLeft = Offset(lcTo * sqSize, lrTo * sqSize),
            size = Size(sqSize, sqSize),
        )
    }

    if (selectedSquare != null) {
        val row = if (flipped) 7 - (selectedSquare / 8) else selectedSquare / 8
        val col = if (flipped) 7 - (selectedSquare % 8) else selectedSquare % 8
        val isLight = (row + col) % 2 == 0
        drawRect(
            color = if (isLight) BoardLightSelected else BoardDarkSelected,
            topLeft = Offset(col * sqSize, row * sqSize),
            size = Size(sqSize, sqSize),
        )
    }

    for (sq in legalMoves) {
        val row = if (flipped) 7 - (sq / 8) else sq / 8
        val col = if (flipped) 7 - (sq % 8) else sq % 8
        val cx = col * sqSize + sqSize / 2
        val cy = row * sqSize + sqSize / 2
        drawCircle(MoveDot, sqSize * 0.15f, Offset(cx, cy))
    }

    for (sq in checkSquares) {
        val row = if (flipped) 7 - (sq / 8) else sq / 8
        val col = if (flipped) 7 - (sq % 8) else sq % 8
        val cx = col * sqSize + sqSize / 2
        val cy = row * sqSize + sqSize / 2
        drawCircle(CheckRed, sqSize * 0.45f, Offset(cx, cy), style = Stroke(width = sqSize * 0.06f))
    }
}

private fun DrawScope.drawPieces(sqSize: Float, board: Array<Array<Piece?>>, flipped: Boolean) {
    for (row in 0..7) {
        for (col in 0..7) {
            val displayRow = if (flipped) 7 - row else row
            val displayCol = if (flipped) 7 - col else col
            val piece = board[displayRow][displayCol] ?: continue
            val cx = col * sqSize + sqSize / 2
            val cy = row * sqSize + sqSize / 2
            val radius = sqSize * 0.38f
            drawPiece(piece, cx, cy, radius)
        }
    }
}

private fun DrawScope.drawPiece(piece: Piece, cx: Float, cy: Float, r: Float) {
    val bg = if (piece.color == PieceColor.WHITE) Color(0xFFFFFEF7) else Color(0xFF1A1A1A)
    val fg = if (piece.color == PieceColor.WHITE) Color(0xFF333333) else Color(0xFFFFFEF7)
    val shadow = Color(0x40000000)

    drawCircle(shadow, r, Offset(cx + r * 0.04f, cy + r * 0.04f))
    drawCircle(bg, r, Offset(cx, cy))

    val symbol = when (piece.type) {
        PieceType.KING -> "\u2654"
        PieceType.QUEEN -> "\u2655"
        PieceType.ROOK -> "\u2656"
        PieceType.BISHOP -> "\u2657"
        PieceType.KNIGHT -> "\u2658"
        PieceType.PAWN -> "\u2659"
    }
    val pt = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = fg.hashCode()
        textSize = r * 1.4f
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawText(symbol, cx, cy + r * 0.45f, pt)
    }
}
