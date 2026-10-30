package com.example.kugellabyrinth

/**
 * Zehntelsekunden (abgeschnitten wie bei einer Stoppuhr). Das kleine Epsilon verhindert, dass
 * z. B. 0,3 s wegen Float-Rundung (2,9999998 Zehntel) als 0,2 s angezeigt wird.
 */
fun tenthsOf(seconds: Float): Int = (seconds * 10f + 1e-3f).toInt()

/** "12,3 s" ohne String.format (kein Formatter/Regex pro Aufruf). */
fun formatSeconds(seconds: Float): String {
    val t = tenthsOf(seconds)
    return "${t / 10},${t % 10} s"
}
