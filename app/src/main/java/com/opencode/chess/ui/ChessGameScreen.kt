package com.opencode.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opencode.chess.engine.Color as PieceColor
import com.opencode.chess.engine.Piece
import com.opencode.chess.engine.PieceType
import com.opencode.chess.model.GameState
import com.opencode.chess.ui.theme.AccentGold
import com.opencode.chess.ui.theme.AccentGoldDark
import com.opencode.chess.ui.theme.DarkBackground
import com.opencode.chess.ui.theme.SurfaceColor

@Composable
fun ChessGameScreen(state: GameState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DarkBackground, Color(0xFF0F0F23), DarkBackground)
                )
            )
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "CHESS",
                color = AccentGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )
            Text(
                text = "MASTER",
                color = Color(0xFFE0D5C1),
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 6.sp,
            )
            PlayerBadge(
                label = if (state.playerColor == PieceColor.WHITE) "YOU" else "COM",
                isWhite = state.playerColor == PieceColor.WHITE,
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerLabel("White", state.currentPlayer == PieceColor.WHITE)
            PlayerLabel("Black", state.currentPlayer == PieceColor.BLACK)
        }

        Spacer(Modifier.height(4.dp))

        CapturedPiecesRow(pieces = state.capturedBlack, flipped = true)
        Spacer(Modifier.height(2.dp))

        ChessBoard(
            board = state.engine.board,
            selectedSquare = state.selectedSquare,
            legalMoves = state.legalMovesForSelected,
            lastMove = state.lastMove,
            checkSquares = state.checkSquares,
            playerColor = state.playerColor,
            flipped = state.playerColor == PieceColor.BLACK,
            onSquareClick = { state.onSquareClick(it) },
            animatedFrom = state.animatedFrom,
            animatedTo = state.animatedTo,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.height(2.dp))
        CapturedPiecesRow(pieces = state.capturedWhite, flipped = false)
        Spacer(Modifier.height(4.dp))

        StatusBar(
            message = state.gameMessage,
            isThinking = state.isAiThinking,
            currentPlayer = state.currentPlayer,
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { state.resetGame() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    contentColor = Color(0xFF1A1A2E),
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text("New Game", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Button(
                onClick = { state.toggleFlip() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceColor,
                    contentColor = Color(0xFFE0D5C1),
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text("Flip Board", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PlayerBadge(label: String, isWhite: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    listOf(SurfaceColor, SurfaceColor.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = Color(0xFFE0D5C1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PlayerLabel(name: String, isActive: Boolean) {
    Text(
        text = name,
        color = if (isActive) AccentGold else Color(0xFF555577),
        fontSize = 12.sp,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
    )
}

@Composable
private fun CapturedPiecesRow(pieces: List<Piece>, flipped: Boolean) {
    if (pieces.isEmpty()) {
        Spacer(Modifier.height(6.dp))
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = if (flipped) Arrangement.End else Arrangement.Start,
    ) {
        for (piece in pieces.takeLast(8)) {
            val symbol = if (piece.color == PieceColor.WHITE) {
                when (piece.type) {
                    PieceType.PAWN -> "\u2659"
                    PieceType.KNIGHT -> "\u2658"
                    PieceType.BISHOP -> "\u2657"
                    PieceType.ROOK -> "\u2656"
                    PieceType.QUEEN -> "\u2655"
                    PieceType.KING -> "\u2654"
                }
            } else {
                when (piece.type) {
                    PieceType.PAWN -> "\u265F"
                    PieceType.KNIGHT -> "\u265E"
                    PieceType.BISHOP -> "\u265D"
                    PieceType.ROOK -> "\u265C"
                    PieceType.QUEEN -> "\u265B"
                    PieceType.KING -> "\u265A"
                }
            }
            Text(text = symbol, fontSize = 14.sp, color = Color(0xFFE0D5C1))
        }
    }
}

@Composable
private fun StatusBar(
    message: String,
    isThinking: Boolean,
    currentPlayer: PieceColor,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x22000000)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = isThinking, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AccentGold,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }

            Text(
                text = when {
                    message.isNotEmpty() -> message
                    isThinking -> "Computer thinking..."
                    else -> "${if (currentPlayer == PieceColor.WHITE) "White" else "Black"}'s turn"
                },
                color = if (message.isNotEmpty()) AccentGold else Color(0xFFE0D5C1),
                fontSize = 13.sp,
                fontWeight = if (message.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}
