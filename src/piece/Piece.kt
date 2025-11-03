package piece

import main.Board
import main.GamePanel
import main.Type
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.Objects
import javax.imageio.ImageIO
import kotlin.math.abs

open class Piece(var color: Int, var col: Int, var row: Int) {
    @JvmField
    var type: Type? = null
    @JvmField
    var image: BufferedImage? = null
    var x: Int
    var y: Int
    @JvmField
    var preCol: Int
    @JvmField
    var preRow: Int
    var hittingPiece: Piece? = null
    var moved: Boolean = false
    var twoStepped: Boolean = false

    init {
        x = getX(col)
        y = getY(row)
        preCol = col
        preRow = row
    }

    fun getImage(imagePath: String?): BufferedImage? {
        var image: BufferedImage? = null
        try {
            image = ImageIO.read(
                Objects.requireNonNull<InputStream?>(
                    javaClass.getResourceAsStream(imagePath + ".png")
                )
            )
            val scaledImage =
                image.getScaledInstance(Board.SQUARE_SIZE, Board.SQUARE_SIZE, Image.SCALE_SMOOTH)
            val resizedImage =
                BufferedImage(Board.SQUARE_SIZE, Board.SQUARE_SIZE, BufferedImage.TYPE_INT_ARGB)
            val g2 = resizedImage.createGraphics()
            g2.drawImage(scaledImage, 0, 0, null)
            g2.dispose()
            image = resizedImage
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return image
    }

    fun getX(col: Int): Int {
        return col * Board.SQUARE_SIZE
    }

    fun getY(row: Int): Int {
        return row * Board.SQUARE_SIZE
    }

    fun getCol(x: Int): Int {
        return (x + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
    }

    fun getRow(y: Int): Int {
        return (y + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
    }

    val index: Int
        get() {
            for (index in GamePanel.simPieces.indices) {
                if (GamePanel.simPieces.get(index) === this) {
                    return index
                }
            }
            return 0
        }

    fun updatePosition() {
        // Check En Passant

        if (type == Type.PAWN) {
            if (abs(row - preRow) == 2) {
                twoStepped = true
            }
        }

        x = getX(col)
        y = getY(row)
        preCol = getCol(x)
        preRow = getRow(y)
        moved = true
    }

    fun resetPosition() {
        col = preCol
        row = preRow
        x = getX(col)
        y = getY(row)
    }

    open fun canMove(targetCol: Int, targetRow: Int): Boolean {
        return false
    }

    fun isWithinBoard(targetCol: Int, targetRow: Int): Boolean {
        if (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
            return true
        }
        return false
    }

    fun isSameSquare(targetCol: Int, targetRow: Int): Boolean {
        if (targetCol == preCol && targetRow == preRow) {
            return true
        }
        return false
    }

    fun getHittingPiece(targetCol: Int, targetRow: Int): Piece? {
        for (piece in GamePanel.simPieces) {
            if (piece.col == targetCol && piece.row == targetRow && piece !== this) {
                return piece
            }
        }
        return null
    }

    fun isValidSquare(targetCol: Int, targetRow: Int): Boolean {
        hittingPiece = getHittingPiece(targetCol, targetRow)
        if (hittingPiece == null) { // This square is vacant
            return true
        } else { // This square is occupied
            if (hittingPiece!!.color != this.color) { // if the color is different, it can be captured
                return true
            } else {
                hittingPiece = null
            }
        }
        return false
    }

    fun pieceIsOnStraightLine(targetCol: Int, targetRow: Int): Boolean {
        // When piece is moving to the left
        for (c in preCol - 1 downTo targetCol + 1) {
            for (piece in GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece
                    return true
                }
            }
        }
        // When piece is moving to the right
        for (c in preCol + 1..<targetCol) {
            for (piece in GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece
                    return true
                }
            }
        }
        // When piece is moving up
        for (r in preRow - 1 downTo targetRow + 1) {
            for (piece in GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingPiece = piece
                    return true
                }
            }
        }
        // When piece is moving down
        for (r in preRow + 1..<targetRow) {
            for (piece in GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingPiece = piece
                    return true
                }
            }
        }
        return false
    }

    fun pieceIsOnDiagonalLine(targetCol: Int, targetRow: Int): Boolean {
        if (targetRow < preRow) {
            // Up Left
            for (c in preCol - 1 downTo targetCol + 1) {
                val diff = abs(c - preCol)
                for (piece in GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
            // Up Right
            for (c in preCol + 1..<targetCol) {
                val diff = abs(c - preCol)
                for (piece in GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        if (targetRow > preRow) {
            // Down Left
            for (c in preCol - 1 downTo targetCol + 1) {
                val diff = abs(c - preCol)
                for (piece in GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
            // Down Right
            for (c in preCol + 1..<targetCol) {
                val diff = abs(c - preCol)
                for (piece in GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece
                        return true
                    }
                }
            }
        }
        return false
    }

    fun draw(g2: Graphics2D) {
        if (image != null) {
            // Calculate offset to center the 50x50 piece in a 100x100 square
            val offset = (Board.SQUARE_SIZE - 50) / 2 // (100 - 50)/2 = 25
            val drawX = x + offset // x = col * 100
            val drawY = y + offset // y = row * 100
            g2.drawImage(image, drawX, drawY, 50, 50, null)
        } else {
            // Fallback (optional)
            g2.setColor(Color.RED)
            g2.fillRect(x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE)
        }
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null)
    }
}
