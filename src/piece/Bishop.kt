package piece

import main.GamePanel
import main.Type
import kotlin.math.abs

class Bishop(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init {
        type = Type.BISHOP
        if (color == GamePanel.WHITE) {
            image = getImage("/piece/w-bishop")
        } else {
            image = getImage("/piece/b-bishop")
        }
    }

    override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
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
