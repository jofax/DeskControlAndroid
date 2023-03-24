package com.smartpods.android.pulseecho.CustomUI

import android.animation.ValueAnimator
import android.view.View
import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.TypeEvaluator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.*
import androidx.annotation.IntRange
import com.smartpods.android.pulseecho.R
import java.lang.annotation.RetentionPolicy
import kotlin.math.cos
import kotlin.math.sin

//DefaultProgressTextAdapter
class DefaultProgressTextAdapter:CircularProgressIndicator.ProgressTextAdapter {
    override fun formatText(currentProgress:Double):String {
        return (currentProgress.toInt()).toString()
    }
}

//DefaultAnimatorListener
internal open class DefaultAnimatorListener:Animator.AnimatorListener {
    override fun onAnimationStart(animation:Animator, isReverse:Boolean) {
    }
    override fun onAnimationEnd(animation:Animator, isReverse:Boolean) {
    }
    override fun onAnimationStart(animation:Animator) {
    }
    override fun onAnimationEnd(animation:Animator) {
    }
    override fun onAnimationCancel(animation:Animator) {
    }
    override fun onAnimationRepeat(animation:Animator) {
    }
}

//PatternProgressTextAdapter
class PatternProgressTextAdapter(pattern:String):CircularProgressIndicator.ProgressTextAdapter {
    private val pattern:String
    init{
        this.pattern = pattern
    }
    @NonNull
    override fun formatText(currentProgress:Double):String {
        return String.format(pattern, currentProgress)
    }
}

/*
* CircularProgressIndicator Class
*
* */

class CircularProgressIndicator: View {
    private var progressPaint: Paint = Paint()
    private var progressBackgroundPaint:Paint = Paint()
    private var dotPaint:Paint = Paint()
    private var textPaint:Paint = Paint()
    var startAngle = DEFAULT_PROGRESS_START_ANGLE
        set(@IntRange(from = 0, to = 360) startAngle) {
            field = startAngle
            invalidate()
        }
    private var sweepAngle = 0
    private var circleBounds: RectF  = RectF()
    private var progressText:String = ""
    private var textX:Float = 0.toFloat()
    private var textY:Float = 0.toFloat()
    private var radius:Float = 0.toFloat()
    var isDotEnabled:Boolean = false

    var maxProgress = 100.0
        set(maxProgress) {
            field = maxProgress
            if (this.maxProgress < progress)
            {
                setCurrentProgress(maxProgress)
            }
            invalidate()
        }
    var progress = 0.0
    var isAnimationEnabled:Boolean = false
        set(enableAnimation) {
            field = enableAnimation
            if (!enableAnimation) stopProgressAnimation()
        }
    var isFillBackgroundEnabled:Boolean = false
        set(fillBackgroundEnabled) {
            if (fillBackgroundEnabled == this.isFillBackgroundEnabled) return
            field = fillBackgroundEnabled
            val style = if (fillBackgroundEnabled) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
            progressBackgroundPaint.setStyle(style)
            invalidate()
        }
    @Direction
    @get:Direction
    var direction = DIRECTION_CLOCKWISE
        set(@Direction direction) {
            field = direction
            invalidate()
        }
    lateinit var progressAnimator: ValueAnimator
    var progressTextAdapter:ProgressTextAdapter? = null
        set(@Nullable progressTextAdapter) {
            field = progressTextAdapter ?: DefaultProgressTextAdapter()
            reformatProgressText()
            invalidateEverything()
        }
    @Nullable
    @get:Nullable
    var mProgressChangeListener:OnProgressChangeListener? = null
    var interpolator: AccelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()
    var progressColor:Int
        @ColorInt
        get() {
            return progressPaint.color
        }
        set(@ColorInt color) {
            progressPaint.color = color
            invalidate()
        }
    var progressBackgroundColor:Int
        @ColorInt
        get() {
            return progressBackgroundPaint.color
        }
        set(@ColorInt color) {
            progressBackgroundPaint.color = color
            invalidate()
        }
    val progressStrokeWidth:Float
        get() {
            return progressPaint.strokeWidth
        }
    val progressBackgroundStrokeWidth:Float
        get() {
            return progressBackgroundPaint.strokeWidth
        }
    var textColor:Int
        @ColorInt
        get() {
            return textPaint.color
        }
        set(@ColorInt color) {
            textPaint.color = color
            val textRect = Rect()
            textPaint.getTextBounds(progressText, 0, progressText.length, textRect)

        }
    val textSize:Float
        get() {
            return textPaint.textSize
        }
    var dotColor:Int
        @ColorInt
        get() {
            return dotPaint.color
        }
        set(@ColorInt color) {
            dotPaint.color = color
            invalidate()
        }
    var dotWidth:Float = 3.0F
        get() {
            return dotPaint.strokeWidth
        }
    var progressStrokeCap:Int
        @Cap
        get() {
            return if ((progressPaint.strokeCap === Paint.Cap.ROUND)) CAP_ROUND else CAP_BUTT
        }
        set(@Cap cap) {
            val paintCap = if ((cap == CAP_ROUND)) Paint.Cap.ROUND else Paint.Cap.BUTT
            if (progressPaint.strokeCap !== paintCap)
            {
                progressPaint.strokeCap = paintCap
                invalidate()
            }
        }
    val gradientType:Int
        @GradientType
        get() {
            val shader = progressPaint.shader
            var type = NO_GRADIENT
            if (shader is LinearGradient)
            {
                type = LINEAR_GRADIENT
            }
            else if (shader is RadialGradient)
            {
                type = RADIAL_GRADIENT
            }
            else if (shader is SweepGradient)
            {
                type = SWEEP_GRADIENT
            }
            return type
        }

