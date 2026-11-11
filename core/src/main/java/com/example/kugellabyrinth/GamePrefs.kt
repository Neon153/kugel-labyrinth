package com.example.kugellabyrinth

import android.content.Context
import android.content.SharedPreferences
import com.example.kugellabyrinth.core.R

/** Gespeicherte Einstellungen und Bestzeiten (Handy und Uhr speichern jeweils lokal auf dem Gerät). */
object GamePrefs {
    private const val FILE = "kugel_labyrinth"
    private const val KEY_DIFFICULTY = "difficulty"
    private const val KEY_ASSIST = "assist"
    private const val KEY_SPEED = "speed_percent"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun difficulty(context: Context): Difficulty =
        Difficulty.fromName(prefs(context).getString(KEY_DIFFICULTY, null))

    fun assist(context: Context): AssistMode =
        AssistMode.fromName(prefs(context).getString(KEY_ASSIST, null))

    fun speedPercent(context: Context): Int =
        BallSpeed.clamp(prefs(context).getInt(KEY_SPEED, BallSpeed.DEFAULT_PERCENT))

    fun save(context: Context, difficulty: Difficulty, assist: AssistMode, speedPercent: Int) {
        prefs(context).edit()
            .putString(KEY_DIFFICULTY, difficulty.name)
            .putString(KEY_ASSIST, assist.name)
            .putInt(KEY_SPEED, BallSpeed.clamp(speedPercent))
            .apply()
    }

    /** Bestzeit in Sekunden oder `null`, falls es noch keine gibt. */
    fun bestTime(context: Context, difficulty: Difficulty, assist: AssistMode, speedPercent: Int): Float? {
        val t = prefs(context).getFloat(bestKey(difficulty, assist, speedPercent), -1f)
        return if (t > 0f) t else null
    }

    /** Speichert [time], falls sie besser ist. Gibt `true` bei neuem Rekord zurück. */
    fun submitTime(context: Context, difficulty: Difficulty, assist: AssistMode, speedPercent: Int, time: Float): Boolean {
        val old = bestTime(context, difficulty, assist, speedPercent)
        if (old != null && old <= time) return false
        prefs(context).edit().putFloat(bestKey(difficulty, assist, speedPercent), time).apply()
        return true
    }

    /** Bestzeiten getrennt nach Stufe, Hilfe-Art und Tempo. Bei 0 % Hilfe ist die Hilfe-Art egal. */
    private fun bestKey(d: Difficulty, a: AssistMode, speedPercent: Int): String {
        val assistPart = if (d.hasAssist) a.name else "NONE"
        return "best_${d.name}_${assistPart}_${BallSpeed.clamp(speedPercent)}"
    }
}

/** Anzeigetexte der Enums (Android-Ressourcen, daher nicht in Settings.kt). */
fun Difficulty.labelRes(): Int = when (this) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
    Difficulty.PRO -> R.string.difficulty_pro
}

fun AssistMode.labelRes(): Int = when (this) {
    AssistMode.SENSITIVITY -> R.string.assist_sensitivity
    AssistMode.SOFTWARE -> R.string.assist_software
}
