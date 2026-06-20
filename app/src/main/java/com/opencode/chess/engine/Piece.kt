package com.opencode.chess.engine

enum class PieceType(val symbol: Char) {
    PAWN('P'), KNIGHT('N'), BISHOP('B'),
    ROOK('R'), QUEEN('Q'), KING('K');

    val value: Int get() = when (this) {
        PAWN -> 100
        KNIGHT -> 320
        BISHOP -> 330
        ROOK -> 500
        QUEEN -> 900
        KING -> 20000
    }
}

enum class Color(val sign: Int) {
    WHITE(1), BLACK(-1);

    val opponent: Color get() = if (this == WHITE) BLACK else WHITE
}

data class Piece(val type: PieceType, val color: Color) {
    val symbol: Char get() = if (color == Color.WHITE) type.symbol else type.symbol.lowercaseChar()
}
