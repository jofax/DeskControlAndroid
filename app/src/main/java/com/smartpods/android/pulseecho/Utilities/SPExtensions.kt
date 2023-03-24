package com.smartpods.android.pulseecho.Utilities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.annotation.DimenRes
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.nio.ByteBuffer
import java.util.*
import kotlin.experimental.xor

val View.screenLocation
    get(): IntArray {
        val point = IntArray(2)
        getLocationOnScreen(point)
        return point
    }

val View.boundingBox
    get(): Rect {
        val (x, y) = screenLocation
        return Rect(x, y, x + width, y + height)
    }

fun Boolean.toInt() = if (this) 1 else 0

fun Intent.clearStack(additionalFlags: Int = 0) {
    flags = additionalFlags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}

fun String.isValidEmail() =
    !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

inline fun <T> T.guard(block: T.() -> Unit): T {
    if (this == null) block(); return this
}


fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun View.slideUp(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(0f, 0f, this.height.toFloat(), 0f)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.slideDown(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(0f, 0f, 0f, this.height.toFloat())
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}


fun View.slideLeft(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(this.width.toFloat(), 0f, 0f, 0f)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

fun View.slideRight(duration: Int = 500) {
    visibility = View.VISIBLE
    val animate = TranslateAnimation(0f, this.right.toFloat(), 0f, 0f)
    animate.duration = duration.toLong()
    animate.fillAfter = true
    this.startAnimation(animate)
}

enum class SlideDirection{
    UP,
    DOWN,
    LEFT,
    RIGHT
}

enum class SlideType{
    SHOW,
    HIDE
}

fun View.slideAnimation(direction: SlideDirection, type: SlideType, duration: Long = 250){
    val fromX: Float
    val toX: Float
    val fromY: Float
    val toY: Float
    val array = IntArray(2)
    getLocationInWindow(array)
    if((type == SlideType.HIDE && (direction == SlideDirection.RIGHT || direction == SlideDirection.DOWN)) ||
        (type == SlideType.SHOW && (direction == SlideDirection.LEFT || direction == SlideDirection.UP))   ){
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        val deviceHeight = displayMetrics.heightPixels
        array[0] = deviceWidth
        array[1] = deviceHeight
    }
    when (direction) {
        SlideDirection.UP -> {
            fromX = 0f
            toX = 0f
            fromY = if(type == SlideType.HIDE) 0f else (array[1] + height).toFloat()
            toY = if(type == SlideType.HIDE) -1f * (array[1] + height)  else 0f
        }
        SlideDirection.DOWN -> {
            fromX = 0f
            toX = 0f
            fromY = if(type == SlideType.HIDE) 0f else -1f * (array[1] + height)
            toY = if(type == SlideType.HIDE) 1f * (array[1] + height)  else 0f
        }
        SlideDirection.LEFT -> {
            fromX = if(type == SlideType.HIDE) 0f else 1f * (array[0] + width)
            toX = if(type == SlideType.HIDE) -1f * (array[0] + width) else 0f
            fromY = 0f
            toY = 0f
        }
        SlideDirection.RIGHT -> {
            fromX = if(type == SlideType.HIDE) 0f else -1f * (array[0] + width)
            toX = if(type == SlideType.HIDE) 1f * (array[0] + width) else 0f
            fromY = 0f
            toY = 0f
        }
    }
    val animate = TranslateAnimation(
        fromX,
        toX,
        fromY,
        toY
    )
    animate.duration = duration
    animate.setAnimationListener(object: Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            if(type == SlideType.HIDE){
                visibility = View.INVISIBLE
            }
        }

        override fun onAnimationStart(animation: Animation?) {
            visibility = View.VISIBLE
        }

    })
    startAnimation(animate)
}

fun Short.reverseBytes(): Short {
    val v0 = ((this.toInt() ushr 0) and 0xFF)
    val v1 = ((this.toInt() ushr 8) and 0xFF)
    return ((v1 and 0xFF) or (v0 shl 8)).toShort()
}

fun Int.reverseBytes(): Int {
    val v0 = ((this ushr 0) and 0xFF)
    val v1 = ((this ushr 8) and 0xFF)
    val v2 = ((this ushr 16) and 0xFF)
    val v3 = ((this ushr 24) and 0xFF)
    return (v0 shl 24) or (v1 shl 16) or (v2 shl 8) or (v3 shl 0)
}

fun Long.reverseBytes(): Long {
    val v0 = (this ushr 0).toInt().reverseBytes().toLong() and 0xFFFFFFFFL
    val v1 = (this ushr 32).toInt().reverseBytes().toLong() and 0xFFFFFFFFL
    return (v0 shl 32) or (v1 shl 0)
}

fun byteToInt(bytes: ByteArray): Int {
    var result = 0
    var shift = 0
    for (byte in bytes) {
        result = result or (byte.toInt() shl shift)
        shift += 8
    }
    return result
}

fun toInt32(bytes:ByteArray):Int {
    if (bytes.size != 4) {
        throw Exception("wrong len")
    }
    bytes.reverse()
    return ByteBuffer.wrap(bytes).int
}

fun View?.show(visible: Boolean) {
    if (visible) show() else gone()
}

fun View?.hide(hide: Boolean) {
    if (hide) hide() else show()
}

fun View?.gone(gone: Boolean = true) {
    if (gone) gone() else show()
}

