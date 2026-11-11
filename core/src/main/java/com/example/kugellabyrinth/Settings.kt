package com.example.kugellabyrinth

/**
 * Schwierigkeit:
 *  - [cols]: Labyrinthbreite auf dem Handy. Mehr Spalten = schmalere Gänge auf gleichem Bildschirm.
 *  - [watchCols]: Seitenlänge des quadratischen Labyrinths auf der Uhr (watchCols × watchCols Felder).
 *  - [assistStrength]: Stärke der gewählten Unterstützung (1.0 = maximal, 0.0 = keine Hilfe).
 *
 * Bewusst frei von Android-Abhängigkeiten, damit Engine und Tests auf der JVM laufen.
 */
enum class Difficulty(val cols: Int, val watchCols: Int, val assistStrength: Float) {
    EASY(8, 5, 1.0f),
    MEDIUM(10, 6, 0.6f),
    HARD(13, 7, 0.3f),
    PRO(14, 8, 0.0f);

    /** false = 0 % Hilfe; die Art der Unterstützung spielt dann keine Rolle. */
    val hasAssist: Boolean get() = assistStrength > 0f

    companion object {
        fun fromName(name: String?, fallback: Difficulty = MEDIUM): Difficulty =
            entries.firstOrNull { it.name == name } ?: fallback
    }
}

/**
 * Art der Unterstützung (wirkt mit der Stärke der Schwierigkeit):
 *  - SENSITIVITY: Nur der Sensor-Eingang wird abgeschwächt (geringere Verstärkung, größere Totzone).
 *  - SOFTWARE:    Sensor bleibt unverändert; die Software unterstützt aktiv
 *                 (Kippen wird in den passenden offenen Gang gelenkt, Gangmitte wird gehalten,
 *                 Abprallen wird gedämpft). Ohne Kippen bewegt sich nichts.
 */
enum class AssistMode {
    SENSITIVITY,
    SOFTWARE;

    companion object {
        fun fromName(name: String?, fallback: AssistMode = SOFTWARE): AssistMode =
            entries.firstOrNull { it.name == name } ?: fallback
    }
}

/** Einstellbare Kugelgeschwindigkeit in Prozent (100 % = Standard). */
object BallSpeed {
    const val MIN_PERCENT = 50
    const val MAX_PERCENT = 200
    const val STEP_PERCENT = 10
    const val DEFAULT_PERCENT = 100

    /** Auf den gültigen Bereich und das 10-%-Raster bringen. */
    fun clamp(percent: Int): Int {
        val p = percent.coerceIn(MIN_PERCENT, MAX_PERCENT)
        return MIN_PERCENT + (p - MIN_PERCENT + STEP_PERCENT / 2) / STEP_PERCENT * STEP_PERCENT
    }

    fun factor(percent: Int): Float = clamp(percent) / 100f
}
