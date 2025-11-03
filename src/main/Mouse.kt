package main

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class Mouse : MouseAdapter() {
    var x: Int = 0
    var y: Int = 0
    var pressed: Boolean = false
    override fun mousePressed(e: MouseEvent?) {
        pressed = true
    }

    override fun mouseReleased(e: MouseEvent?) {
        pressed = false
    }

    override fun mouseDragged(e: MouseEvent) {
        x = e.getX()
        y = e.getY()
    }

    override fun mouseMoved(e: MouseEvent) {
        x = e.getX()
        y = e.getY()
    }
}
