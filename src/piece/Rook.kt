package piece

import main.GamePanel
import main.Type

class Rook(color: Int, col: Int, row: Int) : Piece(color, col, row) {
    init {
        type = Type.ROOK

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/w-rook")
        } else {
            image = getImage("/piece/b-rook")
        }
    }

    public override fun canMove(targetCol: Int, targetRow: Int): Boolean {
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            if (targetCol == preCol || targetRow == preRow) {
                if (isValidSquare(targetCol, targetRow) && pieceIsOnStraightLine(
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
