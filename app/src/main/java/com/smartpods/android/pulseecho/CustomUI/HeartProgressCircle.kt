package com.smartpods.android.pulseecho.CustomUI

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.*
import androidx.annotation.IntRange
import com.smartpods.android.pulseecho.R
import com.smartpods.android.pulseecho.Utilities.Utilities
import kotlinx.coroutines.runBlocking
import kotlin.math.min
import kotlin.math.PI

internal open class HeartProgressCircleAnimatorListener: Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator, isReverse:Boolean) {
    }
    override fun onAnimationEnd(animation: Animator, isReverse:Boolean) {
    }
    override fun onAnimationStart(animation: Animator) {
    }
    override fun onAnimationEnd(animation: Animator) {
    }
    override fun onAnimationCancel(animation: Animator) {
    }
    override fun onAnimationRepeat(animation: Animator) {
    }
}

class HeartProgressCircle: View{
    private var radius:Float = 0.toFloat()
    private var sitPaintColor: Paint = Paint()
    private var standPaintColor: Paint = Paint()
    private var progressBackgroundPaint:Paint = Paint()
    private var progressPaint: Paint = Paint()
    private var sweepAngle = 0
    var circleBounds: RectF  = RectF()
    var movements:List<Map<String, Int>> = listOf()

    var startAngle = HeartProgressCircle.DEFAULT_PROGRESS_START_ANGLE
        set(@IntRange(from = 0, to = 360) startAngle) {
            field = startAngle
            invalidate()
        }

    lateinit var progressAnimator: ValueAnimator
    @Nullable
    @get:Nullable
    var mProgressChangeListener: HeartProgressCircle.OnProgressChangeListener? = null
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

    private fun init(@NonNull context: Context, @Nullable attrs: AttributeSet) {
        var progressStrokeCap = Paint.Cap.BUTT
        var progressStrokeWidth = dp2px(10.0F)
        val progressColor = Utilities.getCustomRawColor(R.color.smartpods_gray)

        sitPaintColor = Paint()
        sitPaintColor.strokeCap = progressStrokeCap
        sitPaintColor.strokeWidth = progressStrokeWidth.toFloat()
        sitPaintColor.style = Paint.Style.STROKE
        sitPaintColor.color = Utilities.getCustomRawColor(R.color.smartpods_bluish_white)
        sitPaintColor.isAntiAlias = true

        standPaintColor = Paint()
        standPaintColor.strokeCap = progressStrokeCap
        standPaintColor.strokeWidth = progressStrokeWidth.toFloat()
        standPaintColor.style = Paint.Style.STROKE
        standPaintColor.color = Utilities.getCustomRawColor(R.color.smartpods_blue)
        standPaintColor.isAntiAlias = true

        val progressBackgroundStyle = Paint.Style.FILL_AND_STROKE
        progressBackgroundPaint = Paint()
        progressBackgroundPaint.style = progressBackgroundStyle
        progressBackgroundPaint.strokeWidth = 10.0.toFloat()
        progressBackgroundPaint.color = Color.TRANSPARENT
        progressBackgroundPaint.isAntiAlias = true

        progressPaint = Paint()
        progressPaint.strokeCap = progressStrokeCap
        progressPaint.strokeWidth = progressStrokeWidth.toFloat()
        progressPaint.style = Paint.Style.STROKE
        progressPaint.color = progressColor
        progressPaint.isAntiAlias = true

        circleBounds = RectF()
        progressAnimator = ValueAnimator()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (progressAnimator != null)
        {
            progressAnimator.cancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
       // runBlocking {
            createPeriodicProgressFromMovements(canvas)
            drawProgress(canvas)
        //}
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        calculateBounds(w, h)
    }

    private fun calculateBounds(w: Int, h: Int) {
        radius = w / 2f
        val progressWidth = progressPaint.strokeWidth
        val progressBackgroundWidth = progressBackgroundPaint.strokeWidth
        val strokeSizeOffset = progressWidth.coerceAtLeast(progressBackgroundWidth)
        val halfOffset = strokeSizeOffset / 2f
        circleBounds.left = halfOffset
        circleBounds.top = halfOffset
        circleBounds.right = w - halfOffset
        circleBounds.bottom = h - halfOffset
        radius = circleBounds.width() / 2f
    }

    fun getListener(): HeartProgressCircle.OnProgressChangeListener? {
        return mProgressChangeListener
    }

    fun setListener(_mListener: HeartProgressCircle.OnProgressChangeListener?) {
        if (_mListener != null) {
            this.mProgressChangeListener = _mListener
        }
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.drawArc(circleBounds, this.startAngle.toFloat(),
            sweepAngle.toFloat(), false, progressPaint)
    }

    private fun createPeriodicProgressFromMovements(canvas: Canvas) {
        //println("rectangle : $circleBounds")

        for(item in movements) {
            val _key = if (item["key"] != null) item["key"] else 0
            val _start = if (item["start"] != null) item["start"] else 0
            val _angle = if (item["sweep"] != null) item["sweep"] else 0

//            println("item : $item")
//            println("startAngle : $_start")
//            println("endAngle : $_angle")

            if (_start != null) {
                if (_angle != null) {
                    if (_key != null) {
                        //println("key : $_key")
                        if (_key === 7) {
                            canvas.drawArc(
                                circleBounds, _start.toFloat(),
                                _angle.toFloat(),
                                false, sitPaintColor
                            )
                        } else {
                            canvas.drawArc(
                                circleBounds, _start.toFloat(),
                                _angle.toFloat(),
                                false, standPaintColor
                            )
                        }
                    }
                }
            }
        }
    }

    fun invalidateEverything() {
        calculateBounds(width, height)
        requestLayout()
        invalidate()
    }

    fun setCurrentProgress(currentProgress: Double) {
        if (currentProgress > maxProgress)
        {
            maxProgress = currentProgress
        }
        setProgress(currentProgress, maxProgress)
    }
    fun setProgress(current: Double, max: Double) {
        val finalAngle:Double = current / max * 360
        val oldCurrentProgress = progress
        maxProgress = max
        progress = current.coerceAtMost(max)
        mProgressChangeListener?.onProgressChanged(progress, maxProgress)
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
            HeartProgressCircle.PROPERTY_ANGLE,
            sweepAngle,
            finalAngle.toInt()
        )
        progressAnimator = ValueAnimator.ofObject(TypeEvaluator<Double> { fraction, startValue, endValue -> (startValue + (endValue - startValue) * fraction) }, oldCurrentProgress, progress)
        progressAnimator.duration = HeartProgressCircle.DEFAULT_ANIMATION_DURATION.toLong()
        progressAnimator.setValues(angleProperty)
        progressAnimator.interpolator = interpolator
        progressAnimator.addUpdateListener { animation ->
            sweepAngle = animation.getAnimatedValue(HeartProgressCircle.PROPERTY_ANGLE) as Int
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

    fun setProgressStrokeWidthDp(@Dimension strokeWidth: Int) {
        setProgressStrokeWidthPx(dp2px(strokeWidth.toFloat()))
    }
    fun setProgressStrokeWidthPx(@Dimension strokeWidth: Int) {
        progressPaint.strokeWidth = strokeWidth.toFloat()
        invalidateEverything()
    }

    fun dp2px(dp: Float):Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }

    interface OnProgressChangeListener {
        fun onProgressChanged(progress: Double, maxProgress: Double)
    }

    companion object {
        private const val DEFAULT_PROGRESS_START_ANGLE = 270
        private const val PROPERTY_ANGLE = "angle"
        private const val DEFAULT_ANIMATION_DURATION = 3600
    }

}
