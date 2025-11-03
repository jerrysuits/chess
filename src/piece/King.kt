package piece

import main.GamePanel
import main.Type
import kotlin.math.abs

class King(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init {
        type = Type.KING

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/w-king")
        } else {
            image = getImage("/piece/b-king")
        }
    }

    override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (isWithinBoard(targetCol, targetRow)) {
            // MOVEMENT
            if (abs(targetCol - preCol) + abs(targetRow - preRow) == 1 ||
                abs(targetCol - preCol) * abs(targetRow - preRow) == 1
            ) {
                if (isValidSquare(targetCol, targetRow)) {
                    return true
                }
            }

            // CASTLING
            if (!moved) {
                // Right Castling
                if (targetCol == preCol + 2 && targetRow == preRow && !pieceIsOnStraightLine(
                        targetCol,
                        targetRow
                    )
                ) {
                    for (piece in GamePanel.pieces) {
                        if (piece.col == preCol + 3 && piece.row == preRow && !piece.moved) {
                            GamePanel.castlingPiece = piece
                            return true
                        }
                    }
                }
                // Left Castling
                if (targetCol == preCol - 2 && targetRow == preRow && !pieceIsOnStraightLine(
                        targetCol,
                        targetRow
                    )
                ) {
                    val p: Array<Piece?>? = arrayOfNulls<Piece>(2)
                    for (piece in GamePanel.simPieces) {
                        if (piece.col == preCol - 3 && piece.row == targetRow) {
                            p!![0] = piece
                        }
                        if (piece.col == preCol - 4 && piece.row == targetRow) {
                            p!![1] = piece
                        }
                        if (p!![0] == null && p[1] != null && !p[1]!!.moved) {
                            GamePanel.castlingPiece = p[1]
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}
