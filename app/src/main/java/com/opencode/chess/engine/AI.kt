package com.opencode.chess.engine

import com.opencode.chess.engine.Color
import com.opencode.chess.engine.Move
import com.opencode.chess.engine.PieceType
import kotlin.math.max
import kotlin.math.min

class ChessAI(private val engine: ChessEngine) {

    data class SearchResult(val score: Int, val move: Move?)

    private val pawnTable = arrayOf(
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
        intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
        intArrayOf(5, 5, 10, 25, 25, 10, 5, 5),
        intArrayOf(0, 0, 0, 20, 20, 0, 0, 0),
        intArrayOf(5, -5, -10, 0, 0, -10, -5, 5),
        intArrayOf(5, 10, 10, -20, -20, 10, 10, 5),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
    )

    private val knightTable = arrayOf(
        intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50),
        intArrayOf(-40, -20, 0, 0, 0, 0, -20, -40),
        intArrayOf(-30, 0, 10, 15, 15, 10, 0, -30),
        intArrayOf(-30, 5, 15, 20, 20, 15, 5, -30),
        intArrayOf(-30, 0, 15, 20, 20, 15, 0, -30),
        intArrayOf(-30, 5, 10, 15, 15, 10, 5, -30),
        intArrayOf(-40, -20, 0, 5, 5, 0, -20, -40),
        intArrayOf(-50, -40, -30, -30, -30, -30, -40, -50),
    )

    private val bishopTable = arrayOf(
        intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20),
        intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
        intArrayOf(-10, 0, 5, 10, 10, 5, 0, -10),
        intArrayOf(-10, 5, 5, 10, 10, 5, 5, -10),
        intArrayOf(-10, 0, 10, 10, 10, 10, 0, -10),
        intArrayOf(-10, 10, 10, 10, 10, 10, 10, -10),
        intArrayOf(-10, 5, 0, 0, 0, 0, 5, -10),
        intArrayOf(-20, -10, -10, -10, -10, -10, -10, -20),
    )

    private val rookTable = arrayOf(
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(5, 10, 10, 10, 10, 10, 10, 5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(-5, 0, 0, 0, 0, 0, 0, -5),
        intArrayOf(0, 0, 0, 5, 5, 0, 0, 0),
    )

    private val queenTable = arrayOf(
        intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20),
        intArrayOf(-10, 0, 0, 0, 0, 0, 0, -10),
        intArrayOf(-10, 0, 5, 5, 5, 5, 0, -10),
        intArrayOf(-5, 0, 5, 5, 5, 5, 0, -5),
        intArrayOf(0, 0, 5, 5, 5, 5, 0, -5),
        intArrayOf(-10, 5, 5, 5, 5, 5, 0, -10),
        intArrayOf(-10, 0, 5, 0, 0, 0, 0, -10),
        intArrayOf(-20, -10, -10, -5, -5, -10, -10, -20),
    )

    private val kingMidgameTable = arrayOf(
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-30, -40, -40, -50, -50, -40, -40, -30),
        intArrayOf(-20, -30, -30, -40, -40, -30, -30, -20),
        intArrayOf(-10, -20, -20, -20, -20, -20, -20, -10),
        intArrayOf(20, 20, 0, 0, 0, 0, 20, 20),
        intArrayOf(20, 30, 10, 0, 0, 10, 30, 20),
    )

    private val kingEndgameTable = arrayOf(
        intArrayOf(-50, -40, -30, -20, -20, -30, -40, -50),
        intArrayOf(-30, -20, -10, 0, 0, -10, -20, -30),
        intArrayOf(-30, -10, 20, 30, 30, 20, -10, -30),
        intArrayOf(-30, -10, 30, 40, 40, 30, -10, -30),
        intArrayOf(-30, -10, 30, 40, 40, 30, -10, -30),
        intArrayOf(-30, -10, 20, 30, 30, 20, -10, -30),
        intArrayOf(-30, -30, 0, 0, 0, 0, -30, -30),
        intArrayOf(-50, -30, -30, -30, -30, -30, -30, -50),
    )

    fun findBestMove(depth: Int = 4): Move? {
        val color = engine.currentPlayer
        val result = minimax(depth, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, color)
        return result.move
    }

    private fun minimax(depth: Int, alpha: Int, beta: Int, color: Color): SearchResult {
        if (depth == 0 || engine.gameOver) {
            return SearchResult(evaluate(color), null)
        }

        val moves = orderMoves(engine.generateMoves())
        if (moves.isEmpty()) {
            return SearchResult(evaluate(color), null)
        }

        var bestMove: Move? = null

        if (color == Color.WHITE) {
            var maxEval = Int.MIN_VALUE + 1
            var a = alpha
            for (move in moves) {
                val snapshot = engine.saveState()
                engine.applyMoveInternal(move)
                val savedPlayer = engine.currentPlayer
                engine.currentPlayer = engine.currentPlayer.opponent
                val savedEp = engine.enPassantSquare
                engine.enPassantSquare = null

                val result = minimax(depth - 1, a, beta, Color.BLACK)

                engine.restoreState(snapshot)
                engine.currentPlayer = savedPlayer
                engine.enPassantSquare = savedEp

                if (result.score > maxEval) {
                    maxEval = result.score
                    bestMove = move
                }
                a = max(a, maxEval)
                if (beta <= a) break
            }
            return SearchResult(maxEval, bestMove)
        } else {
            var minEval = Int.MAX_VALUE - 1
            var b = beta
            for (move in moves) {
                val snapshot = engine.saveState()
                engine.applyMoveInternal(move)
                val savedPlayer = engine.currentPlayer
                engine.currentPlayer = engine.currentPlayer.opponent
                val savedEp = engine.enPassantSquare
                engine.enPassantSquare = null

                val result = minimax(depth - 1, alpha, b, Color.WHITE)

                engine.restoreState(snapshot)
                engine.currentPlayer = savedPlayer
                engine.enPassantSquare = savedEp

                if (result.score < minEval) {
                    minEval = result.score
                    bestMove = move
                }
                b = min(b, minEval)
                if (b <= alpha) break
            }
            return SearchResult(minEval, bestMove)
        }
    }

    private fun orderMoves(moves: List<Move>): List<Move> {
        return moves.sortedByDescending { move ->
            var score = 0
            move.captured?.let { score += it.type.value * 10 }
            if (move.promotion != null) score += move.promotion.value * 5
            if (move.isCastling) score += 50
            if (move.piece.type == PieceType.PAWN && move.toRow == 0 || move.toRow == 7) score += 100
            score
        }
    }

    fun evaluate(color: Color): Int {
        var score = 0
        var whiteMaterial = 0
        var blackMaterial = 0
        var pieceCount = 0

        for (r in 0..7) {
            for (c in 0..7) {
                val piece = engine.board[r][c] ?: continue
                pieceCount++

                val pieceScore = piece.type.value
                val posScore = when (piece.type) {
                    PieceType.PAWN -> pawnTable[r][c] * 10
                    PieceType.KNIGHT -> knightTable[r][c]
                    PieceType.BISHOP -> bishopTable[r][c]
                    PieceType.ROOK -> rookTable[r][c]
                    PieceType.QUEEN -> queenTable[r][c]
                    PieceType.KING -> if (pieceCount < 10) kingEndgameTable[r][c] else kingMidgameTable[r][c]
                }

                val total = pieceScore + posScore
                if (piece.color == Color.WHITE) {
                    score += total
                    whiteMaterial += pieceScore
                } else {
                    score -= total
                    blackMaterial += pieceScore
                }
            }
        }

        val isEndgame = whiteMaterial < 1500 || blackMaterial < 1500
        if (!isEndgame) {
            score += kingSafety(Color.WHITE) * 15
            score -= kingSafety(Color.BLACK) * 15
        }

        score += mobilityBonus(Color.WHITE) * 5
        score -= mobilityBonus(Color.BLACK) * 5

        if (color == Color.WHITE) return score
        return -score
    }

    private fun kingSafety(color: Color): Int {
        val kingPos = findKing(color) ?: return 0
        val (kr, kc) = kingPos
        var safety = 0

        for (dr in -1..1) {
            for (dc in -1..1) {
                val r = kr + dr
                val c = kc + dc
                if (r in 0..7 && c in 0..7) {
                    val p = engine.board[r][c]
                    if (p != null && p.color == color && p.type == PieceType.PAWN) {
                        safety += 10
                    }
                }
            }
        }

        if (color == Color.WHITE && kr == 7) {
            if (kc in 2..6) safety += 15
        } else if (color == Color.BLACK && kr == 0) {
            if (kc in 2..6) safety += 15
        }

        return safety
    }

    private fun mobilityBonus(color: Color): Int {
        val savedPlayer = engine.currentPlayer
        engine.currentPlayer = color
        val moves = engine.generateMoves().size
        engine.currentPlayer = savedPlayer
        return moves
    }

    private fun findKing(color: Color): Pair<Int, Int>? {
        for (r in 0..7) for (c in 0..7) {
            val p = engine.board[r][c]
            if (p != null && p.color == color && p.type == PieceType.KING) return r to c
        }
        return null
    }
}
