package piece

import main.GamePanel
import main.Type
import kotlin.math.abs

class Knight(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init {
        type = Type.KNIGHT
        if (color == GamePanel.WHITE) {
            image = getImage("/piece/w-knight")
        } else {
            image = getImage("/piece/b-knight")
        }
    }

    override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (isWithinBoard(targetCol, targetRow)) {
            if (abs(targetCol - preCol) * abs(targetRow - preRow) == 2) {
                if (isValidSquare(targetCol, targetRow)) {
                    return true
                }
            }
        }
        return false
    }
}
