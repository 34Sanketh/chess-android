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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.min

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
    val color = if (piece.color == PieceColor.WHITE) Color(0xFFFFFEF7) else Color(0xFF1A1A1A)
    val outline = if (piece.color == PieceColor.WHITE) Color(0xFF555555) else Color(0xFFCCCCCC)
    val shadow = Color(0x40000000)

    drawCircle(shadow, r, Offset(cx + r * 0.04f, cy + r * 0.04f))
    drawCircle(color, r, Offset(cx, cy))

    when (piece.type) {
        PieceType.PAWN -> drawPawn(cx, cy, r, color, outline)
        PieceType.ROOK -> drawRook(cx, cy, r, color, outline)
        PieceType.KNIGHT -> drawKnight(cx, cy, r, color, outline)
        PieceType.BISHOP -> drawBishop(cx, cy, r, color, outline)
        PieceType.QUEEN -> drawQueen(cx, cy, r, color, outline)
        PieceType.KING -> drawKing(cx, cy, r, color, outline)
    }
}

private fun DrawScope.drawPawn(cx: Float, cy: Float, r: Float, color: Color, outline: Color) {
    val s = r * 0.5f
    val path = Path().apply {
        moveTo(cx - s * 0.4f, cy + s * 0.7f)
        cubicTo(
            cx - s * 0.6f, cy + s * 0.3f,
            cx - s * 0.3f, cy - s * 0.1f,
            cx - s * 0.1f, cy - s * 0.3f,
        )
        cubicTo(
            cx - s * 0.05f, cy - s * 0.45f,
            cx - s * 0.15f, cy - s * 0.6f,
            cx, cy - s * 0.7f,
        )
        cubicTo(
            cx + s * 0.15f, cy - s * 0.6f,
            cx + s * 0.05f, cy - s * 0.45f,
            cx + s * 0.1f, cy - s * 0.3f,
        )
        cubicTo(
            cx + s * 0.3f, cy - s * 0.1f,
            cx + s * 0.6f, cy + s * 0.3f,
            cx + s * 0.4f, cy + s * 0.7f,
        )
        close()
    }
    drawPath(path, color, style = Fill)
    drawPath(path, outline, style = Stroke(width = r * 0.04f))
}

private fun DrawScope.drawRook(cx: Float, cy: Float, r: Float, color: Color, outline: Color) {
    val s = r * 0.5f
    val path = Path().apply {
        moveTo(cx - s * 0.5f, cy + s * 0.6f)
        lineTo(cx - s * 0.5f, cy + s * 0.3f)
        lineTo(cx - s * 0.7f, cy + s * 0.15f)
        lineTo(cx - s * 0.7f, cy - s * 0.15f)
        lineTo(cx - s * 0.5f, cy - s * 0.15f)
        lineTo(cx - s * 0.5f, cy - s * 0.3f)
        lineTo(cx - s * 0.3f, cy - s * 0.5f)
        lineTo(cx + s * 0.3f, cy - s * 0.5f)
        lineTo(cx + s * 0.5f, cy - s * 0.3f)
        lineTo(cx + s * 0.5f, cy - s * 0.15f)
        lineTo(cx + s * 0.7f, cy - s * 0.15f)
        lineTo(cx + s * 0.7f, cy + s * 0.15f)
        lineTo(cx + s * 0.5f, cy + s * 0.3f)
        lineTo(cx + s * 0.5f, cy + s * 0.6f)
        close()
        moveTo(cx - s * 0.35f, cy - s * 0.15f)
        lineTo(cx - s * 0.35f, cy + s * 0.1f)
        moveTo(cx, cy - s * 0.15f)
        lineTo(cx, cy + s * 0.1f)
        moveTo(cx + s * 0.35f, cy - s * 0.15f)
        lineTo(cx + s * 0.35f, cy + s * 0.1f)
    }
    drawPath(path, color, style = Fill)
    drawPath(path, outline, style = Stroke(width = r * 0.04f))
}

private fun DrawScope.drawKnight(cx: Float, cy: Float, r: Float, color: Color, outline: Color) {
    val s = r * 0.5f
    val path = Path().apply {
        moveTo(cx - s * 0.5f, cy + s * 0.6f)
        lineTo(cx + s * 0.3f, cy + s * 0.6f)
        lineTo(cx + s * 0.5f, cy + s * 0.3f)
        lineTo(cx + s * 0.3f, cy + s * 0.1f)
        lineTo(cx + s * 0.3f, cy - s * 0.1f)
        lineTo(cx + s * 0.5f, cy - s * 0.25f)
        lineTo(cx + s * 0.4f, cy - s * 0.5f)
        lineTo(cx + s * 0.1f, cy - s * 0.6f)
        lineTo(cx - s * 0.2f, cy - s * 0.5f)
        lineTo(cx - s * 0.35f, cy - s * 0.25f)
        lineTo(cx - s * 0.4f, cy)
        lineTo(cx - s * 0.6f, cy + s * 0.1f)
        lineTo(cx - s * 0.5f, cy + s * 0.3f)
        close()
    }
    drawPath(path, color, style = Fill)
    drawPath(path, outline, style = Stroke(width = r * 0.04f))

    val eye = Path().apply {
        arcTo(
            cx - s * 0.15f, cy - s * 0.45f,
            s * 0.1f, s * 0.1f,
            0f, 360f, false
        )
    }
    drawPath(eye, outline, style = Fill)
}

