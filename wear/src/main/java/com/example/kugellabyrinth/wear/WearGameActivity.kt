package com.example.kugellabyrinth.wear

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.kugellabyrinth.AssistMode
import com.example.kugellabyrinth.BallSpeed
import com.example.kugellabyrinth.Difficulty
import com.example.kugellabyrinth.GamePrefs
import com.example.kugellabyrinth.GameView
import com.example.kugellabyrinth.TiltInput
import com.example.kugellabyrinth.core.R as CoreR
import com.example.kugellabyrinth.formatSeconds
import com.example.kugellabyrinth.labelRes
import com.example.kugellabyrinth.tenthsOf

/**
 * Spielbildschirm der Uhr.
 *
 * Steuerung über die Neigung des Handgelenks: Die Kugel rollt immer zur tieferen Seite des Displays.
 * Unterarm drehen (Pronation/Supination) neigt das Display um die Unterarmachse, Hand heben/senken
 * neigt es um die Querachse. Physik, Sensor und Kalibrierung sind dieselben wie in der Handy-App.
 *
 * Bedienung: Antippen = Start (Kalibrierung) bzw. nach dem Ziel neues Labyrinth;
 * lang drücken = Neustart im selben Labyrinth; nach rechts wischen = zurück zum Menü.
 */
class WearGameActivity : Activity(), GameView.Listener {

    private lateinit var game: GameView
    private lateinit var textTimer: TextView
    private lateinit var tilt: TiltInput
    private lateinit var difficulty: Difficulty
    private lateinit var assist: AssistMode
    private var speedPercent = BallSpeed.DEFAULT_PERCENT
    private var shownTenths = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_game)

        difficulty = GamePrefs.difficulty(this)
        assist = GamePrefs.assist(this)
        speedPercent = GamePrefs.speedPercent(this)
        tilt = TiltInput(this)

        textTimer = findViewById(R.id.textTimer)
        findViewById<TextView>(R.id.textMode).text =
            getString(R.string.mode_short, getString(difficulty.labelRes()), speedPercent)

        game = findViewById(R.id.gameView)
        game.shape = GameView.Shape.ROUND
        game.setTextSizes(TITLE_SP, BODY_SP)
        game.readyBody = getString(R.string.ready_body)
        game.difficulty = difficulty
        game.assist = assist
        game.speedFactor = BallSpeed.factor(speedPercent)
        game.tiltSource = tilt
        game.listener = this
        game.setOnLongClickListener {
            game.restart()
            true
        }

        if (!tilt.isAvailable) {
            Toast.makeText(this, CoreR.string.no_sensor, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        tilt.start()
        game.onHostResume()
    }

    override fun onPause() {
        game.onHostPause()
        tilt.stop()
        super.onPause()
    }

    // --- GameView.Listener ---

    override fun onStartRequested() {
        tilt.calibrate()
    }

    override fun onTimeChanged(seconds: Float) {
        val tenths = tenthsOf(seconds)
        if (tenths != shownTenths) {
            shownTenths = tenths
            textTimer.text = formatSeconds(seconds)
        }
    }

    override fun onFinished(seconds: Float) {
        if (isFinishing || isDestroyed) return
        val record = GamePrefs.submitTime(this, difficulty, assist, speedPercent, seconds)
        val best = GamePrefs.bestTime(this, difficulty, assist, speedPercent) ?: seconds
        val timeText = formatSeconds(seconds)
        val bestText = formatSeconds(best)
        val result = getString(R.string.result_body, timeText, bestText)
        val hint = getString(R.string.result_hint)
        val body = if (record) {
            getString(R.string.result_record) + "\n" + result + "\n\n" + hint
        } else {
            result + "\n\n" + hint
        }
        game.showResult(getString(R.string.result_title), body)
    }

    override fun onFinishedTapped() {
        game.newMaze()
    }

    private companion object {
        /** Schriftgrößen der Overlays auf dem kleinen Display (Handy: 28 / 16 sp). */
        const val TITLE_SP = 18f
        const val BODY_SP = 12f
    }
}
