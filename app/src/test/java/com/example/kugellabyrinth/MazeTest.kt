package com.example.kugellabyrinth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt
import kotlin.random.Random

class MazeTest {

    private fun mazes(): List<Maze> = buildList {
        for (seed in 0 until 30) {
            val rnd = Random(seed)
            add(Maze(2 + rnd.nextInt(12), 2 + rnd.nextInt(25), rnd))
        }
        for (d in Difficulty.entries) add(Maze(d.cols, d.cols * 2, Random(d.ordinal)))
        add(Maze(7, 14, Random(1)))
        add(Maze(12, 26, Random(2)))
    }

    @Test
    fun isPerfectMaze_allCellsReachable_noLoops() {
        for (m in mazes()) {
            val dist = m.bfs(0)
            assertTrue("alle Zellen erreichbar", dist.all { it >= 0 })
            var passages = 0
            for (r in 0 until m.rows) for (c in 0 until m.cols) {
                if (m.isOpen(c, r, Maze.RIGHT)) passages++
                if (m.isOpen(c, r, Maze.DOWN)) passages++
            }
            // Baum: genau Zellen - 1 Durchgänge
            assertEquals(m.cellCount - 1, passages)
        }
    }

    @Test
    fun openings_areSymmetric_andNeverLeaveTheGrid() {
        for (m in mazes()) {
            for (r in 0 until m.rows) for (c in 0 until m.cols) for (d in 0..3) {
                if (!m.isOpen(c, r, d)) continue
                val nc = c + Maze.DX[d]
                val nr = r + Maze.DY[d]
                assertTrue(nc in 0 until m.cols && nr in 0 until m.rows)
                assertTrue(m.isOpen(nc, nr, Maze.opposite(d)))
            }
        }
    }

    @Test
    fun goal_isFarthestCellFromStart() {
        for (m in mazes()) {
            val dist = m.bfs(m.startRow * m.cols + m.startCol)
            assertEquals(dist.max(), dist[m.goalRow * m.cols + m.goalCol])
        }
    }

    @Test
    fun wallRects_matchOpenings() {
        // Prüft an jeder Zellkante, ob dort genau dann ein Wand-Rechteck liegt, wenn die Kante geschlossen ist.
        for (m in mazes()) {
            fun wallAt(px: Float, py: Float): Boolean {
                val w = m.walls
                var i = 0
                while (i < w.size) {
                    if (px >= w[i] && px <= w[i + 2] && py >= w[i + 1] && py <= w[i + 3]) return true
                    i += 4
                }
                return false
            }
            for (r in 0 until m.rows) for (c in 0 until m.cols) {
                // rechte Kante, Mitte
                val rightClosed = !m.isOpen(c, r, Maze.RIGHT)
                assertEquals(rightClosed, wallAt(c + 1f, r + 0.5f))
                val downClosed = !m.isOpen(c, r, Maze.DOWN)
                assertEquals(downClosed, wallAt(c + 0.5f, r + 1f))
                // Zellmitte ist immer frei
                assertFalse(wallAt(c + 0.5f, r + 0.5f))
            }
        }
    }

    @Test
    fun cellIndex_containsEveryWallTheBallCanTouch() {
        val radius = GameEngine.BALL_RADIUS
        val rnd = Random(42)
        for (m in mazes()) {
            val w = m.walls
            repeat(2000) {
                val px = rnd.nextFloat() * m.cols
                val py = rnd.nextFloat() * m.rows
                val cell = py.toInt().coerceAtMost(m.rows - 1) * m.cols + px.toInt().coerceAtMost(m.cols - 1)
                val indexed = HashSet<Int>()
                for (k in m.cellWallStart[cell] until m.cellWallStart[cell + 1]) indexed.add(m.cellWalls[k])
                for (i in 0 until w.size / 4) {
                    val nx = px.coerceIn(w[4 * i], w[4 * i + 2])
                    val ny = py.coerceIn(w[4 * i + 1], w[4 * i + 3])
                    val d = sqrt((px - nx) * (px - nx) + (py - ny) * (py - ny))
                    if (d < radius) assertTrue("Wand $i fehlt im Index von Zelle $cell", i in indexed)
                }
            }
        }
    }

    @Test
    fun mergedWalls_areFewerThanCellEdges() {
        val m = Maze(12, 26, Random(7))
        val rects = m.walls.size / 4
        val edgeSegments = (m.rows + 1) * m.cols + (m.cols + 1) * m.rows - (m.cellCount - 1)
        assertTrue("$rects Rechtecke statt $edgeSegments Einzelwände", rects < edgeSegments)
    }
}
