package com.example.kugellabyrinth

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Display
import android.view.Surface
import kotlin.math.sqrt

/**
 * Liefert die Neigung des Geräts in m/s² auf Bildschirmachsen (x nach rechts, y nach unten).
 *
 *  - Bevorzugt TYPE_GRAVITY (vom System fusioniert, frei von Stößen), sonst TYPE_ACCELEROMETER.
 *  - Berücksichtigt die Display-Rotation (wichtig bei Tablets, deren natürliche Lage Querformat ist).
 *  - Tiefpass zeitbasiert über die Sensor-Zeitstempel -> gleiches Verhalten bei jeder Sensorrate.
 *  - Kalibrierung per Rotationsmatrix: Die beim Start gehaltene Lage wird zur "flachen" Lage gedreht.
 *    Dadurch bleibt die Empfindlichkeit in beiden Achsen korrekt, auch wenn das Handy schräg gehalten
 *    wird (ein reiner Offset würde die Seitwärts-Neigung bei schräger Haltung verfälschen).
 */
class TiltInput(activity: Activity) : SensorEventListener, GameView.TiltSource {

    private val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    private val sensor: Sensor? = sensorManager?.let {
        it.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: it.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    private val display: Display? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay
        }

    val isAvailable: Boolean get() = sensor != null

    override var tiltX = 0f
        private set
    override var tiltY = 0f
        private set

    private var rotation = Surface.ROTATION_0
    private var gx = 0f
    private var gy = 0f
    private var gz = 0f
    private var hasSample = false
    private var lastTimestamp = 0L

    // Zeilen 0 und 1 der Kalibrier-Rotation (Zeile 2 wird nicht gebraucht)
    private var r00 = 1f
    private var r01 = 0f
    private var r02 = 0f
    private var r10 = 0f
    private var r11 = 1f
    private var r12 = 0f

    fun start() {
        // Orientierung ist gesperrt -> Rotation einmal lesen statt bei jedem Sensor-Event
        rotation = display?.rotation ?: Surface.ROTATION_0
        hasSample = false
        sensor?.let { sensorManager?.registerListener(this, it, SAMPLING_PERIOD_US) }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    /** Aktuelle Haltung als neutral übernehmen. */
    fun calibrate() {
        val n = sqrt(gx * gx + gy * gy + gz * gz)
        if (!hasSample || n < 1f) {
            resetCalibration()
            return
        }
        val p = gx / n
        val q = gy / n
        val c = gz / n
        if (c < -0.9f) {
            // Display zeigt nach unten -> keine sinnvolle Neutrallage
            resetCalibration()
            return
        }
        // Rodrigues-Rotation, die die Neutrallage (p, q, c) auf (0, 0, 1) dreht
        val k = 1f / (1f + c)
        r00 = 1f - p * p * k; r01 = -p * q * k; r02 = -p
        r10 = -p * q * k;     r11 = 1f - q * q * k; r12 = -q
        updateTilt()
    }

    private fun resetCalibration() {
        r00 = 1f; r01 = 0f; r02 = 0f
        r10 = 0f; r11 = 1f; r12 = 0f
        updateTilt()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val v = event.values
        val sx: Float
        val sy: Float
        when (rotation) {
            Surface.ROTATION_90 -> { sx = -v[1]; sy = v[0] }
            Surface.ROTATION_180 -> { sx = -v[0]; sy = -v[1] }
            Surface.ROTATION_270 -> { sx = v[1]; sy = -v[0] }
            else -> { sx = v[0]; sy = v[1] }
        }
        val sz = v[2]
        if (!hasSample) {
            gx = sx; gy = sy; gz = sz
            hasSample = true
        } else {
            val dt = ((event.timestamp - lastTimestamp) * 1e-9f).coerceIn(0f, 0.1f)
            val a = dt / (FILTER_TAU + dt)
            gx += a * (sx - gx)
            gy += a * (sy - gy)
            gz += a * (sz - gz)
        }
        lastTimestamp = event.timestamp
        updateTilt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updateTilt() {
        // Kippt die rechte Kante nach unten, wird x negativ -> Kugel soll nach rechts (+x).
        // Kippt die obere Kante nach unten, wird y negativ -> Kugel soll nach oben (Bildschirm-y negativ).
        tiltX = -(r00 * gx + r01 * gy + r02 * gz)
        tiltY = r10 * gx + r11 * gy + r12 * gz
    }

    private companion object {
        /** Zeitkonstante des Tiefpasses in Sekunden. */
        const val FILTER_TAU = 0.025f

        /** 100 Hz: flüssiger als SENSOR_DELAY_GAME (~50 Hz), benötigt keine Zusatz-Berechtigung. */
        const val SAMPLING_PERIOD_US = 10_000
    }
}
