package com.smartpods.android.pulseecho.CustomUI

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.smartpods.android.pulseecho.R
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

interface OnWaveUIListener {
    fun onStuffing(progress: Int, max: Int)
}

class CustomShapeProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /*Animator*/
    private var shiftX1 = 0f
    private var waveVector = -0.25f
    private var waveOffset = 25
    private var speed = 25
    private var thread = HandlerThread("CustomShapeProgressView_" + hashCode())
    private var animHandler:Handler = Handler(Looper.getMainLooper())
    private var uiHandler:Handler = UIHandler(WeakReference<View>(this))
    private val mBorderPaint = Paint()
    private val mViewPaint = Paint()
    private var mWavePaint1 = Paint()
    private var mWavePaint2 = Paint()
    private var mPathContent:Path = Path()
    private var mPathBorder:Path  = Path()
    private var mShapePadding = DEFAULT_PADDING
    var mProgress = DEFAULT_PROGRESS
        set(progress) {
            if (progress <= mMax)
            {
                mListener?.onStuffing(progress, mMax)
                field = progress
                createShader()
                val message = Message.obtain(uiHandler)
                message.sendToTarget()
            }
        }
    private var mMax = DEFAULT_MAX
        set(max) {
            if (this.mMax != max)
            {
                if (max >= mProgress)
                {
                    field = max
                    createShader()
                    val message = Message.obtain(uiHandler)
                    message.sendToTarget()
                }
            }
        }
    private var mFrontWaveColor = DEFAULT_FRONT_WAVE_COLOR
    private var mBehindWaveColor = DEFAULT_BEHIND_WAVE_COLOR
    private var mBorderColor = DEFAULT_BORDER_COLOR
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    private var mTextColor = DEFAULT_TEXT_COLOR
    private var isAnimation = DEFAULT_ENABLE_ANIMATION
    private var isHideText = DEFAULT_HIDE_TEXT
    private var mStrong = DEFAULT_STRONG
    private var mSpikes = DEFAULT_SPIKE_COUNT
    private var mShape = Shape.CIRCLE
    private var mTextLabel = ""
    var mListener: OnWaveUIListener? = null
    private var screenSize = Point(0, 0)

    enum class Shape private constructor(value: Int) {
        CIRCLE(1), SQUARE(2), HEART(3), STAR(4);
        internal var value:Int = 0
        init{
            this.value = value
        }
        companion object {
            internal fun fromValue(value: Int):Shape {
                for (shape in values())
                {
                    if (shape.value == value) return shape
                }
                return CIRCLE
            }
        }
    }

    init{
        val attributes = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SPCustomShapeProgress,
            0,
            0
        )
        mFrontWaveColor = attributes.getColor(
            R.styleable.SPCustomShapeProgress_frontColor,
            DEFAULT_FRONT_WAVE_COLOR
        )
        mBehindWaveColor = attributes.getColor(
            R.styleable.SPCustomShapeProgress_behideColor,
            DEFAULT_BEHIND_WAVE_COLOR
        )
        mBorderColor = attributes.getColor(
            R.styleable.SPCustomShapeProgress_borderColor,
            DEFAULT_BORDER_COLOR
        )
        mTextColor = attributes.getColor(
            R.styleable.SPCustomShapeProgress_textColor,
            DEFAULT_TEXT_COLOR
        )
        mProgress = attributes.getInt(R.styleable.SPCustomShapeProgress_progress, DEFAULT_PROGRESS)
        mMax = attributes.getInt(R.styleable.SPCustomShapeProgress_max, DEFAULT_MAX)
        mBorderWidth = attributes.getDimension(
            R.styleable.SPCustomShapeProgress_borderWidthSize,
            DEFAULT_BORDER_WIDTH
        )
        mStrong = attributes.getInt(R.styleable.SPCustomShapeProgress_strong, DEFAULT_STRONG)
        mShape = Shape.fromValue(attributes.getInt(R.styleable.SPCustomShapeProgress_shapeType, 1))
        mShapePadding = attributes.getDimension(
            R.styleable.SPCustomShapeProgress_shapePadding,
            DEFAULT_PADDING
        )
        isAnimation = attributes.getBoolean(
            R.styleable.SPCustomShapeProgress_animatorEnable,
            DEFAULT_ENABLE_ANIMATION
        )
        isHideText = attributes.getBoolean(
            R.styleable.SPCustomShapeProgress_textHidden,
            DEFAULT_HIDE_TEXT
        )
        mBorderPaint.isAntiAlias = true
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.strokeWidth = mBorderWidth
        mBorderPaint.color = mBorderColor
        mWavePaint1 = Paint()
        mWavePaint1.strokeWidth = 2f
        mWavePaint1.isAntiAlias = true
        mWavePaint1.color = mBehindWaveColor
        mWavePaint2 = Paint()
        mWavePaint2.strokeWidth = 2f
        mWavePaint2.isAntiAlias = true
        mWavePaint2.color = mFrontWaveColor
        thread.start()
        animHandler = Handler(thread.looper)
        uiHandler = UIHandler(WeakReference<View>(this))
        screenSize = Point(width, height)
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun startAnimation() {
        isAnimation = true
        if (width > 0 && height > 0)
        {
            animHandler.removeCallbacksAndMessages(null)
            animHandler.post(object : Runnable {
                public override fun run() {
                    shiftX1 += waveVector
                    createShader()
                    val message = Message.obtain(uiHandler)
                    message.sendToTarget()
                    if (isAnimation) {
                        if (animHandler.looper.thread.isAlive) {
                            animHandler.postDelayed(this, speed.toLong())
                        }
                    }
                }
            })
        }
    }
    fun stopAnimation() {
        isAnimation = false
    }

    fun setMax(max: Int) {
        if (mMax != max) {
            if (max >= mProgress) {
                mMax = max
                createShader()
                val message = Message.obtain(uiHandler)
                message.sendToTarget()
            }
        }
    }

    fun getMax(): Int {
        return mMax
    }

    fun setTextLabel(text: String): String {
        mTextLabel = text
        val message = Message.obtain(uiHandler)
        message.sendToTarget()

        return mTextLabel
    }

    fun getListener(): OnWaveUIListener? {
        return mListener
    }

    fun setListener(_mListener: OnWaveUIListener?) {
        if (_mListener != null) {
            this.mListener = _mListener
        }
    }

    fun setBorderColor(color: Int) {
        mBorderColor = color
        mBorderPaint.color = mBorderColor
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setFrontWaveColor(color: Int) {
        mFrontWaveColor = color
        mWavePaint2.color = mFrontWaveColor
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setBehindWaveColor(color: Int) {
        mBehindWaveColor = color
        mWavePaint1.color = mBehindWaveColor
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }

    fun setTextColor(color: Int) {
        mTextColor = color
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setBorderWidth(width: Float) {
        mBorderWidth = width
        mBorderPaint.strokeWidth = mBorderWidth
        resetShapes()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setShapePadding(padding: Float) {
        this.mShapePadding = padding
        resetShapes()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setAnimationSpeed(speed: Int) {
        if (speed < 0)
        {
            throw IllegalArgumentException("The speed must be greater than 0.")
        }
        this.speed = speed
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setWaveVector(offset: Float) {
        if (offset < 0 || offset > 100)
        {
            throw IllegalArgumentException("The vector of wave must be between 0 and 100.")
        }
        this.waveVector = (offset - 50f) / 50f
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setHideText(hidden: Boolean) {
        this.isHideText = hidden
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setStarSpikes(count: Int) {
        if (count < 3)
        {
            throw IllegalArgumentException("The number of spikes must be greater than 3.")
        }
        this.mSpikes = count
        if (Math.min(screenSize.x, screenSize.y) !== 0)
        {
            resetShapes()
        }
    }
    fun setWaveOffset(offset: Int) {
        this.waveOffset = offset
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setWaveStrong(strong: Int) {
        this.mStrong = strong
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    fun setShape(shape: Shape) {
        mShape = shape
        resetShapes()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    protected override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenSize = Point(w, h)
        resetShapes()
        if (isAnimation)
        {
            startAnimation()
        }
    }
    private fun resetShapes() {
        val radius = Math.min(screenSize.x, screenSize.y)
        val cx = (screenSize.x - radius) / 2
        val cy = (screenSize.y - radius) / 2
        when (mShape) {
            CustomShapeProgressView.Shape.STAR -> {
                mPathBorder = drawStart(
                    radius / 2 + cx,
                    radius / 2 + cy + mBorderWidth.toInt(),
                    mSpikes,
                    radius / 2 - mBorderWidth.toInt(),
                    radius / 4
                )
                mPathContent = drawStart(
                    radius / 2 + cx,
                    radius / 2 + cy + mBorderWidth.toInt(),
                    mSpikes,
                    radius / 2 - mBorderWidth.toInt() - mShapePadding.toInt(),
                    radius / 4 - mShapePadding.toInt()
                )
            }
            CustomShapeProgressView.Shape.HEART -> {
                mPathBorder = drawHeart(cx, cy, radius)
                mPathContent = drawHeart(
                    cx + (mShapePadding.toInt() / 2),
                    cy + (mShapePadding.toInt() / 2),
                    radius - mShapePadding.toInt()
                )
            }
            CustomShapeProgressView.Shape.CIRCLE -> {
                mPathBorder = drawCircle(cx, cy, radius)
                mPathContent = drawCircle(
                    cx + (mShapePadding.toInt() / 2),
                    cy + (mShapePadding.toInt() / 2),
                    radius - mShapePadding.toInt()
                )
            }
            CustomShapeProgressView.Shape.SQUARE -> {
                mPathBorder = drawSquare(cx, cy, radius)
                mPathContent = drawSquare(
                    cx + (mShapePadding.toInt() / 2),
                    cy + (mShapePadding.toInt() / 2),
                    radius - mShapePadding.toInt()
                )
            }
        }
        createShader()
        val message = Message.obtain(uiHandler)
        message.sendToTarget()
    }
    private fun drawSquare(cx: Int, cy: Int, radius: Int):Path {
        val path = Path()
        path.moveTo(cx.toFloat(), cy + (mBorderWidth / 2))
        path.lineTo(cx.toFloat(), radius + cy - mBorderWidth)
        path.lineTo((radius + cx).toFloat(), radius + cy - mBorderWidth)
        path.lineTo((radius + cx).toFloat(), cy + mBorderWidth)
        path.lineTo(cx.toFloat(), cy + mBorderWidth)
        path.close()
        return path
    }
    private fun drawCircle(cx: Int, cy: Int, radius: Int):Path {
        val path = Path()
        path.addCircle(
            ((radius / 2) + cx).toFloat(),
            ((radius / 2) + cy).toFloat(),
            (radius / 2) - mBorderWidth,
            Path.Direction.CCW
        )
        path.close()
        return path
    }
    private fun drawHeart(cx: Int, cy: Int, radius: Int):Path {
        val path = Path()
        /*start point*/
        path.moveTo((radius / 2 + cx).toFloat(), (radius / 5 + cy).toFloat())
        /*left rising lin*/
        path.cubicTo(
            (5 * radius / 14 + cx).toFloat(),
            cy.toFloat(),
            cx.toFloat(),
            (radius / 15 + cy).toFloat(),
            (radius / 28 + cx).toFloat(),
            (2 * radius / 5 + cy).toFloat()
        )
        /*left drop line*/
        path.cubicTo(
            (radius / 14 + cx).toFloat(),
            (2 * radius / 3 + cy).toFloat(),
            (3 * radius / 7 + cx).toFloat(),
            (5 * radius / 6 + cy).toFloat(),
            (radius / 2 + cx).toFloat(),
            (9 * radius / 10.5 + cy).toFloat()
        )
        /*right descending line*/
        path.cubicTo(
            (4 * radius / 7 + cx).toFloat(),
            (5 * radius / 6 + cy).toFloat(),
            (13 * radius / 14 + cx).toFloat(),
            (2 * radius / 3 + cy).toFloat(),
            (27 * radius / 28 + cx).toFloat(),
            (2 * radius / 5 + cy).toFloat()
        )
        /*right rising line*/
        path.cubicTo(
            (radius + cx).toFloat(),
            (radius / 15 + cy).toFloat(),
            (9 * radius / 14 + cx).toFloat(),
            cy.toFloat(),
            (radius / 2 + cx).toFloat(),
            (radius / 5 + cy).toFloat()
        )
        path.close()
        return path
    }
    /**
     * Draw the stars
     *
     * @param cx X
     * @param cy Y
     * @param spikes the number of angles of the star
     * @param outerRadius outer radius
     * @param innerRadius inner radius
     * @return path
     */
    private fun drawStart(cx: Int, cy: Int, spikes: Int, outerRadius: Int, innerRadius: Int):Path {
        val path = Path()
        var rot = Math.PI / 2.0 * 3.0
        val step = Math.PI / spikes
        path.moveTo(cx.toFloat(), (cy - outerRadius).toFloat())
        for (i in 0 until spikes)
        {
            path.lineTo(
                cx + cos(rot).toFloat() * outerRadius,
                cy + sin(rot).toFloat() * outerRadius
            )
            rot += step
            path.lineTo(
                cx + cos(rot).toFloat() * innerRadius,
                cy + sin(rot).toFloat() * innerRadius
            )
            rot += step
        }
        path.lineTo(cx.toFloat(), (cy - outerRadius).toFloat())
        path.close()
        return path
    }
    /**
     * Create a fill shader
     * y = Asin(ωx+φ)+h Waveform formula (sine function) y = waveLevel * Math.sin(w * x1 + shiftX) + level
     * φ (initial phase x): the offset of the X-axis of the waveform $shiftX
     * ω (angular frequency): minimum positive period T=2π/|ω| $w
     * A (wave amplitude): the size of the hump $waveLevel
     * h (initial phase y): Y-axis offset of the waveform $level
     * <p>
     * Bézier curve
     * B(t) = X(1-t)^2 + 2t(1-t)Y + Zt^2, 0 <= t <= n
     */
    private fun createShader() {
        if (screenSize.x <= 0 || screenSize.y <= 0)
        {
            return
        }
        val viewSize = Math.min(screenSize.x, screenSize.y)
        val w = (2.0f * Math.PI) / viewSize
        val bitmap = Bitmap.createBitmap(viewSize, viewSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val level = ((((mMax - mProgress).toFloat()) / mMax.toFloat()) * viewSize) + ((screenSize.y / 2) - (viewSize / 2))
        val x2 = viewSize + 1
        val y2 = viewSize + 1
        val zzz = ((viewSize.toFloat() * ((waveOffset - 50) / 100f)) / (viewSize.toFloat() / 6.25f))
        val shiftX2 = shiftX1 + zzz
        val waveLevel = mStrong * (viewSize / 20) / 100 // viewSize / 20
        for (x1 in 0 until x2)
        {
            var y1 = (waveLevel * sin(w * x1 + shiftX1) + level).toFloat()
            canvas.drawLine(x1.toFloat(), y1, x1.toFloat(), y2.toFloat(), mWavePaint1)
            y1 = (waveLevel * sin(w * x1 + shiftX2) + level).toFloat()
            canvas.drawLine(x1.toFloat(), y1, x1.toFloat(), y2.toFloat(), mWavePaint2)
        }
        mViewPaint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
    }
    protected override fun onDetachedFromWindow() {
        if (animHandler != null)
        {
            animHandler.removeCallbacksAndMessages(null)
        }
        if (thread != null)
        {
            thread.quit()
        }
        super.onDetachedFromWindow()
    }
    protected override fun onDraw(canvas: Canvas) {
        canvas.drawPath(mPathContent, mViewPaint)
        if (mBorderWidth > 0)
        {
            canvas.drawPath(mPathBorder, mBorderPaint)
        }
        if (!isHideText)
        {
            val percent = (mProgress * 100) / mMax.toFloat()
            var text = ""

            if (mShape == Shape.HEART) {
                text = mTextLabel
            } else {
                text = String.format(Locale.CANADA, "%.1f", percent) + "%"
            }

            val textPaint = TextPaint()
            textPaint.color = mTextColor
            if (mShape == Shape.STAR)
            {
                textPaint.textSize = (screenSize.x.coerceAtMost(screenSize.y) / 2f) / 3
                val customTypeface = resources.getFont(R.font.gotham_rounded_book)
                textPaint.typeface = customTypeface
            }
            else
            {
                textPaint.textSize = (screenSize.x.coerceAtMost(screenSize.y) / 2f) / 2
                val customTypeface = resources.getFont(R.font.gotham_rounded_book)
                textPaint.typeface = customTypeface
            }
            textPaint.isAntiAlias = true
            val textHeight = textPaint.descent() + textPaint.ascent()
            canvas.drawText(
                text,
                (screenSize.x - textPaint.measureText(text)) / 2.0f,
                (screenSize.y - textHeight) / 2.2f,
                textPaint
            )
        }
    }
    private class UIHandler internal constructor(view: WeakReference<View>):Handler(Looper.getMainLooper()) {
        private val mView:View = view.get()!!
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mView != null)
            {
                mView.invalidate()
            }
        }
    }
    companion object {
        private const val DEFAULT_PROGRESS = 0
        private const val DEFAULT_MAX = 100
        private const val DEFAULT_STRONG = 50
        val DEFAULT_BEHIND_WAVE_COLOR = Color.parseColor("#443030d5")
        val DEFAULT_FRONT_WAVE_COLOR = Color.parseColor("#FF3030d5")
        val DEFAULT_BORDER_COLOR =  Color.parseColor("#000000")
        private const val DEFAULT_BORDER_WIDTH = 10f
        val DEFAULT_TEXT_COLOR = Color.parseColor("#000000")
        private const val DEFAULT_ENABLE_ANIMATION = false
        private const val DEFAULT_HIDE_TEXT = false
        private const val DEFAULT_SPIKE_COUNT = 5
        private const val DEFAULT_PADDING = 0f
    }
}