private fun DrawScope.drawBishop(cx: Float, cy: Float, r: Float, color: Color, outline: Color) {
    val s = r * 0.5f
    val path = Path().apply {
        moveTo(cx, cy - s * 0.8f)
        cubicTo(
            cx + s * 0.1f, cy - s * 0.6f,
            cx + s * 0.3f, cy - s * 0.3f,
            cx + s * 0.4f, cy - s * 0.1f,
        )
        cubicTo(
            cx + s * 0.5f, cy + s * 0.1f,
            cx + s * 0.5f, cy + s * 0.3f,
            cx + s * 0.4f, cy + s * 0.5f,
        )
        lineTo(cx + s * 0.5f, cy + s * 0.6f)
        lineTo(cx - s * 0.5f, cy + s * 0.6f)
        lineTo(cx - s * 0.4f, cy + s * 0.5f)
        cubicTo(
            cx - s * 0.5f, cy + s * 0.3f,
            cx - s * 0.5f, cy + s * 0.1f,
            cx - s * 0.4f, cy - s * 0.1f,
        )
        cubicTo(
            cx - s * 0.3f, cy - s * 0.3f,
            cx - s * 0.1f, cy - s * 0.6f,
            cx, cy - s * 0.8f,
        )
        close()
        moveTo(cx - s * 0.15f, cy - s * 0.3f)
        lineTo(cx - s * 0.2f, cy - s * 0.1f)
        moveTo(cx + s * 0.15f, cy - s * 0.3f)
        lineTo(cx + s * 0.2f, cy - s * 0.1f)
    }
    drawPath(path, color, style = Fill)
    drawPath(path, outline, style = Stroke(width = r * 0.04f))
}

private fun DrawScope.drawQueen(cx: Float, cy: Float, r: Float, color: Color, outline: Color) {
    val s = r * 0.5f
    val path = Path().apply {
        moveTo(cx - s * 0.5f, cy + s * 0.6f)
        lineTo(cx + s * 0.5f, cy + s * 0.6f)
        lineTo(cx + s * 0.4f, cy + s * 0.3f)
        lineTo(cx + s * 0.6f, cy + s * 0.3f)
        lineTo(cx + s * 0.4f, cy)
        lineTo(cx + s * 0.55f, cy - s * 0.2f)
        lineTo(cx + s * 0.3f, cy - s * 0.1f)
        lineTo(cx + s * 0.2f, cy - s * 0.5f)
        lineTo(cx, cy - s * 0.3f)
        lineTo(cx - s * 0.2f, cy - s * 0.5f)
        lineTo(cx - s * 0.3f, cy - s * 0.1f)
        lineTo(cx - s * 0.55f, cy - s * 0.2f)
        lineTo(cx - s * 0.4f, cy)
        lineTo(cx - s * 0.6f, cy + s * 0.3f)
        lineTo(cx - s * 0.4f, cy + s * 0.3f)
        close()
    }
    drawPath(path, color, style = Fill)
    drawPath(path, outline, style = Stroke(width = r * 0.04f))

    val crown = Path().apply {
        moveTo(cx - s * 0.2f, cy - s * 0.15f)
        lineTo(cx - s * 0.15f, cy - s * 0.35f)
        lineTo(cx, cy - s * 0.2f)
        lineTo(cx + s * 0.15f, cy - s * 0.35f)
        lineTo(cx + s * 0.2f, cy - s * 0.15f)
        close()
    }
    drawPath(crown, color, style = Fill)
    drawPath(crown, outline, style = Stroke(width = r * 0.03f))

    for (dx in listOf(-0.25f, 0f, 0.25f)) {
        drawCircle(outline, r * 0.04f, Offset(cx + dx * s, cy - s * 0.55f))
    }
}

private fun DrawScope.drawKing(cx: Float, cy: Float, r: Float, color: Color, outline: Color) {
    val s = r * 0.5f
    val path = Path().apply {
        moveTo(cx - s * 0.5f, cy + s * 0.6f)
        lineTo(cx + s * 0.5f, cy + s * 0.6f)
        lineTo(cx + s * 0.4f, cy + s * 0.3f)
        lineTo(cx + s * 0.6f, cy + s * 0.3f)
        lineTo(cx + s * 0.4f, cy)
        lineTo(cx + s * 0.55f, cy - s * 0.2f)
        lineTo(cx + s * 0.3f, cy - s * 0.1f)
        lineTo(cx + s * 0.2f, cy - s * 0.5f)
        lineTo(cx, cy - s * 0.3f)
        lineTo(cx - s * 0.2f, cy - s * 0.5f)
        lineTo(cx - s * 0.3f, cy - s * 0.1f)
        lineTo(cx - s * 0.55f, cy - s * 0.2f)
        lineTo(cx - s * 0.4f, cy)
        lineTo(cx - s * 0.6f, cy + s * 0.3f)
        lineTo(cx - s * 0.4f, cy + s * 0.3f)
        close()
    }
    drawPath(path, color, style = Fill)
    drawPath(path, outline, style = Stroke(width = r * 0.04f))

    val cross = Path().apply {
        moveTo(cx, cy - s * 0.7f)
        lineTo(cx, cy - s * 0.2f)
        moveTo(cx - s * 0.25f, cy - s * 0.45f)
        lineTo(cx + s * 0.25f, cy - s * 0.45f)
    }
    drawPath(cross, outline, style = Stroke(width = r * 0.06f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
