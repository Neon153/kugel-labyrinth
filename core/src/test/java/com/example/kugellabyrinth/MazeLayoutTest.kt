package com.example.kugellabyrinth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class MazeLayoutTest {

    /** Pixel Watch 5: 41 mm = 408 px, 45 mm = 456 px; dazu ältere/andere Uhren und eine nicht quadratische Fläche. */
    private val watchSizes = listOf(408 to 408, 456 to 456, 384 to 384, 456 to 400)

    @Test
    fun fillRect_usesFullWidth_onTallScreens() {
        val l = MazeLayout.fillRect(1080, 1900, 9)
        assertEquals(120f, l.cell, 1e-3f)
        assertEquals(15, l.rows)
        assertEquals(0f, l.offsetX, 1e-3f)
        assertEquals(50f, l.offsetY, 1e-3f) // (1900 − 15·120) / 2
        assertTrue(l.height <= 1900f)
    }

    @Test
    fun fillRect_shrinksCells_whenMinimumRowsDoNotFit() {
        val l = MazeLayout.fillRect(1080, 300, 9)
        assertEquals(MazeLayout.MIN_ROWS, l.rows)
        assertEquals(60f, l.cell, 1e-3f)
        assertEquals((1080f - 9 * 60f) / 2f, l.offsetX, 1e-3f)
        assertTrue(l.width <= 1080f && l.height <= 300f + 1e-3f)
    }

    @Test
    fun squareInCircle_isSquare_centered_andInsideTheRoundDisplay() {
        for ((w, h) in watchSizes) for (d in Difficulty.entries) {
            val l = MazeLayout.squareInCircle(w, h, d.watchCols)
            assertEquals(l.cols, l.rows)
            assertEquals(l.width, l.height, 1e-3f)
            assertEquals(w / 2f, l.offsetX + l.width / 2f, 1e-3f)
            assertEquals(h / 2f, l.offsetY + l.height / 2f, 1e-3f)
            // Alle vier Ecken liegen im Kreis
            val r = minOf(w, h) / 2f
            for ((cx, cy) in listOf(0f to 0f, 1f to 0f, 0f to 1f, 1f to 1f)) {
                val x = l.offsetX + cx * l.width - w / 2f
                val y = l.offsetY + cy * l.height - h / 2f
                assertTrue("Ecke außerhalb des Kreises bei ${w}x$h", sqrt(x * x + y * y) <= r)
            }
            // ... und das Quadrat nutzt den Kreis gut aus (mindestens 95 % des Inkreis-Quadrats)
            assertTrue(l.width >= 0.95f * 2f * r / sqrt(2f))
        }
    }

    @Test
    fun watchCells_areLargeEnough_onPixelWatch5_41mm() {
        // 41 mm: 408 px bei 320 ppi. Selbst bei „Profi“ (8 × 8) sind Zellen ≥ 32 px ≈ 2,5 mm
        // und die Kugel hat ≥ 1,4 mm Durchmesser.
        val l = MazeLayout.squareInCircle(408, 408, Difficulty.PRO.watchCols)
        assertTrue("Zelle ${l.cell} px", l.cell >= 32f)
        val ballMm = 2 * GameEngine.BALL_RADIUS * l.cell / (320f / 25.4f)
        assertTrue("Kugel $ballMm mm", ballMm >= 1.4f)
    }
}
