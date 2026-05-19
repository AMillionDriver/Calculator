package com.axoloth.calculator.by.sky.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.axoloth.calculator.by.sky.logic.KMapLogic

class LogicGateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), NestedScrollingChild3 {

    private var rootNode: KMapLogic.LogicNode? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
        style = Paint.Style.STROKE
        textSize = 40f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF00E5FF.toInt()
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val gateWidth = 120f
    private val gateHeight = 80f
    private val nodeSpacingX = 200f
    private val nodeSpacingY = 120f

    // Zoom and Pan state
    private var scaleFactor = 1.0f
    private var translateX = 0f
    private var translateY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = -1

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 5.0f)
            invalidate()
            return true
        }
    })

    private val childHelper = NestedScrollingChildHelper(this).apply {
        isNestedScrollingEnabled = true
    }

    fun setLogicExpression(expression: String) {
        rootNode = KMapLogic.parseSOP(expression)
        // Reset view when expression changes
        scaleFactor = 1.0f
        translateX = 0f
        translateY = 0f
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                activePointerId = event.getPointerId(0)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                if (pointerIndex != -1) {
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)

                    if (!scaleDetector.isInProgress) {
                        val dx = x - lastTouchX
                        val dy = y - lastTouchY

                        translateX += dx
                        translateY += dy
                        invalidate()
                    }

                    lastTouchX = x
                    lastTouchY = y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.action == MotionEvent.ACTION_UP) {
                    performClick()
                }
                activePointerId = -1
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val node = rootNode ?: return

        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor, lastTouchX, lastTouchY)

        val startX = width - 150f
        val startY = height / 2f

        drawNode(canvas, node, startX, startY, 1)
        canvas.restore()
    }

    private fun drawNode(canvas: Canvas, node: KMapLogic.LogicNode, x: Float, y: Float, level: Int) {
        when (node) {
            is KMapLogic.LogicNode.Variable -> {
                canvas.drawCircle(x, y, 30f, paint)
                canvas.drawText(node.name.toString(), x, y + 10f, textPaint)
            }
            is KMapLogic.LogicNode.NotGate -> {
                drawGate(canvas, "NOT", x, y)
                val nextX = x - nodeSpacingX
                canvas.drawLine(x - gateWidth / 2f, y, nextX, y, paint)
                drawNode(canvas, node.input, nextX, y, level + 1)
            }
            is KMapLogic.LogicNode.AndGate -> {
                drawGate(canvas, "AND", x, y)
                drawInputs(canvas, node.inputs, x, y, level)
            }
            is KMapLogic.LogicNode.OrGate -> {
                drawGate(canvas, "OR", x, y)
                drawInputs(canvas, node.inputs, x, y, level)
            }
        }
    }

    private fun drawInputs(canvas: Canvas, inputs: List<KMapLogic.LogicNode>, x: Float, y: Float, level: Int) {
        val totalHeight = (inputs.size - 1) * nodeSpacingY
        var startY = y - totalHeight / 2f
        val nextX = x - nodeSpacingX

        for (input in inputs) {
            canvas.drawLine(x - gateWidth / 2f, y, nextX, startY, paint)
            drawNode(canvas, input, nextX, startY, level + 1)
            startY += nodeSpacingY
        }
    }

    private fun drawGate(canvas: Canvas, label: String, x: Float, y: Float) {
        val rect = RectF(x - gateWidth / 2f, y - gateHeight / 2f, x + gateWidth / 2f, y + gateHeight / 2f)
        canvas.drawRoundRect(rect, 10f, 10f, fillPaint)
        canvas.drawRoundRect(rect, 10f, 10f, paint)
        canvas.drawText(label, x, y + 10f, textPaint)
    }

    // NestedScrollingChild3 implementation
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int, consumed: IntArray) {
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean = childHelper.startNestedScroll(axes, type)
    override fun stopNestedScroll(type: Int) = childHelper.stopNestedScroll(type)
    override fun hasNestedScrollingParent(type: Int): Boolean = childHelper.hasNestedScrollingParent(type)
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean =
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int): Boolean =
        childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
}
