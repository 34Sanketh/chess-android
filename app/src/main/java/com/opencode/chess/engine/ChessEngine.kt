package com.opencode.chess.engine

import com.opencode.chess.engine.Color
import com.opencode.chess.engine.Piece
import com.opencode.chess.engine.PieceType
import kotlin.math.abs

class ChessEngine {
    var board: Array<Array<Piece?>> = Array(8) { Array(8) { null } }
    var currentPlayer: Color = Color.WHITE
    var moveLog = mutableListOf<Move>()
    var halfMoveClock = 0
    var fullMoveNumber = 1

    var castlingRights = CastlingRights()
    var enPassantSquare: Int? = null
    var capturedPieces = mutableListOf<Piece>()
    var gameOver = false
    var gameResult: GameResult? = null

    data class CastlingRights(
        var whiteKingSide: Boolean = true,
        var whiteQueenSide: Boolean = true,
        var blackKingSide: Boolean = true,
        var blackQueenSide: Boolean = true,
    ) {
        fun clone() = CastlingRights(whiteKingSide, whiteQueenSide, blackKingSide, blackQueenSide)
    }

    sealed class GameResult {
        data class Win(val winner: Color) : GameResult()
        data class Draw(val reason: String) : GameResult()
    }

    init {
        setupStartingPosition()
    }

    fun setupStartingPosition() {
        board = Array(8) { Array(8) { null } }
        currentPlayer = Color.WHITE
        moveLog.clear()
        castlingRights = CastlingRights(true, true, true, true)
        enPassantSquare = null
        capturedPieces.clear()
        gameOver = false
        gameResult = null
        halfMoveClock = 0
        fullMoveNumber = 1

        val pieceOrder = arrayOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
            PieceType.QUEEN, PieceType.KING,
            PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )

