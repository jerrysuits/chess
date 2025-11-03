package piece

import main.GamePanel
import main.Type
import kotlin.math.abs

class Queen(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init {
        type = Type.QUEEN
        if (color == GamePanel.WHITE) {
            image = getImage("/piece/w-queen")
        } else {
            image = getImage("/piece/b-queen")
        }
    }

    public override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            // Vertical and Horizontal
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol, targetRow) && pieceIsOnStraightLine(
                        targetCol,
                        targetRow
                    ) == false
                ) {
                    return true
                }
            }
            // Diagonal
            if (abs(targetCol - preCol) == abs(targetRow - preRow)) {
                if (isValidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(
                        targetCol,
                        targetRow
                    ) == false
                ) {
                    return true
                }
            }
        }
        return false
    }
}
