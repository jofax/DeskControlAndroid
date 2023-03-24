package com.smartpods.android.pulseecho.CustomUI

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable


class SPCustomHeartView(context: Context?) : View(context) {
    private lateinit var heart_outline_paint: Paint
    private var path: Path
    private val paint: Paint

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Fill the canvas with background color
        canvas.drawColor(Color.GRAY)
        paint.shader = null

        // Defining of  the heart path starts
        path.moveTo((left + WIDTH / 2).toFloat(), (top + HEIGHT / 4).toFloat()) // Starting point
        // Create a cubic Bezier cubic left path
        path.cubicTo(
            (left + WIDTH / 5).toFloat(), top.toFloat(), (
                    left + WIDTH / 4).toFloat(), (top + 4 * HEIGHT / 5).toFloat(), (
                    left + WIDTH / 2).toFloat(), (top + HEIGHT).toFloat()
        )
        // This is right Bezier cubic path
        path.cubicTo(
            (left + 3 * WIDTH / 4).toFloat(), (top + 4 * HEIGHT / 5).toFloat(), (
                    left + 4 * WIDTH / 5).toFloat(), top.toFloat(), (
                    left + WIDTH / 2).toFloat(), (top + HEIGHT / 4).toFloat()
        )
        paint.color = Color.RED // Set with heart color
        //paint.setShader(shader);
        paint.style = Paint.Style.FILL // Fill with heart color
        canvas.drawPath(path, paint) // Actual drawing happens here

        // Draw Blue Boundary
        paint.shader = null
        paint.color = Color.BLUE // Change the boundary color
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)

//        heart_outline_paint = Paint(Paint.ANTI_ALIAS_FLAG)
//        heart_outline_paint.setStrokeJoin(Paint.Join.MITER)
//        heart_outline_paint.setColor(Color.RED) // Change the boundary color
//
//        heart_outline_paint.setStrokeWidth(15F)
//        heart_outline_paint.setStyle(Paint.Style.STROKE)
//        path = Path()
//
//        val length = 100f
//        val x = (canvas.width / 2).toFloat()
//        val y = (canvas.height / 2).toFloat()
//
//        canvas.rotate(45F, x, y)
//
//        path.moveTo(x, y)
//        path.lineTo(x - length, y)
//        path.arcTo(RectF(x - length - length / 2, y - length, x - length / 2, y), 90f, 180f)
//        path.arcTo(RectF(x - length, y - length - length / 2, x, y - length / 2), 180f, 180f)
//        path.lineTo(x, y)
//        path.close()
//
//        canvas.drawPath(path, heart_outline_paint)
    }

    companion object {
        private const val WIDTH = 500
        private const val HEIGHT = 300
    }

    init {
        path = Path()
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        top = 10
        left = 10
    }
}

class HeartShapeObject : FrameLayout {
    private var paint: Paint? = null

    constructor(@NonNull context: Context?) : super(context!!) {
        init()
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.color = Color.RED
        paint!!.style = Paint.Style.STROKE
        setWillNotDraw(false)
    }

    constructor(@NonNull context: Context?, @Nullable attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(
        @NonNull context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    constructor(
        @NonNull context: Context?,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context!!, attrs, defStyleAttr, defStyleRes
    ) {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        val path = createHeartPath(canvas.clipBounds.right, canvas.clipBounds.bottom)
        canvas.drawPath(path, paint!!)
        super.onDraw(canvas)
    }

    private fun createHeartPath(width: Int, height: Int): Path {
        val path = Path()
        path.moveTo(0F, height / 3f)
        path.lineTo(width.toFloat(), height / 3f)
        path.moveTo(width / 2f, 0f)
        path.lineTo(width / 2f, height.toFloat())
        val pX = width / 2f
        val pY = height / 100f * 33.33f
        var x1 = width / 100f * 50
        var y1 = height / 100f * 5
        var x2 = width / 100f * 90
        var y2 = height / 100f * 10
        var x3 = width / 100f * 90
        var y3 = height / 100f * 33.33f
        path.moveTo(pX, pY)
        path.cubicTo(x1, y1, x2, y2, x3, y3)
        path.moveTo(x3, pY)
        x1 = width / 100f * 90
        y1 = height / 100f * 55f
        x2 = width / 100f * 65
        y2 = height / 100f * 60f
        x3 = width / 100f * 50
        y3 = height / 100f * 90f
        path.cubicTo(x1, y1, x2, y2, x3, y3)
        // path.lineTo(pX,pY);
        x1 = width / 100f * 50
        y1 = height / 100f * 5
        x2 = width / 100f * 10
        y2 = height / 100f * 10
        x3 = width / 100f * 10
        y3 = height / 100f * 33.33f
        path.moveTo(pX, pY)
        path.cubicTo(x1, y1, x2, y2, x3, y3)
        path.moveTo(x3, pY)
        x1 = width / 100f * 10
        y1 = height / 100f * 55f
        x2 = width / 100f * 35f
        y2 = height / 100f * 60f
        x3 = width / 100f * 50f
        y3 = height / 100f * 90f
        path.cubicTo(x1, y1, x2, y2, x3, y3)
        //path.lineTo(pX,pY);
        path.moveTo(x3, y3)
        path.close()
        return path
    }
}