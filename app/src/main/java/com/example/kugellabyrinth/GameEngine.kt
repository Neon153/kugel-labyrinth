package com.example.kugellabyrinth

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Spiellogik ohne Android-Abhängigkeiten (auf der JVM testbar).
 *
 * Physik in Zelleneinheiten mit festem Zeitschritt [STEP] (480 Hz), unabhängig von der
 * Bildwiederholrate. Für ruckelfreie Darstellung liefert die Engine eine zwischen zwei
 * Physik-Schritten interpolierte Position ([renderX], [renderY]).
 *
 * Beschleunigung und Höchstgeschwindigkeit skalieren mit
 *  - der Labyrinthbreite (Kugel ist auf dem Bildschirm in jeder Stufe gleich schnell) und
 *  - [speedFactor] (Einstellung „Kugelgeschwindigkeit“ im Menü, 0,5 … 2,0).
 *
 * Eingabe: Neigung in m/s² auf Bildschirmachsen (x nach rechts, y nach unten), bereits kalibriert.
 */
class GameEngine(
    val maze: Maze,
    val difficulty: Difficulty,
    val assist: AssistMode,
    speedFactor: Float = 1f
) {
    enum class State { READY, RUNNING, FINISHED }

    var state = State.READY
        private set

    /** Position und Geschwindigkeit der Kugel (Zelleneinheiten). */
    var x = 0f
        private set
    var y = 0f
        private set
    var vx = 0f
        private set
    var vy = 0f
        private set

    private var prevX = 0f
    private var prevY = 0f
    private var accumulator = 0f
    private var steps = 0

    /** Gespielte Zeit in Sekunden (exakt: Anzahl Physik-Schritte * STEP). */
    val elapsed: Float get() = steps * STEP

    /** Interpolierte Position für die Darstellung. */
    val renderX: Float get() = prevX + (x - prevX) * (accumulator / STEP)
    val renderY: Float get() = prevY + (y - prevY) * (accumulator / STEP)

    // --- aus Schwierigkeit/Modus/Tempo einmalig abgeleitete Parameter ---
    val speedFactor = speedFactor.coerceIn(BallSpeed.MIN_PERCENT / 100f, BallSpeed.MAX_PERCENT / 100f)
    private val scale = maze.cols / REFERENCE_COLS * this.speedFactor
    private val strength = difficulty.assistStrength
    private val softwareAssist = assist == AssistMode.SOFTWARE && strength > 0f
    private val sensitivityAssist = assist == AssistMode.SENSITIVITY && strength > 0f

    /** Totzone in m/s². Nur bei Empfindlichkeits-Hilfe vergrößert. */
    val deadZone = if (sensitivityAssist) BASE_DEAD_ZONE + 0.5f * strength else BASE_DEAD_ZONE

    /** Beschleunigung (Zellen/s²) pro m/s² Neigung. Nur bei Empfindlichkeits-Hilfe verringert. */
    val accelPerTilt = (if (sensitivityAssist) 1f - 0.5f * strength else 1f) * TILT_TO_ACCEL * scale

    /** Höchstgeschwindigkeit (Zellen/s), begrenzt auf [MAX_MOVE_PER_STEP] pro Schritt -> kein Tunneln. */
    val maxSpeed = min(MAX_SPEED * scale, MAX_MOVE_PER_STEP / STEP)

    /** Software-Hilfe dämpft das Abprallen entsprechend ihrer Stärke. */
    private val restitution = if (softwareAssist) WALL_BOUNCE * (1f - strength) else WALL_BOUNCE

    private val goalX = maze.goalCol + 0.5f
    private val goalY = maze.goalRow + 0.5f

    init {
        require(BALL_RADIUS <= Maze.INDEX_REACH)
        reset()
    }

    fun reset() {
        x = maze.startCol + 0.5f
        y = maze.startRow + 0.5f
        prevX = x
        prevY = y
        vx = 0f
        vy = 0f
        accumulator = 0f
        steps = 0
        state = State.READY
    }

    fun start() {
        if (state == State.READY) state = State.RUNNING
    }

    /**
     * Simuliert [dt] Sekunden. Gibt genau einmal `true` zurück – in dem Aufruf, in dem das Ziel erreicht wird.
     */
    fun update(dt: Float, tiltX: Float, tiltY: Float): Boolean {
        if (state != State.RUNNING) return false
        accumulator += dt.coerceIn(0f, MAX_FRAME_DT)
        val tx = shape(tiltX)
        val ty = shape(tiltY)
        while (accumulator >= STEP) {
            accumulator -= STEP
            prevX = x
            prevY = y
            steps++
            if (step(tx, ty)) {
                accumulator = 0f
                prevX = x
                prevY = y
                state = State.FINISHED
                return true
            }
        }
        return false
    }

    /** Totzone abziehen und begrenzen. */
    private fun shape(v: Float): Float {
        val a = abs(v) - deadZone
        if (a <= 0f) return 0f
        val m = min(a, MAX_TILT)
        return if (v < 0f) -m else m
    }

    /** Ein Physik-Schritt. Gibt `true` zurück, wenn das Ziel erreicht wurde. */
    private fun step(tx: Float, ty: Float): Boolean {
        var ax = tx * accelPerTilt
        var ay = ty * accelPerTilt
        var dampX = DAMP_PER_STEP
        var dampY = DAMP_PER_STEP

        if (softwareAssist && (tx != 0f || ty != 0f)) {
            val cx = floor(x).toInt().coerceIn(0, maze.cols - 1)
            val cy = floor(y).toInt().coerceIn(0, maze.rows - 1)
            val mask = maze.openMask(cx, cy)

            // Offener Gang, der am besten zur Kipprichtung des Spielers passt
            var bestDir = -1
            var bestDot = 0f
            for (d in 0..3) {
                if ((mask shr d) and 1 == 0) continue
                val dot = tx * Maze.DX[d] + ty * Maze.DY[d]
                if (dot > bestDot) {
                    bestDot = dot
                    bestDir = d
                }
            }

            if (bestDir >= 0) {
                val mag = sqrt(tx * tx + ty * ty)
                val gate = min(1f, mag / GATE_TILT)

                // (a) Kipp-Eingabe in den gewählten Gang umlenken (skaliert mit der Kippstärke)
                val dx = cx + 0.5f + Maze.DX[bestDir] - x
                val dy = cy + 0.5f + Maze.DY[bestDir] - y
                val len = sqrt(dx * dx + dy * dy)
                if (len > 1e-4f) {
                    val push = strength * REDIRECT * mag * accelPerTilt / len
                    ax += dx * push
                    ay += dy * push
                }

                // (b) Gangmitte halten und Querbewegung dämpfen
                val center = strength * CENTER_ACCEL * scale * gate
                val lateral = 1f - strength * LATERAL_DAMP * gate * STEP
                if (Maze.DX[bestDir] != 0) {
                    ay += (cy + 0.5f - y) * center
                    dampY *= lateral
                } else {
                    ax += (cx + 0.5f - x) * center
                    dampX *= lateral
                }
            }
        }

        // Integration (semi-implizites Euler-Verfahren)
        vx = (vx + ax * STEP) * dampX
        vy = (vy + ay * STEP) * dampY
        val speed2 = vx * vx + vy * vy
        if (speed2 > maxSpeed * maxSpeed) {
            val k = maxSpeed / sqrt(speed2)
            vx *= k
            vy *= k
        }
        x += vx * STEP
        y += vy * STEP

        // Zwei Durchläufe, damit die Kugel in Ecken sauber aufliegt
        collide()
        collide()

        val gx = x - goalX
        val gy = y - goalY
        if (gx * gx + gy * gy < GOAL_RADIUS * GOAL_RADIUS) {
            x = goalX
            y = goalY
            vx = 0f
            vy = 0f
            return true
        }
        return false
    }

    /** Kreis-gegen-Rechteck-Kollision, nur gegen die Wände der aktuellen Zelle (vorindiziert). */
    private fun collide() {
        val cx = floor(x).toInt().coerceIn(0, maze.cols - 1)
        val cy = floor(y).toInt().coerceIn(0, maze.rows - 1)
        val cell = cy * maze.cols + cx
        val w = maze.walls
        val list = maze.cellWalls
        val rr = BALL_RADIUS
        for (k in maze.cellWallStart[cell] until maze.cellWallStart[cell + 1]) {
            val i = list[k] * 4
            val l = w[i]
            val t = w[i + 1]
            val r = w[i + 2]
            val b = w[i + 3]
            val nx = if (x < l) l else if (x > r) r else x
            val ny = if (y < t) t else if (y > b) b else y
            val dx = x - nx
            val dy = y - ny
            val d2 = dx * dx + dy * dy
            if (d2 >= rr * rr) continue

            val normalX: Float
            val normalY: Float
            val push: Float
            if (d2 > 1e-12f) {
                val d = sqrt(d2)
                normalX = dx / d
                normalY = dy / d
                push = rr - d
            } else {
                // Mittelpunkt im Rechteck: über die nächstgelegene Kante hinausschieben
                val toL = x - l
                val toR = r - x
                val toT = y - t
                val toB = b - y
                val m = min(min(toL, toR), min(toT, toB))
                when (m) {
                    toL -> { normalX = -1f; normalY = 0f; push = toL + rr }
                    toR -> { normalX = 1f; normalY = 0f; push = toR + rr }
                    toT -> { normalX = 0f; normalY = -1f; push = toT + rr }
                    else -> { normalX = 0f; normalY = 1f; push = toB + rr }
                }
            }
            x += normalX * push
            y += normalY * push
            val vn = vx * normalX + vy * normalY
            if (vn < 0f) {
                val j = (1f + restitution) * vn
                vx -= j * normalX
                vy -= j * normalY
            }
        }
    }

    companion object {
        /** 480 Hz: auch bei 200 % Tempo bewegt sich die Kugel höchstens ~0,12 Zellen pro Schritt. */
        const val STEP = 1f / 480f
        const val BALL_RADIUS = 0.28f
        const val GOAL_RADIUS = 0.30f

        /** Größter Zeitsprung pro Frame (verhindert "Spiral of Death" nach Rucklern). */
        const val MAX_FRAME_DT = 0.05f

        /** Bezugsbreite: bei dieser Spaltenzahl und 100 % Tempo gelten die Werte unten unverändert. */
        const val REFERENCE_COLS = 9f

        const val TILT_TO_ACCEL = 8f      // Zellen/s² pro m/s² Neigung
        const val MAX_TILT = 6f           // m/s² (ca. 37°)
        const val BASE_DEAD_ZONE = 0.15f  // m/s²
        const val BASE_DAMP = 0.5f        // 1/s, Rollreibung
        const val MAX_SPEED = 18f         // Zellen/s
        const val MAX_MOVE_PER_STEP = 0.15f // Zellen; deutlich unter Kugelradius -> kein Tunneln
        const val WALL_BOUNCE = 0.35f

        // Software-Hilfe (jeweils mit der Unterstützungsstärke skaliert)
        const val REDIRECT = 0.9f         // Anteil der Kippstärke, der in den Gang umgelenkt wird
        const val GATE_TILT = 2.5f        // m/s², ab dieser Neigung volle Zentrier-Hilfe
        const val CENTER_ACCEL = 25f      // 1/s²
        const val LATERAL_DAMP = 8f       // 1/s

        private val DAMP_PER_STEP = exp(-BASE_DAMP * STEP)
    }
}
