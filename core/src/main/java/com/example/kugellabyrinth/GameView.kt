package com.example.kugellabyrinth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Choreographer
import android.view.View
import com.example.kugellabyrinth.core.R

/**
 * Zeichnet das Labyrinth und treibt die [GameEngine] an. Wird von Handy- und Uhr-App gemeinsam genutzt.
 *
 * - [Shape.RECT] (Handy): Labyrinth über die volle Breite, Zeilen aus der Höhe.
 * - [Shape.ROUND] (Uhr): quadratisches Labyrinth im runden Display.
 *
 * Performance:
 *  - Takt über [Choreographer] (vsync-synchron, Zeitstempel des Frames).
 *  - Physik läuft im Frame-Callback, onDraw zeichnet nur.
 *  - Das statische Labyrinth wird einmal pro Labyrinth in eine Bitmap gerendert; pro Frame werden nur
 *    diese Bitmap und drei Kreise gezeichnet.
 *  - Keine Allokationen pro Frame (Callbacks mit primitiven Parametern, kein Boxing).
 *
 * Bedienung: Antippen startet (READY) bzw. meldet [Listener.onFinishedTapped] (FINISHED).
 * Langes Drücken kann die App über setOnLongClickListener belegen.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), Choreographer.FrameCallback {

    /** Callbacks an die Activity. Primitive Parameter -> kein Boxing pro Frame. */
    interface Listener {
        fun onTimeChanged(seconds: Float)
        fun onStartRequested()
        fun onFinished(seconds: Float)

        /** Antippen des Spielfelds nach Erreichen des Ziels. */
        fun onFinishedTapped() {}
    }

    /** Quelle der Neigung in m/s² auf Bildschirmachsen. */
    interface TiltSource {
        val tiltX: Float
        val tiltY: Float
    }

    /** Form der Spielfläche. */
    enum class Shape { RECT, ROUND }

    var listener: Listener? = null
    var tiltSource: TiltSource? = null
    var difficulty = Difficulty.MEDIUM
    var assist = AssistMode.SOFTWARE
    var speedFactor = 1f

    /** Vor dem ersten Layout setzen. */
    var shape = Shape.RECT

    /** Text unter „Bereit?“ (gerätespezifisch, z. B. Handy vs. Arm). */
    var readyBody: String = ""
        set(value) {
            field = value
            buildTextLayouts()
        }

    var engine: GameEngine? = null
        private set

    private val choreographer = Choreographer.getInstance()
    private var frameScheduled = false
    private var lastFrameNs = 0L
    private var hostResumed = false

    // Geometrie
    private var cell = 1f
    private var offX = 0f
    private var offY = 0f
    private var mazeBitmap: Bitmap? = null

    // Paints
    private val rectBackground = Color.parseColor("#111827")
    private val floorPaint = Paint().apply { color = Color.parseColor("#1F2937") }
    private val wallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6B7280") }
    private val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#34D399") }
    private val goalRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#6634D399") }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#334F8CFF") }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#66000000") }
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#4F8CFF") }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#AAFFFFFF") }
    private val dimPaint = Paint().apply { color = Color.parseColor("#C0111827") }
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        textSize = sp(28f)
    }
    private val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = sp(16f)
    }
    private val readyTitle = context.getString(R.string.ready_title)
    private var readyLayout: StaticLayout? = null

    // Ergebnis-Overlay (optional; die Handy-App nutzt stattdessen einen Dialog)
    private var resultTitle: String? = null
    private var resultBody = ""
    private var resultLayout: StaticLayout? = null

    init {
        isClickable = true
        isFocusable = true
        contentDescription = context.getString(R.string.game_area_description)
    }

    private fun sp(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)

    // ------------------------------------------------------------------------------------
    // Steuerung von außen
    // ------------------------------------------------------------------------------------

    /** Schriftgrößen der Overlays in sp (Uhr: kleiner als Handy). */
    fun setTextSizes(titleSp: Float, bodySp: Float) {
        titlePaint.textSize = sp(titleSp)
        bodyPaint.textSize = sp(bodySp)
        buildTextLayouts()
        invalidate()
    }

    /** Zeigt nach dem Ziel ein Ergebnis-Overlay auf dem Spielfeld. */
    fun showResult(title: String, body: String) {
        resultTitle = title
        resultBody = body
        buildTextLayouts()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        buildTextLayouts()
        newMaze()
    }

    /** Neues Labyrinth passend zu Viewgröße und Form. */
    fun newMaze() {
        if (width <= 0 || height <= 0) return
        val layout = when (shape) {
            Shape.RECT -> MazeLayout.fillRect(width, height, difficulty.cols)
            Shape.ROUND -> MazeLayout.squareInCircle(width, height, difficulty.watchCols)
        }
        cell = layout.cell
        offX = layout.offsetX
        offY = layout.offsetY
        val maze = Maze(layout.cols, layout.rows)
        engine = GameEngine(maze, difficulty, assist, speedFactor)
        renderMazeBitmap(maze)
        afterReset()
    }

    /** Gleiches Labyrinth, Kugel zurück auf Start. */
    fun restart() {
        engine?.reset() ?: return
        afterReset()
    }

    fun onHostResume() {
        hostResumed = true
        if (engine?.state == GameEngine.State.RUNNING) scheduleFrame()
    }

    fun onHostPause() {
        hostResumed = false
        cancelFrame()
    }

    private fun afterReset() {
        resultTitle = null
        cancelFrame()
        listener?.onTimeChanged(0f)
        invalidate()
    }

    private fun buildTextLayouts() {
        if (width <= 0) return
        val textWidth = (width * if (shape == Shape.ROUND) ROUND_TEXT_WIDTH else RECT_TEXT_WIDTH).toInt()
        readyLayout = centered(readyBody, textWidth)
        resultLayout = centered(resultBody, textWidth)
    }

    private fun centered(text: String, textWidth: Int): StaticLayout =
        StaticLayout.Builder
            .obtain(text, 0, text.length, bodyPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()

    // ------------------------------------------------------------------------------------
    // Spielschleife
    // ------------------------------------------------------------------------------------

    private fun scheduleFrame() {
        if (frameScheduled || !hostResumed) return
        frameScheduled = true
        lastFrameNs = 0L
        choreographer.postFrameCallback(this)
    }

    private fun cancelFrame() {
        if (!frameScheduled) return
        choreographer.removeFrameCallback(this)
        frameScheduled = false
    }

    override fun doFrame(frameTimeNanos: Long) {
        frameScheduled = false
        val e = engine ?: return
        if (e.state != GameEngine.State.RUNNING || !hostResumed) return

        if (lastFrameNs != 0L) {
            val dt = (frameTimeNanos - lastFrameNs) * 1e-9f
            val src = tiltSource
            val finished = if (src != null) e.update(dt, src.tiltX, src.tiltY) else e.update(dt, 0f, 0f)
            listener?.onTimeChanged(e.elapsed)
            if (finished) {
                invalidate()
                listener?.onFinished(e.elapsed)
                return
            }
        }
        lastFrameNs = frameTimeNanos
        invalidate()
        frameScheduled = true
        choreographer.postFrameCallback(this)
    }

    override fun onDetachedFromWindow() {
        cancelFrame()
        super.onDetachedFromWindow()
    }

    // ------------------------------------------------------------------------------------
    // Eingabe (Touch-Auswertung übernimmt View: Klick, langes Drücken, Bedienungshilfen)
    // ------------------------------------------------------------------------------------

    override fun performClick(): Boolean {
        super.performClick()
        val e = engine ?: return false
        when (e.state) {
            GameEngine.State.READY -> {
                listener?.onStartRequested()
                e.start()
                scheduleFrame()
                invalidate()
            }
            GameEngine.State.FINISHED -> listener?.onFinishedTapped()
            GameEngine.State.RUNNING -> Unit
        }
        return true
    }

    // ------------------------------------------------------------------------------------
    // Zeichnen
    // ------------------------------------------------------------------------------------

    private fun renderMazeBitmap(m: Maze) {
        // Alte Bitmap NICHT recyceln: Der RenderThread kann sie noch in einer Display-List halten.
        val old = mazeBitmap
        val bmp = if (old != null && old.width == width && old.height == height) old
        else Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        // Uhr: schwarz (AMOLED spart Strom, Ränder außerhalb des Kreises sind ohnehin unsichtbar)
        c.drawColor(if (shape == Shape.ROUND) Color.BLACK else rectBackground)
        c.save()
        c.translate(offX, offY)
        c.scale(cell, cell)
        c.drawRect(0f, 0f, m.cols.toFloat(), m.rows.toFloat(), floorPaint)
        c.drawCircle(m.startCol + 0.5f, m.startRow + 0.5f, 0.38f, startPaint)
        c.drawCircle(m.goalCol + 0.5f, m.goalRow + 0.5f, 0.42f, goalRingPaint)
        c.drawCircle(m.goalCol + 0.5f, m.goalRow + 0.5f, 0.24f, goalPaint)
        val w = m.walls
        var i = 0
        while (i < w.size) {
            c.drawRoundRect(w[i], w[i + 1], w[i + 2], w[i + 3], 0.05f, 0.05f, wallPaint)
            i += 4
        }
        c.restore()
        mazeBitmap = bmp
    }

    override fun onDraw(canvas: Canvas) {
        val e = engine ?: return
        mazeBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }

        val px = offX + e.renderX * cell
        val py = offY + e.renderY * cell
        val r = GameEngine.BALL_RADIUS * cell
        canvas.drawCircle(px + r * 0.15f, py + r * 0.2f, r, shadowPaint)
        canvas.drawCircle(px, py, r, ballPaint)
        canvas.drawCircle(px - r * 0.3f, py - r * 0.3f, r * 0.3f, highlightPaint)

        when (e.state) {
            GameEngine.State.READY -> drawOverlay(canvas, readyTitle, readyLayout)
            GameEngine.State.FINISHED -> resultTitle?.let { drawOverlay(canvas, it, resultLayout) }
            GameEngine.State.RUNNING -> Unit
        }
    }

    private fun drawOverlay(canvas: Canvas, title: String, body: StaticLayout?) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
        val bodyHeight = body?.height ?: 0
        val top = height / 2f - (titlePaint.textSize * 1.6f + bodyHeight) / 2f
        canvas.drawText(title, width / 2f, top + titlePaint.textSize, titlePaint)
        if (body == null) return
        canvas.save()
        canvas.translate((width - body.width) / 2f, top + titlePaint.textSize * 1.6f)
        body.draw(canvas)
        canvas.restore()
    }

    private companion object {
        /** Textbreite der Overlays relativ zur Viewbreite. */
        const val RECT_TEXT_WIDTH = 0.85f
        const val ROUND_TEXT_WIDTH = 0.72f
    }
}
