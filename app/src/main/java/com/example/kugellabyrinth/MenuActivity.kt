package com.example.kugellabyrinth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView

/**
 * Menü: Schwierigkeit (inkl. 0 % Hilfe), Art der Unterstützung und Kugelgeschwindigkeit.
 * Die Auswahl wird gespeichert.
 */
class MenuActivity : Activity() {

    private lateinit var groupDifficulty: RadioGroup
    private lateinit var groupAssist: RadioGroup
    private lateinit var seekSpeed: SeekBar
    private lateinit var textSpeed: TextView
    private lateinit var textInfo: TextView
    private lateinit var textBest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        groupDifficulty = findViewById(R.id.groupDifficulty)
        groupAssist = findViewById(R.id.groupAssist)
        seekSpeed = findViewById(R.id.seekSpeed)
        textSpeed = findViewById(R.id.textSpeed)
        textInfo = findViewById(R.id.textInfo)
        textBest = findViewById(R.id.textBest)

        // Gespeicherte Auswahl wiederherstellen, bevor die Listener gesetzt werden
        groupDifficulty.check(
            when (GamePrefs.difficulty(this)) {
                Difficulty.EASY -> R.id.rbEasy
                Difficulty.MEDIUM -> R.id.rbMedium
                Difficulty.HARD -> R.id.rbHard
                Difficulty.PRO -> R.id.rbPro
            }
        )
        groupAssist.check(
            when (GamePrefs.assist(this)) {
                AssistMode.SENSITIVITY -> R.id.rbSensitivity
                AssistMode.SOFTWARE -> R.id.rbSoftware
            }
        )
        seekSpeed.max = (BallSpeed.MAX_PERCENT - BallSpeed.MIN_PERCENT) / BallSpeed.STEP_PERCENT
        seekSpeed.progress = (GamePrefs.speedPercent(this) - BallSpeed.MIN_PERCENT) / BallSpeed.STEP_PERCENT

        val radioListener = RadioGroup.OnCheckedChangeListener { _, _ -> onSelectionChanged() }
        groupDifficulty.setOnCheckedChangeListener(radioListener)
        groupAssist.setOnCheckedChangeListener(radioListener)
        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onSelectionChanged()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi() // Bestzeit kann sich nach einem Spiel geändert haben
    }

    private fun selectedDifficulty(): Difficulty = when (groupDifficulty.checkedRadioButtonId) {
        R.id.rbEasy -> Difficulty.EASY
        R.id.rbHard -> Difficulty.HARD
        R.id.rbPro -> Difficulty.PRO
        else -> Difficulty.MEDIUM
    }

    private fun selectedAssist(): AssistMode = when (groupAssist.checkedRadioButtonId) {
        R.id.rbSensitivity -> AssistMode.SENSITIVITY
        else -> AssistMode.SOFTWARE
    }

    private fun selectedSpeedPercent(): Int =
        BallSpeed.clamp(BallSpeed.MIN_PERCENT + seekSpeed.progress * BallSpeed.STEP_PERCENT)

    private fun onSelectionChanged() {
        GamePrefs.save(this, selectedDifficulty(), selectedAssist(), selectedSpeedPercent())
        updateUi()
    }

    private fun updateUi() {
        val d = selectedDifficulty()
        val a = selectedAssist()
        val speed = selectedSpeedPercent()

        // Bei 0 % Hilfe spielt die Art der Unterstützung keine Rolle -> ausgegraut
        val assistEnabled = d.hasAssist
        groupAssist.alpha = if (assistEnabled) 1f else 0.4f
        for (i in 0 until groupAssist.childCount) groupAssist.getChildAt(i).isEnabled = assistEnabled

        textSpeed.text = if (speed == BallSpeed.DEFAULT_PERCENT) {
            getString(R.string.speed_value_default, speed)
        } else {
            getString(R.string.speed_value, speed)
        }

        val strength = (d.assistStrength * 100).toInt()
        textInfo.text = when {
            !d.hasAssist -> getString(R.string.info_pro, d.cols)
            a == AssistMode.SENSITIVITY -> getString(R.string.info_sensitivity, strength)
            else -> getString(R.string.info_software, strength)
        }

        val best = GamePrefs.bestTime(this, d, a, speed)
        textBest.text = if (best != null) {
            getString(R.string.best_time, formatSeconds(best))
        } else {
            getString(R.string.best_time_none)
        }
    }
}
