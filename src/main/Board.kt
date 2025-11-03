package main

import java.awt.Color
import java.awt.Graphics2D

class Board {
    val MAX_COL: Int = 8
    val MAX_ROW: Int = 8
    fun draw(g2: Graphics2D) {
        var c = 0
        for (row in 0..<MAX_ROW) {
            for (col in 0..<MAX_COL) {
                if (c == 0) {
                    g2.setColor(Color(210, 165, 125))
                    c = 1
                } else {
                    g2.setColor(Color(175, 115, 70))
                    c = 0
                }
                g2.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE)
            }
            if (c == 0) {
                c = 1
            } else {
                c = 0
            }
        }
    }

    companion object {
        const val SQUARE_SIZE: Int = 80
        @kotlin.jvm.JvmField
        val HALF_SQUARE_SIZE: Int = SQUARE_SIZE / 2
    }
}

