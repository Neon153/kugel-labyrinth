package com.example.kugellabyrinth

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Platzierung des Labyrinths auf der Spielfläche in Pixeln.
 *
 * Android-frei, damit die Geometrie für Handy und Uhr per JUnit geprüft werden kann.
 *
 * @property cols Spalten des Labyrinths
 * @property rows Zeilen des Labyrinths
 * @property cell Kantenlänge einer Zelle in Pixeln
 * @property offsetX linker Rand des Labyrinths in Pixeln
 * @property offsetY oberer Rand des Labyrinths in Pixeln
 */
class MazeLayout(
    val cols: Int,
    val rows: Int,
    val cell: Float,
    val offsetX: Float,
    val offsetY: Float
) {
    val width: Float get() = cols * cell
    val height: Float get() = rows * cell

    companion object {
        /** Mindestzahl an Zeilen auf rechteckigen Flächen. */
        const val MIN_ROWS = 5

        /** Anteil des Inkreis-Quadrats, den das Labyrinth auf runden Displays nutzt (Rest = Rand). */
        const val CIRCLE_FILL = 0.96f

        /**
         * Rechteckige Fläche (Handy): volle Breite, so viele Zeilen wie in die Höhe passen,
         * mindestens [MIN_ROWS]. Das Labyrinth wird zentriert.
         */
        fun fillRect(viewWidth: Int, viewHeight: Int, cols: Int): MazeLayout {
            require(cols >= 2 && viewWidth > 0 && viewHeight > 0)
            var cell = viewWidth.toFloat() / cols
            val rows = max(MIN_ROWS, (viewHeight / cell).toInt())
            if (rows * cell > viewHeight) cell = viewHeight.toFloat() / rows
            return MazeLayout(
                cols, rows, cell,
                (viewWidth - cols * cell) / 2f,
                (viewHeight - rows * cell) / 2f
            )
        }

        /**
         * Runde Fläche (Uhr): quadratisches Labyrinth cols × cols im Quadrat, das in den Kreis passt
         * (Seitenlänge = Durchmesser / √2), verkleinert um [CIRCLE_FILL]. Zentriert.
         */
        fun squareInCircle(viewWidth: Int, viewHeight: Int, cols: Int): MazeLayout {
            require(cols >= 2 && viewWidth > 0 && viewHeight > 0)
            val side = min(viewWidth, viewHeight) / sqrt(2f) * CIRCLE_FILL
            val cell = side / cols
            return MazeLayout(
                cols, cols, cell,
                (viewWidth - side) / 2f,
                (viewHeight - side) / 2f
            )
        }
    }
}
