package com.example.kugellabyrinth

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.kugellabyrinth.core.R as CoreR

/** Spielbildschirm: verbindet Sensor, GameView und Ergebnisdialog. */
class GameActivity : Activity(), GameView.Listener {

    private lateinit var game: GameView
    private lateinit var textTimer: TextView
    private lateinit var tilt: TiltInput
    private lateinit var difficulty: Difficulty
    private lateinit var assist: AssistMode
    private var speedPercent = BallSpeed.DEFAULT_PERCENT

    private var resultDialog: AlertDialog? = null
    private var shownTenths = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        difficulty = GamePrefs.difficulty(this)
        assist = GamePrefs.assist(this)
        speedPercent = GamePrefs.speedPercent(this)
        tilt = TiltInput(this)

        textTimer = findViewById(R.id.textTimer)
        val difficultyLabel = getString(difficulty.labelRes())
        val assistLabel = getString(if (difficulty.hasAssist) assist.labelRes() else CoreR.string.assist_none)
        findViewById<TextView>(R.id.textMode).text =
            getString(R.string.mode_format, difficultyLabel, assistLabel, speedPercent)

        game = findViewById(R.id.gameView)
        game.readyBody = getString(R.string.ready_body)
        game.difficulty = difficulty
        game.assist = assist
        game.speedFactor = BallSpeed.factor(speedPercent)
        game.tiltSource = tilt
        game.listener = this

        findViewById<Button>(R.id.buttonRestart).setOnClickListener { game.restart() }
        findViewById<Button>(R.id.buttonMenu).setOnClickListener { finish() }

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

    override fun onDestroy() {
        resultDialog?.dismiss()
        resultDialog = null
        super.onDestroy()
    }

    // --- GameView.Listener ---

    override fun onStartRequested() {
        tilt.calibrate()
    }

    override fun onTimeChanged(seconds: Float) {
        // Nur bei Änderung der Zehntelsekunde neu setzen (TextView hat feste Breite -> kein Relayout)
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
        var message = getString(R.string.result_message, timeText, bestText)
        if (record) message += "\n" + getString(R.string.result_record)

        resultDialog?.dismiss()
        resultDialog = AlertDialog.Builder(this)
            .setTitle(R.string.result_title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.action_new_maze) { _, _ -> game.newMaze() }
            .setNeutralButton(R.string.action_retry) { _, _ -> game.restart() }
            .setNegativeButton(R.string.action_menu) { _, _ -> finish() }
            .show()
    }
}
