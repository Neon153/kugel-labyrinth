package com.example.kugellabyrinth

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class FormatTest {
    @Test
    fun formatsWithGermanDecimalComma() {
        assertEquals("0,0 s", formatSeconds(0f))
        assertEquals("12,3 s", formatSeconds(12.34f))
        assertEquals("59,9 s", formatSeconds(59.99f))
        assertEquals("123,0 s", formatSeconds(123f))
    }

    @Test
    fun physicsStepTimes_doNotLoseATenth() {
        val perTenth = (0.1f / GameEngine.STEP).roundToInt()
        // 0,3 s an Schritten darf nicht als 0,2 s erscheinen
        assertEquals("0,3 s", formatSeconds(3 * perTenth * GameEngine.STEP))
        // jede Schrittanzahl bis 100 s
        for (steps in 0..1000 * perTenth) {
            assertEquals(steps / perTenth, tenthsOf(steps * GameEngine.STEP))
        }
    }
}
