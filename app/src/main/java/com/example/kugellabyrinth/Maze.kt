package com.example.kugellabyrinth

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random

/**
 * Perfektes Labyrinth (Recursive Backtracker, iterativ). Koordinaten in Zelleneinheiten:
 * Zelle (c, r) belegt das Quadrat [c, c+1] x [r, r+1].
 *
 * Für die Kollision werden die Wände
 *  1. zu möglichst langen Rechtecken zusammengefasst (weniger Rechtecke, keine Nahtstellen) und
 *  2. pro Zelle vorindiziert (CSR-Format), sodass pro Physik-Schritt nur die Handvoll Wände
 *     rund um die Kugel geprüft wird statt aller Wände.
 */
class Maze(val cols: Int, val rows: Int, random: Random = Random.Default) {

    companion object {
        /** Richtungen: 0 = rechts, 1 = unten, 2 = links, 3 = oben */
        val DX = intArrayOf(1, 0, -1, 0)
        val DY = intArrayOf(0, 1, 0, -1)
        const val RIGHT = 0
        const val DOWN = 1

        const val WALL_THICKNESS = 0.14f

        /** Reichweite des Zellen-Index über den Zellrand hinaus. Muss >= Kugelradius sein. */
        const val INDEX_REACH = 0.36f

        fun opposite(dir: Int) = (dir + 2) and 3
    }

    init {
        require(cols >= 2 && rows >= 2) { "Labyrinth muss mindestens 2x2 groß sein" }
    }

    val cellCount = cols * rows

    /** Bitmaske pro Zelle: Bit d gesetzt = Richtung d ist offen. Index = r * cols + c. */
    private val openMask = IntArray(cellCount)

    val startCol = 0
    val startRow = 0
    val goalCol: Int
    val goalRow: Int

    /** Wand-Rechtecke als [left, top, right, bottom] hintereinander. */
    val walls: FloatArray

    /** Zellen-Index: Wände der Zelle i stehen in cellWalls[cellWallStart[i] until cellWallStart[i + 1]]. */
    val cellWallStart: IntArray
    val cellWalls: IntArray

    init {
        generate(random)
        val dist = bfs(startRow * cols + startCol)
        var far = 0
        for (i in dist.indices) if (dist[i] > dist[far]) far = i
        goalCol = far % cols
        goalRow = far / cols
        walls = buildWalls()
        val start = IntArray(cellCount + 1)
        cellWalls = buildIndex(start)
        cellWallStart = start
    }

    fun openMask(c: Int, r: Int): Int = openMask[r * cols + c]

    fun isOpen(c: Int, r: Int, dir: Int): Boolean = (openMask[r * cols + c] shr dir) and 1 == 1

    // ------------------------------------------------------------------------------------

    private fun generate(random: Random) {
        val visited = BooleanArray(cellCount)
        val stack = IntArray(cellCount)
        val options = IntArray(4)
        var sp = 0
        val start = startRow * cols + startCol
        visited[start] = true
        stack[sp++] = start
        while (sp > 0) {
            val cur = stack[sp - 1]
            val c = cur % cols
            val r = cur / cols
            var n = 0
            for (d in 0..3) {
                val nc = c + DX[d]
                val nr = r + DY[d]
                if (nc in 0 until cols && nr in 0 until rows && !visited[nr * cols + nc]) options[n++] = d
            }
            if (n == 0) {
                sp--
                continue
            }
            val d = options[random.nextInt(n)]
            val next = (r + DY[d]) * cols + (c + DX[d])
            openMask[cur] = openMask[cur] or (1 shl d)
            openMask[next] = openMask[next] or (1 shl opposite(d))
            visited[next] = true
            stack[sp++] = next
        }
    }

    /** Breitensuche; liefert die Weglänge (in Zellen) jeder Zelle zum Startfeld [from]. */
    fun bfs(from: Int): IntArray {
        val dist = IntArray(cellCount) { -1 }
        val queue = IntArray(cellCount)
        var head = 0
        var tail = 0
        dist[from] = 0
        queue[tail++] = from
        while (head < tail) {
            val cur = queue[head++]
            val c = cur % cols
            val r = cur / cols
            val mask = openMask[cur]
            for (d in 0..3) {
                if ((mask shr d) and 1 == 0) continue
                val n = (r + DY[d]) * cols + (c + DX[d])
                if (dist[n] == -1) {
                    dist[n] = dist[cur] + 1
                    queue[tail++] = n
                }
            }
        }
        return dist
    }

    /** Fasst zusammenhängende Wandstücke einer Linie zu einem Rechteck zusammen. */
    private fun buildWalls(): FloatArray {
        val t = WALL_THICKNESS / 2f
        val out = FloatArray(4 * ((rows + 1) * (cols + 1) / 2 + (cols + 1) * (rows + 1) / 2 + cols + rows + 4))
        var n = 0

        // horizontale Linien y = 0..rows
        for (y in 0..rows) {
            var runStart = -1
            for (c in 0..cols) {
                val wall = c < cols && (y == 0 || y == rows || !isOpen(c, y - 1, DOWN))
                if (wall && runStart < 0) runStart = c
                if (!wall && runStart >= 0) {
                    out[n++] = runStart - t; out[n++] = y - t; out[n++] = c + t; out[n++] = y + t
                    runStart = -1
                }
            }
        }
        // vertikale Linien x = 0..cols
        for (x in 0..cols) {
            var runStart = -1
            for (r in 0..rows) {
                val wall = r < rows && (x == 0 || x == cols || !isOpen(x - 1, r, RIGHT))
                if (wall && runStart < 0) runStart = r
                if (!wall && runStart >= 0) {
                    out[n++] = x - t; out[n++] = runStart - t; out[n++] = x + t; out[n++] = r + t
                    runStart = -1
                }
            }
        }
        return out.copyOf(n)
    }

    /** Ordnet jeder Zelle alle Wände zu, die eine Kugel mit Mittelpunkt in dieser Zelle berühren kann. */
    private fun buildIndex(start: IntArray): IntArray {
        val rectCount = walls.size / 4
        val counts = IntArray(cellCount)
        for (pass in 0..1) {
            val fill = if (pass == 1) IntArray(start[cellCount]) else null
            val cursor = if (pass == 1) start.copyOf() else null
            for (i in 0 until rectCount) {
                val l = walls[4 * i]
                val tp = walls[4 * i + 1]
                val r = walls[4 * i + 2]
                val b = walls[4 * i + 3]
                val c0 = floor(l - 1f - INDEX_REACH).toInt().coerceAtLeast(0)
                val c1 = ceil(r + INDEX_REACH).toInt().coerceAtMost(cols - 1)
                val r0 = floor(tp - 1f - INDEX_REACH).toInt().coerceAtLeast(0)
                val r1 = ceil(b + INDEX_REACH).toInt().coerceAtMost(rows - 1)
                for (cy in r0..r1) {
                    if (cy - INDEX_REACH >= b || cy + 1 + INDEX_REACH <= tp) continue
                    for (cx in c0..c1) {
                        if (cx - INDEX_REACH >= r || cx + 1 + INDEX_REACH <= l) continue
                        val cell = cy * cols + cx
                        if (fill == null) counts[cell]++ else fill[cursor!![cell]++] = i
                    }
                }
            }
            if (fill == null) {
                for (k in 0 until cellCount) start[k + 1] = start[k] + counts[k]
            } else {
                return fill
            }
        }
        error("unreachable")
    }
}
