package main

import javax.swing.JFrame

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val window = JFrame("Chess")
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        window.setResizable(false)

        val gp = GamePanel()
        window.add(gp)
        window.pack()
        window.setLocationRelativeTo(null)
        window.setVisible(true)

        gp.launchGame()
    }
}
