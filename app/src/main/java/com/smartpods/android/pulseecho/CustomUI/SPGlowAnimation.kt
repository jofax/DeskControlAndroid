package com.smartpods.android.pulseecho.CustomUI

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.smartpods.android.pulseecho.R

class SPGlowAnimation: FrameLayout {
    var animatedLayers = 0
    var color1 = 0
    var color2 = 0
    var color3 = 0
    var color4 = 0
    var color5 = 0
    var cornerRadius = 0f
    var duration = 0L
    var startDelay = 0L
    var scaleMax = 0f

    val animatedViews = mutableListOf<AnimatedView>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setAttributes(attrs)
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (childCount > animatedLayers + 1) throw IllegalArgumentException("Too many views. SPGlowAnimation can contain only one view.")

        val child = getChildAt(animatedLayers)

        animatedViews.forEachIndexed { index, _ ->
            val animatedView = getChildAt(index)
            val lpAnimatedView = animatedView.layoutParams
            lpAnimatedView.width = child.measuredWidth
            lpAnimatedView.height = child.measuredHeight
            animatedView.setPadding(child.paddingLeft, child.paddingTop, child.paddingRight, child.paddingBottom)
            animatedView.layoutParams = lpAnimatedView
            animatedView.x = child.x
            animatedView.y = child.y
        }

    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    private fun setAttributes(attrs: AttributeSet) {
        val attrsTypedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SPGlowAnimation, 0, 0)

        animatedLayers = attrsTypedArray.getInt(R.styleable.SPGlowAnimation_layers, 0)
        color1 = attrsTypedArray.getColor(R.styleable.SPGlowAnimation_color1, 0)
        color2 = attrsTypedArray.getColor(R.styleable.SPGlowAnimation_color2, 0)
        color3 = attrsTypedArray.getColor(R.styleable.SPGlowAnimation_color3, 0)
        color4 = attrsTypedArray.getColor(R.styleable.SPGlowAnimation_color4, 0)
        color5 = attrsTypedArray.getColor(R.styleable.SPGlowAnimation_color5, 0)
        cornerRadius = attrsTypedArray.getDimension(R.styleable.SPGlowAnimation_android_radius, 0f)
        duration = attrsTypedArray.getInt(R.styleable.SPGlowAnimation_duration, 0).toLong()
        startDelay = attrsTypedArray.getInt(R.styleable.SPGlowAnimation_startDelay, 0).toLong()
        scaleMax = attrsTypedArray.getFloat(R.styleable.SPGlowAnimation_scaleMax, 0f)

        attrsTypedArray.recycle()

        for (index in 0 until animatedLayers) {
            val color = when (index.rem(5)) {
                0 -> color1
                1 -> color2
                2 -> color3
                3 -> color4
                4 -> color5
                else -> throw IllegalArgumentException()
            }
            val cardView = CardView(context).apply {
                setCardBackgroundColor(color)
                radius = this@SPGlowAnimation.cornerRadius
                elevation = 0f
                visibility = View.GONE
            }

            val actualDelay = calcDelay(index)
            val oaScaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, this.scaleMax).apply {
                duration = this@SPGlowAnimation.duration
                startDelay = actualDelay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }

            val oaScaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, this.scaleMax).apply {
                duration = this@SPGlowAnimation.duration
                startDelay = actualDelay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }

            val oaAlpha = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0f).apply {
                duration = this@SPGlowAnimation.duration
                startDelay = actualDelay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }

            val av = AnimatedView(cardView, oaScaleX, oaScaleY, oaAlpha)
            this.animatedViews.add(av)
            this.addView(av.cardView)
        }

    }

    private fun calcDelay(actualLayer: Int): Long {
        return this.startDelay + (this.duration * 0.3 * actualLayer).toLong()
    }

    fun startAnimation() {
        animatedViews.forEach { it.animate() }
    }

    fun stopAnimation() {
        animatedViews.forEach { it.stop() }
    }

    data class AnimatedView(
        val cardView: CardView,
        val oaScaleX: ObjectAnimator,
        val oaScaleY: ObjectAnimator,
        val oaAlpha : ObjectAnimator
    ) {
        fun animate() {
            cardView.visibility = View.VISIBLE
            oaScaleX.start()
            oaScaleY.start()
            oaAlpha.start()
        }

        fun stop() {
            cardView.visibility = View.GONE
            oaScaleX.cancel()
            oaScaleY.cancel()
            oaAlpha.cancel()
        }
    }
}