    constructor(context: Context) : super(context) {
        init(context, null!!)
    }
    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }
    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        @Nullable attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }
    private fun init(@NonNull context: Context, @Nullable attrs: AttributeSet) {
        val progressColor = Color.parseColor(DEFAULT_PROGRESS_COLOR)
        val progressBackgroundColor = Color.parseColor(DEFAULT_PROGRESS_BACKGROUND_COLOR)
        var progressStrokeWidth = dp2px(DEFAULT_STROKE_WIDTH_DP)
        var progressBackgroundStrokeWidth = progressStrokeWidth
        val textColor = progressColor
        var textSize = sp2px(DEFAULT_TEXT_SIZE_SP.toFloat())
        isDotEnabled = true
        val dotColor = progressColor
        var dotWidth = progressStrokeWidth
        var progressStrokeCap = Paint.Cap.ROUND
        if (attrs != null)
        {
            val a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressIndicator)
            this@CircularProgressIndicator.progressColor = a.getColor(
                R.styleable.SPCircularProgressIndicator_progressColor,
                progressColor
            )
            this@CircularProgressIndicator.progressBackgroundColor = a.getColor(
                R.styleable.SPCircularProgressIndicator_progressBackgroundColor,
                progressBackgroundColor
            )
            progressStrokeWidth = a.getDimensionPixelSize(
                R.styleable.SPCircularProgressIndicator_progressStrokeWidth,
                progressStrokeWidth
            )
            progressBackgroundStrokeWidth = a.getDimensionPixelSize(
                R.styleable.SPCircularProgressIndicator_progressBackgroundStrokeWidth,
                progressStrokeWidth
            )
            this@CircularProgressIndicator.textColor = a.getColor(R.styleable.SPCircularProgressIndicator_textCircularProgressColor, progressColor)
            textSize = a.getDimensionPixelSize(
                R.styleable.SPCircularProgressIndicator_textSize,
                textSize
            )
            isDotEnabled = a.getBoolean(R.styleable.SPCircularProgressIndicator_drawDot, true)
            this@CircularProgressIndicator.dotColor = a.getColor(R.styleable.SPCircularProgressIndicator_dotColor, progressColor)
            dotWidth = a.getDimensionPixelSize(
                R.styleable.SPCircularProgressIndicator_dotWidth,
                progressStrokeWidth
            )
            startAngle = a.getInt(
                R.styleable.SPCircularProgressIndicator_startAngle,
                DEFAULT_PROGRESS_START_ANGLE
            )
            if (this.startAngle < 0 || this.startAngle > 360)
            {
                startAngle = DEFAULT_PROGRESS_START_ANGLE
            }
            isAnimationEnabled = a.getBoolean(
                R.styleable.SPCircularProgressIndicator_enableProgressAnimation,
                true
            )
            isFillBackgroundEnabled = a.getBoolean(R.styleable.SPCircularProgressIndicator_fillBackground, false)
            direction = a.getInt(
                R.styleable.SPCircularProgressIndicator_direction,
                DIRECTION_COUNTERCLOCKWISE
            )
            val cap = a.getInt(R.styleable.SPCircularProgressIndicator_progressCap, CAP_ROUND)
            progressStrokeCap = if ((cap == CAP_ROUND)) Paint.Cap.ROUND else Paint.Cap.BUTT
            val formattingPattern = a.getString(R.styleable.SPCircularProgressIndicator_formattingPattern)
            progressTextAdapter = if (formattingPattern != null) {
                PatternProgressTextAdapter(formattingPattern)
            } else {
                DefaultProgressTextAdapter()
            }
            reformatProgressText()
            val gradientType = a.getColor(R.styleable.SPCircularProgressIndicator_gradientType, 0)
            if (gradientType != NO_GRADIENT)
            {
                val gradientColorEnd = a.getColor(
                    R.styleable.SPCircularProgressIndicator_gradientEndColor,
                    -1
                )
                if (gradientColorEnd == -1)
                {
                    throw IllegalArgumentException("did you forget to specify gradientColorEnd?")
                }
                post { setGradient(gradientType, gradientColorEnd) }
            }
            a.recycle()
        }
        progressPaint = Paint()
        progressPaint.strokeCap = progressStrokeCap
        progressPaint.strokeWidth = progressStrokeWidth.toFloat()
        progressPaint.style = Paint.Style.STROKE
        progressPaint.color = progressColor
        progressPaint.isAntiAlias = true
        val progressBackgroundStyle = if (this.isFillBackgroundEnabled) Paint.Style.FILL_AND_STROKE else Paint.Style.STROKE
        progressBackgroundPaint = Paint()
        progressBackgroundPaint.style = progressBackgroundStyle
        progressBackgroundPaint.strokeWidth = progressBackgroundStrokeWidth.toFloat()
        progressBackgroundPaint.color = progressBackgroundColor
        progressBackgroundPaint.isAntiAlias = true
        dotPaint = Paint()
        dotPaint.strokeCap = Paint.Cap.ROUND
        dotPaint.strokeWidth = dotWidth.toFloat()
        dotPaint.style = Paint.Style.FILL_AND_STROKE
        dotPaint.color = dotColor
        dotPaint.isAntiAlias = true
        textPaint = TextPaint()
        textPaint.strokeCap = Paint.Cap.ROUND
        textPaint.color = textColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize.toFloat()
        circleBounds = RectF()
        progressAnimator = ValueAnimator()
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val textBoundsRect = Rect()
        textPaint.getTextBounds(progressText, 0, progressText.length, textBoundsRect)
        val dotWidth = dotPaint.strokeWidth
        val progressWidth = progressPaint.strokeWidth
        val progressBackgroundWidth = progressBackgroundPaint.strokeWidth
        val strokeSizeOffset = if ((isDotEnabled)) dotWidth.coerceAtLeast(
            progressWidth.coerceAtLeast(progressBackgroundWidth)
        ) else progressWidth.coerceAtLeast(progressBackgroundWidth)
        var desiredSize = ((strokeSizeOffset.toInt()) + dp2px(DESIRED_WIDTH_DP.toFloat()) +
                (paddingBottom + paddingTop).coerceAtLeast(paddingLeft + paddingRight))
        // multiply by .1f to have an extra space for small padding between text and circle
        desiredSize += (textBoundsRect.width().coerceAtLeast(textBoundsRect.height()) + desiredSize * .1f).toInt()
        val finalWidth:Int = when (widthMode) {
            MeasureSpec.EXACTLY -> measuredWidth
            MeasureSpec.AT_MOST -> desiredSize.coerceAtMost(measuredWidth)
            else -> desiredSize
        }
        val finalHeight:Int = when (heightMode) {
            MeasureSpec.EXACTLY -> measuredHeight
            MeasureSpec.AT_MOST -> desiredSize.coerceAtMost(measuredHeight)
            else -> desiredSize
        }
        val widthWithoutPadding = finalWidth - paddingLeft - paddingRight
        val heightWithoutPadding = finalHeight - paddingTop - paddingBottom
        val smallestSide = heightWithoutPadding.coerceAtMost(widthWithoutPadding)
        setMeasuredDimension(smallestSide, smallestSide)
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateBounds(w, h)
        val shader = progressPaint.shader
        if (shader is RadialGradient)
        {
            val gradient = shader as RadialGradient
        }
    }

    fun getListener(): OnProgressChangeListener? {
        return mProgressChangeListener
    }

    fun setListener(_mListener: OnProgressChangeListener?) {
        if (_mListener != null) {
            this.mProgressChangeListener = _mListener
        }
    }

    private fun calculateBounds(w: Int, h: Int) {
        radius = w / 2f
        val dotWidth = dotPaint.strokeWidth
        val progressWidth = progressPaint.strokeWidth
        val progressBackgroundWidth = progressBackgroundPaint.strokeWidth
        val strokeSizeOffset = if ((isDotEnabled)) dotWidth.coerceAtLeast(
            progressWidth.coerceAtLeast(progressBackgroundWidth)
        ) else progressWidth.coerceAtLeast(progressBackgroundWidth) // to prevent progress or dot from drawing over the bounds
        val halfOffset = strokeSizeOffset / 2f
        circleBounds.left = halfOffset
        circleBounds.top = halfOffset
        circleBounds.right = w - halfOffset
        circleBounds.bottom = h - halfOffset
        radius = circleBounds.width() / 2f
        calculateTextBounds()
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (progressAnimator != null)
        {
            progressAnimator.cancel()
        }
    }
    override fun onDraw(canvas: Canvas) {
        drawProgressBackground(canvas)
        drawProgress(canvas)
        if (isDotEnabled) drawDot(canvas)
        drawText(canvas)
    }
    private fun drawProgressBackground(canvas: Canvas) {
        canvas.drawArc(
            circleBounds, ANGLE_START_PROGRESS_BACKGROUND.toFloat(),
            ANGLE_END_PROGRESS_BACKGROUND.toFloat(),
            false, progressBackgroundPaint
        )
    }
    private fun drawProgress(canvas: Canvas) {
        canvas.drawArc(circleBounds, this.startAngle.toFloat(),
            sweepAngle.toFloat(), false, progressPaint)
    }
    private fun drawDot(canvas: Canvas) {
        val angleRadians = Math.toRadians((this.startAngle + sweepAngle + 180).toDouble())
        val cos = cos(angleRadians).toFloat()
        val sin = sin(angleRadians).toFloat()
        val x = circleBounds.centerX() - radius * cos
        val y = circleBounds.centerY() - radius * sin
        canvas.drawPoint(x, y, dotPaint)
    }
    private fun drawText(canvas: Canvas) {
        canvas.drawText(progressText, textX, textY, textPaint)
    }
    fun setCurrentProgress(currentProgress: Double) {
        if (currentProgress > maxProgress)
        {
            maxProgress = currentProgress
        }
        setProgress(currentProgress, maxProgress)
    }
    fun setProgress(current: Double, max: Double) {
        val finalAngle:Double = if (this.direction == DIRECTION_CLOCKWISE) {
            -(current / max * 360)
        } else {
            current / max * 360
        }
        val oldCurrentProgress = progress
        maxProgress = max
        progress = current.coerceAtMost(max)
        mProgressChangeListener?.onProgressChanged(progress, maxProgress)
        reformatProgressText()
        calculateTextBounds()
        stopProgressAnimation()
        if (this.isAnimationEnabled)
        {
            startProgressAnimation(oldCurrentProgress, finalAngle)
        }
        else
        {
            sweepAngle = finalAngle.toInt()
            invalidate()
        }
    }
    private fun startProgressAnimation(oldCurrentProgress: Double, finalAngle: Double) {
        val angleProperty = PropertyValuesHolder.ofInt(
            PROPERTY_ANGLE,
            sweepAngle,
            finalAngle.toInt()
        )
        progressAnimator = ValueAnimator.ofObject(TypeEvaluator<Double> { fraction, startValue, endValue -> (startValue + (endValue - startValue) * fraction) }, oldCurrentProgress, progress)
        progressAnimator.duration = DEFAULT_ANIMATION_DURATION.toLong()
        progressAnimator.setValues(angleProperty)
        progressAnimator.interpolator = interpolator
        progressAnimator.addUpdateListener { animation ->
            sweepAngle = animation.getAnimatedValue(PROPERTY_ANGLE) as Int
            invalidate()
        }
        progressAnimator.addListener(object : DefaultAnimatorListener() {
            override fun onAnimationCancel(animation: Animator) {
                sweepAngle = finalAngle.toInt()
                invalidate()
                //progressAnimator = null
            }
        })
        progressAnimator.start()
    }
    private fun stopProgressAnimation() {
        if (progressAnimator != null)
        {
            progressAnimator.cancel()
        }
    }
    private fun reformatProgressText() {
        progressText = this.progressTextAdapter?.formatText(progress) ?: ""
    }
    private fun calculateTextBounds():Rect {
        val textRect = Rect()
        textPaint.getTextBounds(progressText, 0, progressText.length, textRect)
        textX = circleBounds.centerX() - textRect.width() / 2f
        textY = circleBounds.centerY() + textRect.height() / 2f
        return textRect
    }
    private fun dp2px(dp: Float):Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }
    private fun sp2px(sp: Float):Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics).toInt()
    }
    // calculates circle bounds, view size and requests invalidation
    private fun invalidateEverything() {
        calculateBounds(width, height)
        requestLayout()
        invalidate()
    }
    fun setProgressStrokeWidthDp(@Dimension strokeWidth: Int) {
        setProgressStrokeWidthPx(dp2px(strokeWidth.toFloat()))
    }
    fun setProgressStrokeWidthPx(@Dimension strokeWidth: Int) {
        progressPaint.strokeWidth = strokeWidth.toFloat()
        invalidateEverything()
    }
    fun setProgressBackgroundStrokeWidthDp(@Dimension strokeWidth: Int) {
        setProgressBackgroundStrokeWidthPx(dp2px(strokeWidth.toFloat()))
    }
    fun setProgressBackgroundStrokeWidthPx(@Dimension strokeWidth: Int) {
        progressBackgroundPaint.strokeWidth = strokeWidth.toFloat()
        invalidateEverything()
    }
    fun setTextSizeSp(@Dimension size: Int) {
        setTextSizePx(sp2px(size.toFloat()))
    }
    fun setTextSizePx(@Dimension size: Int) {
        val currentSize = textPaint.textSize
        val factor = textPaint.measureText(progressText) / currentSize
        val offset = if ((isDotEnabled)) dotPaint.strokeWidth.coerceAtLeast(progressPaint.strokeWidth) else progressPaint.strokeWidth
        val maximumAvailableTextWidth = circleBounds.width() - offset
        var mSize = size
        if (size * factor >= maximumAvailableTextWidth)
        {
            mSize = (maximumAvailableTextWidth / factor).toInt()
        }
        textPaint.textSize = mSize.toFloat()
        val textBounds = calculateTextBounds()

    }
    fun setShouldDrawDot(shouldDrawDot: Boolean) {
        this.isDotEnabled = shouldDrawDot
        if (dotPaint.strokeWidth > progressPaint.strokeWidth)
        {
            requestLayout()
            return
        }
        invalidate()
    }
    fun setDotWidthDp(@Dimension width: Int) {
        setDotWidthPx(dp2px(width.toFloat()))
    }
    fun setDotWidthPx(@Dimension width: Int) {
        dotPaint.strokeWidth = width.toFloat()
        invalidateEverything()
    }
    fun setGradient(@GradientType type: Int, @ColorInt endColor: Int) {
        var gradient: Shader? = null
        val cx = width / 2f
        val cy = height / 2f
        val startColor = progressPaint.color
        when (type) {
            LINEAR_GRADIENT -> gradient = LinearGradient(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                startColor,
                endColor,
                Shader.TileMode.CLAMP
            )
            RADIAL_GRADIENT -> gradient = RadialGradient(
                cx,
                cy,
                cx,
                startColor,
                endColor,
                Shader.TileMode.MIRROR
            )
            SWEEP_GRADIENT -> gradient = SweepGradient(
                cx,
                cy,
                intArrayOf(startColor, endColor),
                null
            )
        }
        if (gradient != null)
        {
            val matrix = Matrix()
            matrix.postRotate(this.startAngle.toFloat(), cx, cy)
            gradient.setLocalMatrix(matrix)
        }
        progressPaint.setShader(gradient)
        invalidate()
    }

    @IntDef(DIRECTION_CLOCKWISE, DIRECTION_COUNTERCLOCKWISE)
    annotation class Direction
    @IntDef(CAP_ROUND, CAP_BUTT)
    annotation class Cap
    @IntDef(NO_GRADIENT, LINEAR_GRADIENT, RADIAL_GRADIENT, SWEEP_GRADIENT)
    annotation class GradientType
    interface ProgressTextAdapter {
        @NonNull
        fun formatText(currentProgress: Double):String
    }
    interface OnProgressChangeListener {
        fun onProgressChanged(progress: Double, maxProgress: Double)
    }

    companion object {
        const val DIRECTION_CLOCKWISE = 0
        const val DIRECTION_COUNTERCLOCKWISE = 1
        const val CAP_ROUND = 0
        const val CAP_BUTT = 1
        const val NO_GRADIENT = 0
        const val LINEAR_GRADIENT = 1
        const val RADIAL_GRADIENT = 2
        const val SWEEP_GRADIENT = 3
        private const val DEFAULT_PROGRESS_START_ANGLE = 270
        private const val ANGLE_START_PROGRESS_BACKGROUND = 0
        private const val ANGLE_END_PROGRESS_BACKGROUND = 360
        var DESIRED_WIDTH_DP = 350 //250 // should be calculated original value is 150
        private const val DEFAULT_PROGRESS_COLOR = "#3F51B5"
        private const val DEFAULT_TEXT_SIZE_SP = 0
        private const val DEFAULT_STROKE_WIDTH_DP = 3.0F
        private const val DEFAULT_PROGRESS_BACKGROUND_COLOR = "#e0e0e0"
        private const val DEFAULT_ANIMATION_DURATION = 1000
        private const val PROPERTY_ANGLE = "angle"
    }
}