fun View?.show() {
    if (this == null) return
    if (!isVisible) isVisible = true
}

fun View?.hide() {
    if (this == null) return
    if (!isInvisible) isInvisible = true
}

fun View?.gone() {
    if (this == null) return
    if (!isGone) isGone = true
}

fun View.setMargin(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    val params = (layoutParams as? ViewGroup.MarginLayoutParams)
    params?.setMargins(
        left ?: params.leftMargin,
        top ?: params.topMargin,
        right ?: params.rightMargin,
        bottom ?: params.bottomMargin)
    layoutParams = params
}

fun View.setTopMargin(@DimenRes dimensionResId: Int) {
    (layoutParams as ViewGroup.MarginLayoutParams).topMargin = resources.getDimension(dimensionResId).toInt()
}

fun View.addLayoutMargins(left: Int? = null, top: Int? = null,
                          right: Int? = null, bottom: Int? = null) {
    this.layoutParams = ViewGroup.MarginLayoutParams(this.layoutParams)
        .apply {
            left?.let { leftMargin = it }
            top?.let { topMargin = it }
            right?.let { rightMargin = it }
            bottom?.let { bottomMargin = it }
        }
}

fun RecyclerView.addScrollListener(onScroll: (position: Int, left: Boolean, right: Boolean) -> Unit) {
    var lastPosition = 0
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (layoutManager is LinearLayoutManager) {
                val currentVisibleItemPosition =
                    (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                if (lastPosition != currentVisibleItemPosition && currentVisibleItemPosition != RecyclerView.NO_POSITION) {
                    var mLeft = false
                    var mRight = false
                    if(dy > 0){
                        //println("SCROLL => RIGHT")
                        mLeft = false
                        mRight = true
                    }else{
                        //println("SCROLL => LEFT")
                        mLeft = true
                        mRight = false
                    }

                    onScroll.invoke(currentVisibleItemPosition, mLeft, mRight)
                    lastPosition = currentVisibleItemPosition
                }
            }
        }
    })
}

fun RecyclerView?.getCurrentPosition() : Int {
    return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
}

infix fun Byte.shl(that: Int): Int = this.toInt().shl(that)
infix fun Int.shl(that: Byte): Int = this.shl(that.toInt()) // Not necessary in this case because no there's (Int shl Byte)
infix fun Byte.shl(that: Byte): Int = this.toInt().shl(that.toInt()) // Not necessary in this case because no there's (Byte shl Byte)

infix fun Byte.and(that: Int): Int = this.toInt().and(that)
infix fun Int.and(that: Byte): Int = this.and(that.toInt()) // Not necessary in this case because no there's (Int and Byte)
infix fun Byte.and(that: Byte): Int = this.toInt().and(that.toInt()) // Not necessary in this case because no there's (Byte and Byte)

infix fun UShort.shl(bitCount: Int): UShort = (this.toUInt() shl bitCount).toUShort()
infix fun UShort.shr(bitCount: Int): UShort = (this.toUInt() shr bitCount).toUShort()

infix fun UByte.shl(bitCount: Int): UByte = (this.toUInt() shl bitCount).toUByte()
infix fun UByte.shr(bitCount: Int): UByte = (this.toUInt() shr bitCount).toUByte()
infix fun UShort.ushr(bitCount: Int): UShort = (this.toUInt() shr bitCount).toUShort()

fun Int.toByteArray(): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
fun Double.toByteArray(): ByteArray = ByteBuffer.allocate(Double.SIZE_BYTES).putDouble(this).array()
fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

fun UByte.toBigEndianUShort(): UShort = this.toUShort() shl 8
fun UByte.toBigEndianUInt(): UInt = this.toUInt() shl 24
fun Byte.toPositiveInt() = toInt() and 0xFF

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun ByteArray.toHex() : String{
    val result = StringBuffer()

    forEach {
        val st = String.format("%02x", it)
        result.append(st)
    }

    return result.toString()
}

fun String.hexStringToByteArray() : ByteArray {
    val HEX_CHARS_STR = "0123456789abcdef"
    val HEX_CHARS = HEX_CHARS_STR.toCharArray()
    val result = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        val firstIndex = HEX_CHARS_STR.indexOf(this[i]);
        val secondIndex = HEX_CHARS_STR.indexOf(this[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}

fun ByteArray.stringFromUtf8(start: Int = 0, size: Int = this.size) : String = stringFromUtf8(start, size)
fun ByteArray.charArrayToHex(charArray: CharArray): String {
    val hex = CharArray(2 * this.size)
    this.forEachIndexed { i, byte ->
        val unsigned = 0xff and byte.toInt()
        hex[2 * i] = charArray[unsigned / 16]
        hex[2 * i + 1] = charArray[unsigned % 16]
    }

    return hex.joinToString("")
}
@ExperimentalUnsignedTypes
fun ByteArray.toUnsignedHexString(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }

//private val crcTable = ShortArray(256)
//
//fun calculateCrc16(buffer: ByteArray): Short {
//    return calculateCrc16(buffer, 0, buffer.size)
//}
//
//fun calculateCrc16(buffer: ByteArray, offset: Int, length: Int): Short {
//    var crc = 0xffff.toShort() //x25 CCITT-CRC16 seed
//    for (i in 0 until length) {
//        crc = (crcTable[buffer[i + offset] xor (crc ushr 8) and 0xff] xor (crc shl 8)) as Short
//    }
//    return crc
//}