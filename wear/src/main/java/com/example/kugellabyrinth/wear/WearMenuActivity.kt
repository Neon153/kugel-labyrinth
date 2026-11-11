package com.example.kugellabyrinth.wear

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.example.kugellabyrinth.AssistMode
import com.example.kugellabyrinth.BallSpeed
import com.example.kugellabyrinth.Difficulty
import com.example.kugellabyrinth.GamePrefs
import com.example.kugellabyrinth.core.R as CoreR
import com.example.kugellabyrinth.formatSeconds
import com.example.kugellabyrinth.labelRes

/**
 * Menü der Uhr. Statt Auswahllisten (zu klein fürs Uhrdisplay) schalten Chips per Antippen
 * durch die Werte; das Tempo wird mit −/+ in 10-%-Schritten verstellt.
 * Die Krone scrollt das Menü (fokussierte ScrollView).
 */
class WearMenuActivity : Activity() {

    private lateinit var scroll: ScrollView
    private lateinit var buttonDifficulty: Button
    private lateinit var buttonAssist: Button
    private lateinit var textSpeed: TextView
    private lateinit var textMazeSize: TextView
    private lateinit var textBest: TextView

    private var difficulty = Difficulty.MEDIUM
    private var assist = AssistMode.SOFTWARE
    private var speedPercent = BallSpeed.DEFAULT_PERCENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear_menu)

        scroll = findViewById(R.id.scroll)
        buttonDifficulty = findViewById(R.id.buttonDifficulty)
        buttonAssist = findViewById(R.id.buttonAssist)
        textSpeed = findViewById(R.id.textSpeed)
        textMazeSize = findViewById(R.id.textMazeSize)
        textBest = findViewById(R.id.textBest)

        difficulty = GamePrefs.difficulty(this)
        assist = GamePrefs.assist(this)
        speedPercent = GamePrefs.speedPercent(this)

        buttonDifficulty.setOnClickListener {
            val all = Difficulty.entries
            difficulty = all[(difficulty.ordinal + 1) % all.size]
            onSelectionChanged()
        }
        buttonAssist.setOnClickListener {
            assist = if (assist == AssistMode.SOFTWARE) AssistMode.SENSITIVITY else AssistMode.SOFTWARE
            onSelectionChanged()
        }
        findViewById<Button>(R.id.buttonSlower).setOnClickListener {
            speedPercent = BallSpeed.clamp(speedPercent - BallSpeed.STEP_PERCENT)
            onSelectionChanged()
        }
        findViewById<Button>(R.id.buttonFaster).setOnClickListener {
            speedPercent = BallSpeed.clamp(speedPercent + BallSpeed.STEP_PERCENT)
            onSelectionChanged()
        }
        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            startActivity(Intent(this, WearGameActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi() // Bestzeit kann sich nach einem Spiel geändert haben
        scroll.requestFocus() // Krone scrollt nur die fokussierte View
    }

    private fun onSelectionChanged() {
        GamePrefs.save(this, difficulty, assist, speedPercent)
        updateUi()
    }

    private fun updateUi() {
        val strength = (difficulty.assistStrength * 100).toInt()
        buttonDifficulty.text = getString(R.string.menu_difficulty, getString(difficulty.labelRes()), strength)

        // Bei 0 % Hilfe spielt die Hilfe-Art keine Rolle -> ausgegraut
        val assistEnabled = difficulty.hasAssist
        buttonAssist.isEnabled = assistEnabled
        buttonAssist.alpha = if (assistEnabled) 1f else 0.4f
        val assistLabel = getString(if (assistEnabled) assist.labelRes() else CoreR.string.assist_none)
        buttonAssist.text = getString(R.string.menu_assist, assistLabel)

        textSpeed.text = getString(R.string.menu_speed, speedPercent)
        textMazeSize.text = getString(R.string.menu_maze_size, difficulty.watchCols, difficulty.watchCols)

        val best = GamePrefs.bestTime(this, difficulty, assist, speedPercent)
        textBest.text = if (best != null) {
            getString(R.string.menu_best, formatSeconds(best))
        } else {
            getString(R.string.menu_best_none)
        }
    }
}
