package com.opencode.chess.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.opencode.chess.engine.ChessAI
import com.opencode.chess.engine.ChessEngine
import com.opencode.chess.engine.Color
import com.opencode.chess.engine.Move
import com.opencode.chess.engine.Piece
import com.opencode.chess.engine.PieceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameState : ViewModel() {
    val engine = ChessEngine()
    private val ai = ChessAI(engine)

    var selectedSquare by mutableStateOf<Int?>(null)
    var legalMovesForSelected = mutableListOf<Int>()
    var lastMove by mutableStateOf<Move?>(null)
    var checkSquares = mutableListOf<Int>()
    var capturedWhite = mutableListOf<Piece>()
    var capturedBlack = mutableListOf<Piece>()
    var currentPlayer by mutableStateOf(Color.WHITE)
    var gameOver by mutableStateOf(false)
    var gameMessage by mutableStateOf("")
    var isAiThinking by mutableStateOf(false)
    var playerColor by mutableStateOf(Color.WHITE)
    var moveHistory by mutableStateOf(listOf<Move>())
    var selectedHistoryIndex by mutableIntStateOf(-1)

    private val scope = CoroutineScope(Dispatchers.Default)

    fun onSquareClick(index: Int) {
        if (gameOver || isAiThinking || currentPlayer != playerColor) return

        if (selectedSquare == null) {
            val piece = engine.getPieceAt(7 - (index / 8), index % 8)
            if (piece == null || piece.color != playerColor) return
            selectedSquare = index
            legalMovesForSelected = engine.generateMoves()
                .filter { it.from == index }
                .map { it.to }
                .toMutableList()
        } else {
            if (legalMovesForSelected.contains(index)) {
                val move = engine.generateMoves()
                    .firstOrNull { it.from == selectedSquare && it.to == index }
                if (move != null) {
                    executeMove(move)
                }
            } else {
                val piece = engine.getPieceAt(7 - (index / 8), index % 8)
                if (piece != null && piece.color == playerColor) {
                    selectedSquare = index
                    legalMovesForSelected = engine.generateMoves()
                        .filter { it.from == index }
                        .map { it.to }
                        .toMutableList()
                } else {
                    selectedSquare = null
                    legalMovesForSelected.clear()
                }
            }
            if (legalMovesForSelected.isEmpty()) {
                selectedSquare = null
            }
        }
    }

    private fun executeMove(move: Move) {
        if (engine.applyMove(move)) {
            lastMove = move
            updateGameState()
            selectedSquare = null
            legalMovesForSelected.clear()

            if (!gameOver && currentPlayer != playerColor) {
                doAiMove()
            }
        }
    }

    fun doAiMove() {
        if (gameOver || isAiThinking) return
        isAiThinking = true
        scope.launch {
            val bestMove = ai.findBestMove(4)
            if (bestMove != null) {
                engine.applyMove(bestMove)
                lastMove = bestMove
                updateGameState()
            }
            isAiThinking = false
        }
    }

    private fun updateGameState() {
        moveHistory = engine.moveLog.toList()
        currentPlayer = engine.currentPlayer
        capturedWhite.clear()
        capturedBlack.clear()
        for (p in engine.capturedPieces) {
            if (p.color == Color.WHITE) capturedBlack.add(p)
            else capturedWhite.add(p)
        }

        checkSquares.clear()
        if (engine.isInCheck(engine.currentPlayer)) {
            for (r in 0..7) for (c in 0..7) {
                val p = engine.board[r][c]
                if (p != null && p.color == engine.currentPlayer && p.type == PieceType.KING) {
                    checkSquares.add(r * 8 + c)
                }
            }
        }

        gameOver = engine.gameOver
        gameMessage = when (val result = engine.gameResult) {
            is ChessEngine.GameResult.Win -> {
                if (result.winner == Color.WHITE) "Checkmate! White wins!"
                else "Checkmate! Black wins!"
            }
            is ChessEngine.GameResult.Draw -> "Draw: ${result.reason}"
            null -> ""
        }
    }

    fun resetGame() {
        engine.setupStartingPosition()
        selectedSquare = null
        legalMovesForSelected.clear()
        lastMove = null
        checkSquares.clear()
        capturedWhite.clear()
        capturedBlack.clear()
        currentPlayer = engine.currentPlayer
        gameOver = false
        gameMessage = ""
        isAiThinking = false
        moveHistory = emptyList()
        selectedHistoryIndex = -1
    }

    fun getPieceAtSquare(index: Int): Piece? {
        val row = 7 - (index / 8)
        val col = index % 8
        return engine.board[row][col]
    }

    fun toggleFlop() {
        playerColor = playerColor.opponent
        selectedSquare = null
        legalMovesForSelected.clear()
    }
}
