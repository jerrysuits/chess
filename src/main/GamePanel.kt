package main

import piece.Bishop
import piece.King
import piece.Knight
import piece.Pawn
import piece.Piece
import piece.Queen
import piece.Rook
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel
import kotlin.math.abs

class GamePanel : JPanel(), Runnable {
    val FPS: Int = 60
    var gameThread: Thread? = null
    var board: Board = Board()
    var mouse: Mouse = Mouse()

    var promotionPieces: ArrayList<Piece> = ArrayList<Piece>()
    var activePiece: Piece? = null
    var checkingPiece: Piece? = null
    var currentColor: Int = WHITE

    // GAME-STATE BOOLEANS
    var canMove: Boolean = false
    var validSquare: Boolean = false
    var promotion: Boolean = false
    var gameOver: Boolean = false
    var stalemate: Boolean = false

    init {
        setPreferredSize(Dimension(WIDTH, HEIGHT))
        setBackground(Color.black)
        addMouseMotionListener(mouse)
        addMouseListener(mouse)
        setPieces();
        //testPromotion();
        //testIllegal()

        copyPieces(pieces, simPieces)
    }

    fun launchGame() {
        gameThread = Thread(this)
        gameThread!!.start()
    }

    fun setPieces() {
        // White
        pieces.add(Pawn(WHITE, 0, 6))
        pieces.add(Pawn(WHITE, 1, 6))
        pieces.add(Pawn(WHITE, 2, 6))
        pieces.add(Pawn(WHITE, 3, 6))
        pieces.add(Pawn(WHITE, 4, 6))
        pieces.add(Pawn(WHITE, 5, 6))
        pieces.add(Pawn(WHITE, 6, 6))
        pieces.add(Pawn(WHITE, 7, 6))
        pieces.add(Rook(WHITE, 0, 7))
        pieces.add(Rook(WHITE, 7, 7))
        pieces.add(Knight(WHITE, 1, 7))
        pieces.add(Knight(WHITE, 6, 7))
        pieces.add(Bishop(WHITE, 2, 7))
        pieces.add(Bishop(WHITE, 5, 7))
        pieces.add(Queen(WHITE, 3, 7))
        pieces.add(King(WHITE, 4, 7))

        // Black
        pieces.add(Pawn(BLACK, 0, 1))
        pieces.add(Pawn(BLACK, 1, 1))
        pieces.add(Pawn(BLACK, 2, 1))
        pieces.add(Pawn(BLACK, 3, 1))
        pieces.add(Pawn(BLACK, 4, 1))
        pieces.add(Pawn(BLACK, 5, 1))
        pieces.add(Pawn(BLACK, 6, 1))
        pieces.add(Pawn(BLACK, 7, 1))
        pieces.add(Rook(BLACK, 0, 0))
        pieces.add(Rook(BLACK, 7, 0))
        pieces.add(Knight(BLACK, 1, 0))
        pieces.add(Knight(BLACK, 6, 0))
        pieces.add(Bishop(BLACK, 2, 0))
        pieces.add(Bishop(BLACK, 5, 0))
        pieces.add(Queen(BLACK, 3, 0))
        pieces.add(King(BLACK, 4, 0))
    }

    fun testPromotion() {
        pieces.add(Pawn(WHITE, 0, 3))
        pieces.add(Pawn(BLACK, 5, 4))
    }

    fun testIllegal() {
        pieces.add(Pawn(WHITE, 7, 6))
        pieces.add(King(WHITE, 3, 7))
        pieces.add(King(BLACK, 0, 3))
        pieces.add(Bishop(BLACK, 1, 4))
        pieces.add(Queen(BLACK, 4, 5))
    }

    private fun copyPieces(source: ArrayList<Piece>, target: ArrayList<Piece>) {
        target.clear()
        for (i in source.indices) {
            target.add(source.get(i))
        }
    }

    override fun run() {
        val drawInterval = (1000000000 / FPS).toDouble()
        var delta = 0.0
        var lastTime = System.nanoTime()
        var currentTime: Long

        while (gameThread != null) {
            currentTime = System.nanoTime()
            delta += (currentTime - lastTime) / drawInterval
            lastTime = currentTime
            if (delta >= 1) {
                update()
                repaint()
                delta--
            }
        }
    }