        for (col in 0..7) {
            board[0][col] = Piece(pieceOrder[col], Color.BLACK)
            board[1][col] = Piece(PieceType.PAWN, Color.BLACK)
            board[6][col] = Piece(PieceType.PAWN, Color.WHITE)
            board[7][col] = Piece(pieceOrder[col], Color.WHITE)
        }
    }

    fun loadFen(fen: String) {
        val parts = fen.split(" ")
        val rows = parts[0].split("/")
        board = Array(8) { Array(8) { null } }

        for (r in 0..7) {
            var c = 0
            for (ch in rows[r]) {
                if (ch.isDigit()) {
                    c += ch.digitToInt()
                } else {
                    val color = if (ch.isUpperCase()) Color.WHITE else Color.BLACK
                    val type = when (ch.uppercaseChar()) {
                        'P' -> PieceType.PAWN
                        'N' -> PieceType.KNIGHT
                        'B' -> PieceType.BISHOP
                        'R' -> PieceType.ROOK
                        'Q' -> PieceType.QUEEN
                        'K' -> PieceType.KING
                        else -> throw IllegalArgumentException("Unknown piece: $ch")
                    }
                    board[r][c] = Piece(type, color)
                    c++
                }
            }
        }

        currentPlayer = if (parts[1] == "w") Color.WHITE else Color.BLACK
        castlingRights = CastlingRights(
            parts[2].contains('K'),
            parts[2].contains('Q'),
            parts[2].contains('k'),
            parts[2].contains('q'),
        )
        enPassantSquare = if (parts[3] != "-") {
            val col = parts[3][0] - 'a'
            val row = 8 - (parts[3][1] - '0')
            row * 8 + col
        } else null

        halfMoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0
        fullMoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1
        moveLog.clear()
        capturedPieces.clear()
        gameOver = false
        gameResult = null
    }

    fun generateFen(): String {
        val sb = StringBuilder()
        for (r in 0..7) {
            var empty = 0
            for (c in 0..7) {
                val p = board[r][c]
                if (p == null) {
                    empty++
                } else {
                    if (empty > 0) { sb.append(empty); empty = 0 }
                    sb.append(p.symbol)
                }
            }
            if (empty > 0) sb.append(empty)
            if (r < 7) sb.append('/')
        }
        sb.append(' ')
        sb.append(if (currentPlayer == Color.WHITE) 'w' else 'b')
        sb.append(' ')
        val cr = StringBuilder()
        if (castlingRights.whiteKingSide) cr.append('K')
        if (castlingRights.whiteQueenSide) cr.append('Q')
        if (castlingRights.blackKingSide) cr.append('k')
        if (castlingRights.blackQueenSide) cr.append('q')
        sb.append(if (cr.isEmpty()) "-" else cr.toString())
        sb.append(' ')
        if (enPassantSquare != null) {
            val col = enPassantSquare!! % 8
            val row = 8 - (enPassantSquare!! / 8)
            sb.append('a' + col)
            sb.append(row)
        } else {
            sb.append('-')
        }
        sb.append(' ')
        sb.append(halfMoveClock)
        sb.append(' ')
        sb.append(fullMoveNumber)
        return sb.toString()
    }

    fun getPieceAt(row: Int, col: Int): Piece? {
        if (row in 0..7 && col in 0..7) return board[row][col]
        return null
    }

    fun indexToRowCol(index: Int): Pair<Int, Int> = Pair(7 - (index / 8), index % 8)
    fun rowColToIndex(row: Int, col: Int): Int = (7 - row) * 8 + col

    fun generateMoves(): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c] ?: continue
                if (piece.color != currentPlayer) continue
                moves.addAll(generatePieceMoves(r, c, piece))
            }
        }
        return moves.filter { isMoveLegal(it) }
    }

    private fun generatePieceMoves(row: Int, col: Int, piece: Piece): List<Move> {
        return when (piece.type) {
            PieceType.PAWN -> generatePawnMoves(row, col, piece.color)
            PieceType.KNIGHT -> generateKnightMoves(row, col, piece.color)
            PieceType.BISHOP -> generateSlidingMoves(row, col, piece.color, listOf(
                -1 to -1, -1 to 1, 1 to -1, 1 to 1
            ))
            PieceType.ROOK -> generateSlidingMoves(row, col, piece.color, listOf(
                -1 to 0, 1 to 0, 0 to -1, 0 to 1
            ))
            PieceType.QUEEN -> generateSlidingMoves(row, col, piece.color, listOf(
                -1 to -1, -1 to 1, 1 to -1, 1 to 1,
                -1 to 0, 1 to 0, 0 to -1, 0 to 1
            ))
            PieceType.KING -> generateKingMoves(row, col, piece.color)
        }
    }

    private fun generatePawnMoves(row: Int, col: Int, color: Color): List<Move> {
        val moves = mutableListOf<Move>()
        val dir = if (color == Color.WHITE) -1 else 1
        val startRow = if (color == Color.WHITE) 6 else 1
        val promoRow = if (color == Color.WHITE) 0 else 7
        val from = rowColToIndex(row, col)

        val forward = row + dir
        if (forward in 0..7 && board[forward][col] == null) {
            if (forward == promoRow) {
                for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                    moves.add(Move(from, rowColToIndex(forward, col), Piece(PieceType.PAWN, color), promotion = promo))
                }
            } else {
                moves.add(Move(from, rowColToIndex(forward, col), Piece(PieceType.PAWN, color)))
            }

            if (row == startRow) {
                val twoForward = row + 2 * dir
                if (board[twoForward][col] == null) {
                    moves.add(Move(from, rowColToIndex(twoForward, col), Piece(PieceType.PAWN, color)))
                }
            }
        }

        for (dc in listOf(-1, 1)) {
            val nc = col + dc
            if (nc !in 0..7) continue
            val captured = board[forward][nc]
            if (captured != null && captured.color != color) {
                if (forward == promoRow) {
                    for (promo in listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
                        moves.add(Move(from, rowColToIndex(forward, nc), Piece(PieceType.PAWN, color), captured = captured, promotion = promo))
                    }
                } else {
                    moves.add(Move(from, rowColToIndex(forward, nc), Piece(PieceType.PAWN, color), captured = captured))
                }
            }

            if (enPassantSquare != null) {
                val epRow = if (color == Color.WHITE) 3 else 4
                val epCol = enPassantSquare!! % 8
                if (forward == epRow && nc == epCol) {
                    val capturedPawn = board[row][nc]
                    if (capturedPawn != null && capturedPawn.type == PieceType.PAWN) {
                        moves.add(Move(from, enPassantSquare!!, Piece(PieceType.PAWN, color), captured = capturedPawn, isEnPassant = true))
                    }
                }
            }
        }
        return moves
    }

    private fun generateKnightMoves(row: Int, col: Int, color: Color): List<Move> {
        val moves = mutableListOf<Move>()
        val from = rowColToIndex(row, col)
        val offsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in offsets) {
            val nr = row + dr
            val nc = col + dc
            if (nr !in 0..7 || nc !in 0..7) continue
            val target = board[nr][nc]
            if (target != null && target.color == color) continue
            moves.add(Move(from, rowColToIndex(nr, nc), Piece(PieceType.KNIGHT, color), captured = target))
        }
        return moves
    }

    private fun generateSlidingMoves(row: Int, col: Int, color: Color, directions: List<Pair<Int, Int>>): List<Move> {
        val moves = mutableListOf<Move>()
        val from = rowColToIndex(row, col)
        for ((dr, dc) in directions) {
            var r = row + dr
            var c = col + dc
            while (r in 0..7 && c in 0..7) {
                val target = board[r][c]
                if (target != null) {
                    if (target.color != color) {
                        moves.add(Move(from, rowColToIndex(r, c), board[row][col]!!, captured = target))
                    }
                    break
                }
                moves.add(Move(from, rowColToIndex(r, c), board[row][col]!!))
                r += dr
                c += dc
            }
        }
        return moves
    }

    private fun generateKingMoves(row: Int, col: Int, color: Color): List<Move> {
        val moves = mutableListOf<Move>()
        val from = rowColToIndex(row, col)
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = row + dr
                val nc = col + dc
                if (nr !in 0..7 || nc !in 0..7) continue
                val target = board[nr][nc]
                if (target != null && target.color == color) continue
                moves.add(Move(from, rowColToIndex(nr, nc), Piece(PieceType.KING, color), captured = target))
            }
        }

        if (color == Color.WHITE) {
            if (castlingRights.whiteKingSide && canCastle(row, col, Color.WHITE, kingSide = true)) {
                moves.add(Move(from, rowColToIndex(row, 6), Piece(PieceType.KING, Color.WHITE), isCastling = true))
            }
            if (castlingRights.whiteQueenSide && canCastle(row, col, Color.WHITE, kingSide = false)) {
                moves.add(Move(from, rowColToIndex(row, 2), Piece(PieceType.KING, Color.WHITE), isCastling = true))
            }
        } else {
            if (castlingRights.blackKingSide && canCastle(row, col, Color.BLACK, kingSide = true)) {
                moves.add(Move(from, rowColToIndex(row, 6), Piece(PieceType.KING, Color.BLACK), isCastling = true))
            }
            if (castlingRights.blackQueenSide && canCastle(row, col, Color.BLACK, kingSide = false)) {
                moves.add(Move(from, rowColToIndex(row, 2), Piece(PieceType.KING, Color.BLACK), isCastling = true))
            }
        }
        return moves
    }

    private fun canCastle(row: Int, col: Int, color: Color, kingSide: Boolean): Boolean {
        if (isInCheck(color)) return false
        val rank = if (color == Color.WHITE) 7 else 0
        if (row != rank || col != 4) return false

        val rookCol = if (kingSide) 7 else 0
        val rook = board[rank][rookCol] ?: return false
        if (rook.type != PieceType.ROOK) return false

        val clearCols = if (kingSide) (5..6) else (1..3)
        for (c in clearCols) {
            if (board[rank][c] != null) return false
        }

        val checkCols = if (kingSide) listOf(5, 6) else listOf(2, 3)
        for (c in checkCols) {
            val kingAt = rowColToIndex(rank, c)
            if (isSquareAttacked(kingAt, color)) return false
        }
        return true
    }

    fun isInCheck(color: Color): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c] ?: continue
                if (p.color == color && p.type == PieceType.KING) {
                    return isSquareAttacked(rowColToIndex(r, c), color)
                }
            }
        }
        return false
    }

    fun isSquareAttacked(index: Int, defenderColor: Color): Boolean {
        val (row, col) = indexToRowCol(index)
        val attackerColor = defenderColor.opponent

        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                if (abs(dr) + abs(dc) == 1) {
                    val nr = row + dr
                    val nc = col + dc
                    if (nr in 0..7 && nc in 0..7) {
                        val p = board[nr][nc]
                        if (p != null && p.color == attackerColor && p.type == PieceType.KING) return true
                    }
                }
            }
        }

        val knightOffsets = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        for ((dr, dc) in knightOffsets) {
            val nr = row + dr
            val nc = col + dc
            if (nr in 0..7 && nc in 0..7) {
                val p = board[nr][nc]
                if (p != null && p.color == attackerColor && p.type == PieceType.KNIGHT) return true
            }
        }

        val pawnDir = if (attackerColor == Color.WHITE) -1 else 1
        for (dc in listOf(-1, 1)) {
            val nr = row + pawnDir
            val nc = col + dc
            if (nr in 0..7 && nc in 0..7) {
                val p = board[nr][nc]
                if (p != null && p.color == attackerColor && p.type == PieceType.PAWN) return true
            }
        }

        val diagDirs = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        for ((dr, dc) in diagDirs) {
            var r = row + dr
            var c = col + dc
            while (r in 0..7 && c in 0..7) {
                val p = board[r][c]
                if (p != null) {
                    if (p.color == attackerColor && (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)) return true
                    break
                }
                r += dr
                c += dc
            }
        }

        val straightDirs = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for ((dr, dc) in straightDirs) {
            var r = row + dr
            var c = col + dc
            while (r in 0..7 && c in 0..7) {
                val p = board[r][c]
                if (p != null) {
                    if (p.color == attackerColor && (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)) return true
                    break
                }
                r += dr
                c += dc
            }
        }

        return false
    }

    private fun isMoveLegal(move: Move): Boolean {
        val snapshot = saveState()
        applyMoveInternal(move)
        val inCheck = isInCheck(move.piece.color)
        restoreState(snapshot)
        return !inCheck
    }

    fun applyMove(move: Move): Boolean {
        val legal = generateMoves().any { it.from == move.from && it.to == move.to
            && it.promotion == move.promotion && it.isCastling == move.isCastling }
        if (!legal) return false

        val captured = move.captured
        if (captured != null) capturedPieces.add(captured)

        applyMoveInternal(move)
        moveLog.add(move)

        if (move.piece.type == PieceType.PAWN || move.captured != null) {
            halfMoveClock = 0
        } else {
            halfMoveClock++
        }

        if (currentPlayer == Color.BLACK) fullMoveNumber++

        if (move.isEnPassant) {
            val epRow = if (currentPlayer == Color.WHITE) 3 else 4
            val col = move.toCol
            board[epRow][col] = null
        }

        if (move.isCastling) {
            val rank = move.toRow
            if (move.toCol == 6) { // king side
                board[rank][7] = null
                board[rank][5] = Piece(PieceType.ROOK, move.piece.color)
            } else { // queen side
                board[rank][0] = null
                board[rank][3] = Piece(PieceType.ROOK, move.piece.color)
            }
        }

        currentPlayer = currentPlayer.opponent
        enPassantSquare = null

        if (move.piece.type == PieceType.PAWN && abs(move.toRow - move.fromRow) == 2) {
            val midRow = (move.toRow + move.fromRow) / 2
            enPassantSquare = rowColToIndex(midRow, move.toCol)
        }

        checkGameState()
        return true
    }

    internal fun applyMoveInternal(move: Move) {
        val fromRow = move.fromRow
        val fromCol = move.fromCol
        val toRow = move.toRow
        val toCol = move.toCol
        board[toRow][toCol] = board[fromRow][fromCol]
        board[fromRow][fromCol] = null

        if (move.promotion != null && board[toRow][toCol]?.type == PieceType.PAWN) {
            board[toRow][toCol] = Piece(move.promotion, move.piece.color)
        }
    }

    private fun checkGameState() {
        val moves = generateMoves()
        if (moves.isEmpty()) {
            gameOver = true
            gameResult = if (isInCheck(currentPlayer)) {
                GameResult.Win(currentPlayer.opponent)
            } else {
                GameResult.Draw("Stalemate")
            }
            return
        }

        if (insufficientMaterial()) {
            gameOver = true
            gameResult = GameResult.Draw("Insufficient material")
            return
        }

        if (halfMoveClock >= 100) {
            gameOver = true
            gameResult = GameResult.Draw("50-move rule")
            return
        }

        if (threefoldRepetition()) {
            gameOver = true
            gameResult = GameResult.Draw("Threefold repetition")
            return
        }
    }

    private fun insufficientMaterial(): Boolean {
        val pieces = mutableListOf<Piece>()
        var kings = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c] ?: continue
                if (p.type == PieceType.KING) kings++
                else pieces.add(p)
            }
        }

        if (kings == 2 && pieces.isEmpty()) return true
        if (pieces.size == 1 && pieces[0].type == PieceType.KNIGHT) return true
        if (pieces.size == 1 && pieces[0].type == PieceType.BISHOP) return true
        if (pieces.size == 2 && pieces.all { it.type == PieceType.BISHOP }
            && pieces[0].color != pieces[1].color) {
            val sq1 = findPiece(pieces[0])
            val sq2 = findPiece(pieces[1])
            if (sq1 != null && sq2 != null) {
                if ((sq1.first + sq1.second) % 2 == (sq2.first + sq2.second) % 2) return true
            }
        }
        return false
    }

    private fun findPiece(piece: Piece): Pair<Int, Int>? {
        for (r in 0..7) for (c in 0..7) {
            if (board[r][c] == piece) return r to c
        }
        return null
    }

    private fun threefoldRepetition(): Boolean {
        val fenHistory = moveLog.map { generateFen() }
        val currentFen = generateFen()
        return fenHistory.count { it == currentFen } >= 2
    }

    internal data class BoardState(
        val board: Array<Array<Piece?>>,
        val castlingRights: CastlingRights,
        val enPassantSquare: Int?,
        val halfMoveClock: Int,
    )

    internal fun saveState(): BoardState {
        return BoardState(
            Array(8) { r -> Array(8) { c -> board[r][c] } },
            castlingRights.clone(),
            enPassantSquare,
            halfMoveClock
        )
    }

    internal fun restoreState(state: BoardState) {
        board = state.board
        castlingRights = state.castlingRights
        enPassantSquare = state.enPassantSquare
        halfMoveClock = state.halfMoveClock
    }

    fun isCheckmate(): Boolean = gameOver && gameResult is GameResult.Win
    fun isDraw(): Boolean = gameOver && gameResult is GameResult.Draw
    fun getWinner(): Color? = (gameResult as? GameResult.Win)?.winner
}
