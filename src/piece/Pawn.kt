package piece

import main.GamePanel
import main.Type
import kotlin.math.abs

class Pawn(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init {
        type = Type.PAWN

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/w-pawn")
        } else {
            image = getImage("/piece/b-pawn")
        }
    }

    override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            // Define move value based on the color
            val moveValue: Int
            if (color == GamePanel.WHITE) {
                moveValue = -1
            } else {
                moveValue = 1
            }
            // Check the hitting piece
            hittingPiece = getHittingPiece(targetCol, targetRow)

            // 1-square movement
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingPiece == null) {
                return true
            }
            // 2-square movement (only if not moved before)
            if (targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingPiece == null && moved == false && pieceIsOnStraightLine(
                    targetCol,
                    targetRow
                ) == false
            ) {
                return true
            }
            // Diagonal movement & Capture (if a piece is on a square diagonally in front of it)
// Diagonal movement & Capture
            val target = hittingPiece
            if (abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && target != null && target.color != color) {
                return true
            }
            // En Passant
            if (abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
                for (piece in GamePanel.pieces) {
                    if (piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        return false
    }
}