    private fun update() {
        if (promotion) {
            promoting()
        } else if (!gameOver && !stalemate) {
            /** MOUSE BUTTON PRESSED */
            if (mouse.pressed) {
                if (activePiece == null) {
                    /** If there is no active piece, check if you can pick up a piece */
                    for (piece in simPieces) {
                        /**  If the mouse is on the same color, pick it up as the active piece */
                        if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activePiece = piece
                            break
                        }
                    }
                } else {
                    // If the player is holding a piece, simulate the move
                    simulate()
                }
            }
            /** MOUSE BUTTON RELEASED */
            if (!mouse.pressed) {
                if (activePiece != null) {
                    if (validSquare) {
                        // Move confirmed

                        // Update the piece list in case a piece has been captured and removed during the simulation

                        copyPieces(simPieces, pieces)
                        activePiece!!.updatePosition()
                        if (castlingPiece != null) {
                            castlingPiece!!.updatePosition()
                        }

                        if (this.isKingInCheck && this.isCheckmate) {
                            gameOver = true
                        } else if (isStalemate()) {
                            stalemate = true
                        } else {
                            if (canPromote()) {
                                promotion = true
                            } else {
                                changePlayer()
                            }
                        }
                    } else {
                        // The move is not valid, so reset
                        copyPieces(simPieces, pieces)
                        activePiece!!.resetPosition()
                        activePiece = null
                    }
                }
            }
        }
    }

    fun simulate() {
        canMove = false
        validSquare = false

        // Repeat the list in every loop
        copyPieces(pieces, simPieces)

        if (castlingPiece != null) {
            castlingPiece!!.col = castlingPiece!!.preCol
            castlingPiece!!.x = castlingPiece!!.getX(castlingPiece!!.col)
            castlingPiece = null
        }

        // If a piece is being held, update its positioned
        activePiece!!.x = mouse.x - Board.HALF_SQUARE_SIZE
        activePiece!!.y = mouse.y - Board.HALF_SQUARE_SIZE
        activePiece!!.col = activePiece!!.getCol(activePiece!!.x)
        activePiece!!.row = activePiece!!.getRow(activePiece!!.y)

        // Check if piece is hovering above a reachable square
        if (activePiece!!.canMove(activePiece!!.col, activePiece!!.row)) {
            canMove = true

            // if hitting a piece, remove from board
            if (activePiece!!.hittingPiece != null) {
                simPieces.remove(activePiece!!.hittingPiece)
            }
            checkCastling()

            if (!isIllegal(activePiece!!) && !opponentCanCaptureKing()) {
                validSquare = true
            }
        }
    }

    private fun isIllegal(king: Piece): Boolean {
        if (king.type == Type.KING) {
            for (piece in simPieces) {
                if (piece !== king && piece.color != king.color && piece.canMove(
                        king.col,
                        king.row
                    )
                ) {
                    return true
                }
            }
        }
        return false
    }

    private fun opponentCanCaptureKing(): Boolean {
        val king = getKing(false)

        for (piece in simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true
            }
        }
        return false
    }

    private val isKingInCheck: Boolean
        get() {
            val king = getKing(true)

            if (activePiece!!.canMove(king.col, king.row)) {
                checkingPiece = activePiece
                return true
            } else {
                checkingPiece = null
            }

            return false
        }

    private fun getKing(opponent: Boolean): Piece {
        for (piece in simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    return piece
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    return piece
                }
            }
        }
        throw IllegalStateException("King not found - corrupted game state")
    }

    private val isCheckmate: Boolean
        get() {
            val king = getKing(true)

            if (kingCanMove(king)) {
                return false
            } else {
                // Check if the attack can be blocked by another piece
                // Check the position of the checking piece and the King on check
                val colDiff = abs(checkingPiece!!.col - king.col)
                val rowDiff = abs(checkingPiece!!.row - king.row)

                if (colDiff == 0) {
                    // The chacking piece is attacking vertically
                    if (checkingPiece!!.row < king.row) {
                        // The checking piece is above the king
                        for (row in checkingPiece!!.row..<king.row) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(
                                        checkingPiece!!.col,
                                        row
                                    )
                                ) {
                                    return false
                                }
                            }
                        }
                    }
                    if (checkingPiece!!.row > king.row) {
                        // The checking Piece is below the king
                        for (row in checkingPiece!!.row downTo king.row + 1) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(
                                        checkingPiece!!.col,
                                        row
                                    )
                                ) {
                                    return false
                                }
                            }
                        }
                    }
                } else if (rowDiff == 0) {
                    // The checking piece is attacking horizontally
                    if (checkingPiece!!.col < king.col) {
                        // The checking piece is to the left of the King
                        for (col in checkingPiece!!.col..<king.col) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(
                                        col,
                                        checkingPiece!!.row
                                    )
                                ) {
                                    return false
                                }
                            }
                        }
                    }
                    if (checkingPiece!!.col > king.col) {
                        // The checking piece is to the right of the King
                        for (col in checkingPiece!!.col downTo king.col + 1) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(
                                        col,
                                        checkingPiece!!.row
                                    )
                                ) {
                                    return false
                                }
                            }
                        }
                    }
                } else if (colDiff == rowDiff) {
                    // The checking piece is attacking diagonally
                    if (checkingPiece!!.row < king.row) {
                        // The checking piece is attacking from the top
                        if (checkingPiece!!.col < king.col) {
                            // The checking piece is attacking from the top left
                            var col = checkingPiece!!.col
                            var row = checkingPiece!!.row
                            while (col < king.col) {
                                for (piece in simPieces) {
                                    if (piece !== king && piece.color != currentColor && piece.canMove(
                                            col,
                                            row
                                        )
                                    ) {
                                        return false
                                    }
                                }
                                col++
                                row++
                            }
                        }
                        if (checkingPiece!!.col > king.col) {
                            // The checking piece is attacking from the top right
                            var col = checkingPiece!!.col
                            var row = checkingPiece!!.row
                            while (col > king.col) {
                                for (piece in simPieces) {
                                    if (piece !== king && piece.color != currentColor && piece.canMove(
                                            col,
                                            row
                                        )
                                    ) {
                                        return false
                                    }
                                }
                                col--
                                row++
                            }
                        }
                    }
                    if (checkingPiece!!.row > king.row) {
                        //The checking piece is attacking from the bottom
                        if (checkingPiece!!.col < king.col) {
                            // The checking piece is attacking from the bottom left
                            var col = checkingPiece!!.col
                            var row = checkingPiece!!.row
                            while (col < king.col) {
                                for (piece in simPieces) {
                                    if (piece !== king && piece.color != currentColor && piece.canMove(
                                            col,
                                            row
                                        )
                                    ) {
                                        return false
                                    }
                                }
                                col++
                                row--
                            }
                        }
                        if (checkingPiece!!.col > king.col) {
                            // The checking piece is attacking from the bottom right
                            var col = checkingPiece!!.col
                            var row = checkingPiece!!.row
                            while (col > king.col) {
                                for (piece in simPieces) {
                                    if (piece !== king && piece.color != currentColor && piece.canMove(
                                            col,
                                            row
                                        )
                                    ) {
                                        return false
                                    }
                                }
                                col--
                                row--
                            }
                        }
                    }
                }
            }

            return true
        }

    private fun kingCanMove(king: Piece): Boolean {
        // Simulate if there is any square the King can move to

        if (isValidMove(king, -1, -1)) {
            return true
        }
        if (isValidMove(king, 0, -1)) {
            return true
        }
        if (isValidMove(king, 1, -1)) {
            return true
        }
        if (isValidMove(king, -1, 0)) {
            return true
        }
        if (isValidMove(king, 1, 0)) {
            return true
        }
        if (isValidMove(king, -1, 1)) {
            return true
        }
        if (isValidMove(king, 0, 1)) {
            return true
        }
        if (isValidMove(king, 1, 1)) {
            return true
        }

        return false
    }

    private fun isValidMove(king: Piece, colPlus: Int, rowPlus: Int): Boolean {
        var isValidMove = false
        // Temporarily update the kings position
        king.col += colPlus
        king.row += rowPlus
        if (king.canMove(king.col, king.row)) {
            if (king.hittingPiece != null) {
                simPieces.remove(king.hittingPiece)
            }
            if (!isIllegal(king)) {
                isValidMove = true
            }
        }
        // Reset King's position and remove the hitting piece
        king.resetPosition()
        copyPieces(pieces, simPieces)

        return isValidMove
    }

    private fun isStalemate(): Boolean {
        var count = 0
        for (piece in simPieces) {
            if (piece.color != currentColor) {
                count++
            }
        }
        if (count == 1) {
            if (kingCanMove(getKing(true)) == false) {
                return true
            }
        }
        return false
    }

    private fun checkCastling() {
        if (castlingPiece != null) {
            if (castlingPiece!!.col == 0) {
                castlingPiece!!.col += 3
            } else if (castlingPiece!!.col == 7) {
                castlingPiece!!.col -= 2
            }
            castlingPiece!!.x = castlingPiece!!.getX(castlingPiece!!.col)
        }
    }

    private fun changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK
            // Change black's two-stepped status
            for (piece in pieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false
                }
            }
        } else {
            currentColor = WHITE
            for (piece in pieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false
                }
            }
        }
        activePiece = null
    }

    private fun canPromote(): Boolean {
        if (activePiece!!.type == Type.PAWN) {
            if (currentColor == WHITE && activePiece!!.row == 0 || currentColor == BLACK && activePiece!!.row == 7) {
                promotionPieces.clear()
                promotionPieces.add(Rook(currentColor, 9, 2))
                promotionPieces.add(Knight(currentColor, 9, 3))
                promotionPieces.add(Bishop(currentColor, 9, 4))
                promotionPieces.add(Queen(currentColor, 9, 5))
                return true
            }
        }
        return false
    }

    private fun promoting() {
        if (mouse.pressed) {
            for (piece in promotionPieces) {
                if (piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                    when (piece.type) {
                        Type.ROOK -> simPieces.add(
                            Rook(
                                currentColor,
                                activePiece!!.col,
                                activePiece!!.row
                            )
                        )

                        Type.KNIGHT -> simPieces.add(
                            Knight(
                                currentColor,
                                activePiece!!.col,
                                activePiece!!.row
                            )
                        )

                        Type.BISHOP -> simPieces.add(
                            Bishop(
                                currentColor,
                                activePiece!!.col,
                                activePiece!!.row
                            )
                        )

                        Type.QUEEN -> simPieces.add(
                            Queen(
                                currentColor,
                                activePiece!!.col,
                                activePiece!!.row
                            )
                        )

                        else -> {}
                    }
                    simPieces.remove(activePiece)
                    copyPieces(simPieces, pieces)
                    activePiece = null
                    promotion = false
                    changePlayer()
                }
            }
        }
    }

    public override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val g2 = g as Graphics2D

        // Draw the chessboard (left side)
        board.draw(g2)

        // Draw pieces on the board
        for (p in simPieces) {
            p.draw(g2)
        }

        if (activePiece != null) {
            if (canMove) {
                if (isIllegal(activePiece!!) || opponentCanCaptureKing()) {
                    g2.setColor(Color.red)
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f))
                    g2.fillRect(
                        activePiece!!.col * Board.SQUARE_SIZE,
                        activePiece!!.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE
                    )
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f))
                } else {
                    g2.setColor(Color.white)
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f))
                    g2.fillRect(
                        activePiece!!.col * Board.SQUARE_SIZE,
                        activePiece!!.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE
                    )
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f))
                }
            }

            activePiece!!.draw(g2)
        }

        // STATUS MESSAGES
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        g2.setFont(Font("Book Antiqua", Font.PLAIN, 30))
        g2.setColor(Color.white)

        if (promotion) {
            g2.drawString("Promote to: ", 700, 150)
            for (piece in promotionPieces) {
                g2.drawImage(
                    piece.image, piece.getX(piece.col), piece.getY(piece.row), Board.SQUARE_SIZE,
                    Board.SQUARE_SIZE, null
                )
            }
        } else {
            if (currentColor == WHITE) {
                g2.drawString("White's turn", 700, 550)
                if (checkingPiece != null && checkingPiece!!.color == BLACK) {
                    g2.setColor(Color.red)
                    g2.drawString("King in check", 700, 450)
                }
            } else {
                g2.drawString("Black's turn", 700, 150)
                if (checkingPiece != null && checkingPiece!!.color == WHITE) {
                    g2.setColor(Color.red)
                    g2.drawString("King in check", 700, 50)
                }
            }
        }
        if (gameOver) {
            var s = ""
            if (currentColor == WHITE) {
                s = "WHITE WINS!"
            } else if (currentColor == BLACK) {
                s = "BLACK WINS"
            }
            g2.setFont(Font("Arial", Font.PLAIN, 30))
            g2.setColor(Color.GREEN)
            g2.drawString(s, 200, 420)
        }
        if (stalemate) {
            g2.setFont(Font("Arial", Font.PLAIN, 30))
            g2.setColor(Color.lightGray)
            g2.drawString("STALEMATE", 200, 420)
        }
    }

    companion object {
        const val WIDTH: Int = 940
        const val HEIGHT: Int = 640

        // PIECES
        @JvmField
        var pieces: ArrayList<Piece> = ArrayList<Piece>()
        @JvmField
        var simPieces: ArrayList<Piece> = ArrayList<Piece>()
        @JvmField
        var castlingPiece: Piece? = null


        // COLOR
        const val WHITE: Int = 0
        const val BLACK: Int = 1
    }
}
