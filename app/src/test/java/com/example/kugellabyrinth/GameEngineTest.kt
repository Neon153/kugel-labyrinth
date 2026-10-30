package com.example.kugellabyrinth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngineTest {

    private val speeds = floatArrayOf(0.5f, 1f, 2f)

    private fun minWallDistance(e: GameEngine): Float {
        val w = e.maze.walls
        var best = Float.MAX_VALUE
        for (i in 0 until w.size / 4) {
            val nx = e.x.coerceIn(w[4 * i], w[4 * i + 2])
            val ny = e.y.coerceIn(w[4 * i + 1], w[4 * i + 3])
            val d = sqrt((e.x - nx) * (e.x - nx) + (e.y - ny) * (e.y - ny))
            if (d < best) best = d
        }
        return best
    }

    private fun assertNotInWall(e: GameEngine) {
        assertTrue("Kugel steckt in einer Wand (${e.x}, ${e.y})", minWallDistance(e) >= GameEngine.BALL_RADIUS - 1e-3f)
        assertTrue(e.x > 0f && e.x < e.maze.cols && e.y > 0f && e.y < e.maze.rows)
    }

    /**
     * Test-"Spieler": PD-Regler auf die Mitte der nächsten Zelle des Lösungswegs,
     * Kippen auf [maxTilt] begrenzt (wie ein Mensch).
     */
    private fun play(e: GameEngine, maxTilt: Float = 4f, timeout: Float = 900f): Float {
        val m = e.maze
        val toGoal = m.bfs(m.goalRow * m.cols + m.goalCol)
        e.start()
        var t = 0f
        val dt = 1f / 60f
        while (t < timeout) {
            val cx = floor(e.x).toInt().coerceIn(0, m.cols - 1)
            val cy = floor(e.y).toInt().coerceIn(0, m.rows - 1)
            var tx = cx + 0.5f
            var ty = cy + 0.5f
            for (d in 0..3) {
                if (!m.isOpen(cx, cy, d)) continue
                val n = (cy + Maze.DY[d]) * m.cols + cx + Maze.DX[d]
                if (toGoal[n] < toGoal[cy * m.cols + cx]) {
                    tx += Maze.DX[d]
                    ty += Maze.DY[d]
                }
            }
            var ix = (40f * (tx - e.x) - 12f * e.vx) / e.accelPerTilt
            var iy = (40f * (ty - e.y) - 12f * e.vy) / e.accelPerTilt
            val mag = sqrt(ix * ix + iy * iy)
            val clamped = minOf(mag, maxTilt)
            if (mag > 1e-3f) {
                val k = (clamped + e.deadZone) / mag
                ix *= k
                iy *= k
            }
            if (e.update(dt, ix, iy)) return e.elapsed
            t += dt
            assertNotInWall(e)
        }
        throw AssertionError("Ziel nicht erreicht (${e.difficulty}/${e.assist}/${e.speedFactor}) bei (${e.x}, ${e.y})")
    }

    private fun mazeFor(d: Difficulty, seed: Int) = Maze(d.cols, d.cols * 2, Random(seed))

    @Test
    fun everyCombination_atEverySpeed_isSolvable_withoutWallPenetration() {
        for (d in Difficulty.entries) for (a in AssistMode.entries) for (s in speeds) for (seed in 0 until 2) {
            assertTrue(play(GameEngine(mazeFor(d, seed), d, a, s)) > 0f)
        }
    }

    @Test
    fun noTilt_meansNoMovement_inEveryMode() {
        for (d in Difficulty.entries) for (a in AssistMode.entries) {
            val e = GameEngine(mazeFor(d, 3), d, a, 2f)
            e.start()
            repeat(600) { e.update(1f / 60f, 0f, 0f) }
            assertEquals(0.5f, e.x, 1e-4f)
            assertEquals(0.5f, e.y, 1e-4f)
        }
    }

    @Test
    fun tiltWithinDeadZone_doesNotMoveBall() {
        val e = GameEngine(Maze(9, 14, Random(1)), Difficulty.PRO, AssistMode.SOFTWARE)
        e.start()
        repeat(300) { e.update(1f / 60f, 0.1f, -0.1f) }
        assertEquals(0.5f, e.x, 1e-4f)
        assertEquals(0.5f, e.y, 1e-4f)
    }

    /** Labyrinth, in dem vom Start aus zwei Felder nach rechts frei sind. */
    private fun straightStart() =
        (0 until 200).map { Maze(9, 14, Random(it)) }.first { it.isOpen(0, 0, Maze.RIGHT) && it.isOpen(1, 0, Maze.RIGHT) }

    @Test
    fun ballIsFast_atDefaultSpeed() {
        val e = GameEngine(straightStart(), Difficulty.PRO, AssistMode.SOFTWARE)
        e.start()
        repeat(9) { e.update(1f / 60f, 2f, 0f) } // 0,15 s bei ~12° Neigung
        assertTrue("vx = ${e.vx}", e.vx > 2f)
    }

    @Test
    fun speedSetting_scalesAcceleration() {
        val maze = straightStart()
        fun vAfter(speed: Float): Float {
            val e = GameEngine(maze, Difficulty.PRO, AssistMode.SOFTWARE, speed)
            e.start()
            repeat(6) { e.update(1f / 60f, 2f, 0f) } // 0,1 s, noch vor der Wand
            return e.vx
        }
        val normal = vAfter(1f)
        assertEquals(2f, vAfter(2f) / normal, 0.02f)
        assertEquals(0.5f, vAfter(0.5f) / normal, 0.02f)
    }

    @Test
    fun speedFactor_isClampedToMenuRange_andStepMovementStaysSmall() {
        val maze = Maze(14, 28, Random(1))
        assertEquals(2f, GameEngine(maze, Difficulty.PRO, AssistMode.SOFTWARE, 9f).speedFactor, 0f)
        assertEquals(0.5f, GameEngine(maze, Difficulty.PRO, AssistMode.SOFTWARE, 0f).speedFactor, 0f)
        for (d in Difficulty.entries) {
            val e = GameEngine(Maze(d.cols, d.cols * 2, Random(1)), d, AssistMode.SOFTWARE, 2f)
            assertTrue(e.maxSpeed * GameEngine.STEP <= GameEngine.MAX_MOVE_PER_STEP + 1e-6f)
            assertTrue(e.maxSpeed * GameEngine.STEP < GameEngine.BALL_RADIUS / 2f)
        }
    }

    @Test
    fun proLevel_hasZeroAssist_regardlessOfAssistMode() {
        // Bei 0 % muss die Kugel in beiden Hilfe-Arten exakt gleich laufen.
        val maze = Maze(14, 28, Random(8))
        val a = GameEngine(maze, Difficulty.PRO, AssistMode.SOFTWARE)
        val b = GameEngine(maze, Difficulty.PRO, AssistMode.SENSITIVITY)
        a.start(); b.start()
        val rnd = Random(3)
        repeat(1200) {
            val tx = rnd.nextFloat() * 8f - 4f
            val ty = rnd.nextFloat() * 8f - 4f
            a.update(1f / 60f, tx, ty)
            b.update(1f / 60f, tx, ty)
            assertEquals(a.x, b.x, 0f)
            assertEquals(a.y, b.y, 0f)
        }
        assertEquals(GameEngine.BASE_DEAD_ZONE, a.deadZone, 0f)
        assertEquals(a.accelPerTilt, b.accelPerTilt, 0f)
    }

    @Test
    fun sensitivityMode_reactsWeaker_thanZeroAssist() {
        val maze = (0 until 100).map { Maze(9, 14, Random(it)) }.first { it.isOpen(0, 0, Maze.RIGHT) }
        fun speedAfter(mode: AssistMode, d: Difficulty): Float {
            val e = GameEngine(maze, d, mode)
            e.start()
            repeat(4) { e.update(1f / 60f, 3f, 0f) }
            return e.vx
        }
        val pro = speedAfter(AssistMode.SENSITIVITY, Difficulty.PRO)
        val easy = speedAfter(AssistMode.SENSITIVITY, Difficulty.EASY)
        val medium = speedAfter(AssistMode.SENSITIVITY, Difficulty.MEDIUM)
        val hard = speedAfter(AssistMode.SENSITIVITY, Difficulty.HARD)
        assertTrue("leicht $easy < mittel $medium < schwer $hard < profi $pro", easy < medium && medium < hard && hard < pro)
        // Software-Hilfe verändert die Empfindlichkeit nicht
        val software = speedAfter(AssistMode.SOFTWARE, Difficulty.EASY)
        assertTrue("software $software >= profi $pro", software >= pro - 1e-4f)
    }

    @Test
    fun softwareMode_redirectsTiltIntoOpenCorridor() {
        val maze = straightStart()
        fun progress(d: Difficulty): Float {
            val e = GameEngine(maze, d, AssistMode.SOFTWARE)
            e.start()
            repeat(10) { e.update(1f / 60f, 1.5f, -3f) } // schräg gegen die obere Außenwand
            return e.x
        }
        assertTrue(progress(Difficulty.EASY) > progress(Difficulty.PRO))
    }

    @Test
    fun finishedFlag_isReportedExactlyOnce_andStateFreezes() {
        val e = GameEngine(Maze(7, 10, Random(5)), Difficulty.EASY, AssistMode.SOFTWARE)
        play(e)
        assertEquals(GameEngine.State.FINISHED, e.state)
        val t = e.elapsed
        assertFalse(e.update(1f, 3f, 3f))
        assertEquals(t, e.elapsed, 0f)
    }

    @Test
    fun randomMaxTilt_atMaxSpeed_withLagSpikes_neverPenetratesWalls() {
        val rnd = Random(11)
        for (seed in 0 until 5) {
            val e = GameEngine(mazeFor(Difficulty.PRO, seed), Difficulty.PRO, AssistMode.SOFTWARE, 2f)
            e.start()
            repeat(4000) {
                val dt = if (rnd.nextInt(10) == 0) 0.2f else 1f / 120f
                e.update(dt, (rnd.nextFloat() * 2 - 1) * 8f, (rnd.nextFloat() * 2 - 1) * 8f)
                assertNotInWall(e)
                if (e.state == GameEngine.State.FINISHED) { e.reset(); e.start() }
            }
        }
    }

    @Test
    fun reset_restoresStartState() {
        val e = GameEngine(Maze(7, 10, Random(9)), Difficulty.EASY, AssistMode.SENSITIVITY)
        e.start()
        repeat(60) { e.update(1f / 60f, 4f, 4f) }
        e.reset()
        assertEquals(GameEngine.State.READY, e.state)
        assertEquals(0f, e.elapsed, 0f)
        assertEquals(0.5f, e.renderX, 0f)
        assertFalse(e.update(1f / 60f, 4f, 4f)) // READY: keine Bewegung bis start()
        assertEquals(0.5f, e.x, 0f)
    }

    @Test
    fun ballSpeed_menuValues() {
        assertEquals(50, BallSpeed.clamp(10))
        assertEquals(200, BallSpeed.clamp(999))
        assertEquals(130, BallSpeed.clamp(127))
        assertEquals(100, BallSpeed.clamp(BallSpeed.DEFAULT_PERCENT))
        assertEquals(1f, BallSpeed.factor(100), 0f)
    }

    @Test
    fun benchmark_physicsIsCheap() {
        val e = GameEngine(mazeFor(Difficulty.HARD, 1), Difficulty.HARD, AssistMode.SOFTWARE, 2f)
        e.start()
        val rnd = Random(1)
        fun run(n: Int) = repeat(n) {
            e.update(1f / 60f, rnd.nextFloat() * 8 - 4, rnd.nextFloat() * 8 - 4)
            if (e.state != GameEngine.State.RUNNING) { e.reset(); e.start() }
        }
        run(20_000)
        val frames = 60_000
        val t0 = System.nanoTime()
        run(frames)
        val usPerFrame = (System.nanoTime() - t0) / 1000.0 / frames
        println("Physik: %.2f us pro 60-Hz-Frame (8 Schritte)".format(usPerFrame))
        assertTrue(usPerFrame < 200.0)
    }
}
