package com.opencode.chess.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
import com.opencode.chess.ui.theme.AccentColor
import com.opencode.chess.ui.theme.DarkBackground

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
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "CHESS MASTER",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )

        Text(
            text = if (state.playerColor == PieceColor.WHITE) "You: White" else "You: Black",
            color = Color(0xFF8888AA),
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(8.dp))

        CapturedPiecesRow(
            pieces = state.capturedBlack,
            total = state.capturedBlack.size + state.capturedWhite.size,
        )

        Spacer(Modifier.height(4.dp))

        ChessBoard(
            board = state.engine.board,
            selectedSquare = state.selectedSquare,
            legalMoves = state.legalMovesForSelected,
            lastMove = state.lastMove,
            checkSquares = state.checkSquares,
            playerColor = state.playerColor,
            flipped = state.playerColor == PieceColor.BLACK,
            onSquareClick = { state.onSquareClick(it) },
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.height(4.dp))

        CapturedPiecesRow(
            pieces = state.capturedWhite,
            total = state.capturedWhite.size + state.capturedBlack.size,
        )

        Spacer(Modifier.height(8.dp))

        StatusBar(
            message = state.gameMessage,
            isThinking = state.isAiThinking,
            currentPlayer = state.currentPlayer,
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = { state.resetGame() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(end = 6.dp),
            ) {
                Text("New Game", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Button(
                onClick = { state.toggleFlop() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A4A),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).padding(start = 6.dp),
            ) {
                Text("Switch Sides", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CapturedPiecesRow(pieces: List<Piece>, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        for (piece in pieces) {
            val symbol = when (piece.type) {
                PieceType.PAWN -> "\u2659"
                PieceType.KNIGHT -> "\u2658"
                PieceType.BISHOP -> "\u2657"
                PieceType.ROOK -> "\u2656"
                PieceType.QUEEN -> "\u2655"
                PieceType.KING -> "\u2654"
            }
            Text(
                text = symbol,
                fontSize = 18.sp,
                color = if (piece.color == PieceColor.WHITE) Color(0xFFFFFEF7) else Color(0xFF1A1A1A),
            )
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
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = isThinking, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AccentColor,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }

            Text(
                text = if (message.isNotEmpty()) message
                       else if (isThinking) "Computer is thinking..."
                       else "${if (currentPlayer == PieceColor.WHITE) "White" else "Black"}'s turn",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (message.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}
