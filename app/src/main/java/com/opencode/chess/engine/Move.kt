package com.opencode.chess.engine

data class Move(
    val from: Int,
    val to: Int,
    val piece: Piece,
    val captured: Piece? = null,
    val promotion: PieceType? = null,
    val isEnPassant: Boolean = false,
    val isCastling: Boolean = false,
) {
    val fromRow: Int get() = 7 - (from / 8)
    val fromCol: Int get() = from % 8
    val toRow: Int get() = 7 - (to / 8)
    val toCol: Int get() = to % 8

    val uci: String get() {
        val promo = promotion?.let { it.symbol.lowercaseChar() } ?: ""
        return "${'a' + fromCol}${fromRow + 1}${'a' + toCol}${toRow + 1}$promo"
    }

    override fun toString(): String {
        val promo = promotion?.let { "=${it.symbol}" } ?: ""
        val cap = if (captured != null) "x" else "-"
        return "${piece.symbol}$cap${'a' + toCol}${toRow + 1}$promo"
    }
}
