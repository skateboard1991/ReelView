package com.skateboard.reelviewtest

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator


class ReelView(context: Context, attrs: AttributeSet?) : View(context, attrs)
{
    private lateinit var paint: Paint

    private var dis = 0

    private var textColor = Color.BLACK

    private var reelColor = Color.RED

    private var paperColor = Color.WHITE

    private var textSize = 20f

    private var text = ""

    private var reelWidth = 40f

    private var duration = 3000

    private lateinit var disAnimator: ValueAnimator

    private var isExpand = false

    private var reelTopBarHeight = 20f

    private var lineOffset = 10f


    constructor(context: Context) : this(context, null)

    init
    {
        if (attrs != null)
        {
            parseAttrs(attrs)
        }
        initPaint()
        initAnimator()
    }

    private fun parseAttrs(attrs: AttributeSet)
    {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ReelView)
        textColor = typedArray.getColor(R.styleable.ReelView_textColor, Color.BLACK)
        reelColor = typedArray.getColor(R.styleable.ReelView_reelColor, Color.RED)
        paperColor = typedArray.getColor(R.styleable.ReelView_paperColor, Color.WHITE)
        text = typedArray.getString(R.styleable.ReelView_text) ?: "hello world"
        reelWidth = typedArray.getDimension(R.styleable.ReelView_reelWidth, 40f)
        duration = typedArray.getInteger(R.styleable.ReelView_duration, 3000)
        textSize = typedArray.getDimension(R.styleable.ReelView_textSize, 20f)
        typedArray.recycle()

    }


    private fun initPaint()
    {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = 30f
        paint.style = Paint.Style.FILL
    }

    private fun initAnimator()
    {
        disAnimator = ValueAnimator.ofInt(0, duration / 1000)
        disAnimator.duration = duration.toLong()
        disAnimator.interpolator = AccelerateDecelerateInterpolator()
        disAnimator.addUpdateListener {

            dis = (it.animatedFraction * (width / 2 - reelWidth)).toInt()
            postInvalidate()
        }
    }


    override fun onDetachedFromWindow()
    {
        super.onDetachedFromWindow()
        disAnimator.cancel()
    }

    override fun onDraw(canvas: Canvas?)
    {
        super.onDraw(canvas)
        canvas?.let {

            val count = it.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
            drawText(it)
            drawPaper(it)
            paint.xfermode = null
            drawPaerLines(it)
            it.restoreToCount(count)
            drawReels(it)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        when (event?.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                if (!disAnimator.isRunning)
                {
                    if (!isExpand)
                    {
                        val centerX = width / 2.toFloat()
                        if (event.x >= centerX - reelWidth && event.x <= centerX + reelWidth)
                        {
                            startAnimator()
                            isExpand = true
                        }
                    }
                    else
                    {
                        disAnimator.reverse()
                        isExpand = false
                    }
                }
                return true
            }
        }
        return false
    }

    private fun startAnimator()
    {
        if (!disAnimator.isStarted)
        {
            disAnimator.start()
        }
    }


    private fun drawPaper(canvas: Canvas)
    {
        val centerX = width.toFloat() / 2
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvasTmp = Canvas(bitmap)
        paint.color = paperColor
        canvasTmp.drawRect(centerX - dis, reelTopBarHeight, centerX + dis, height - reelTopBarHeight, paint)
        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }

    private fun drawPaerLines(canvas: Canvas)
    {
        val centerX = width.toFloat() / 2
        canvas.drawLine(centerX - dis, reelTopBarHeight + lineOffset, centerX + dis, reelTopBarHeight + lineOffset, paint)
        canvas.drawLine(centerX - dis, height - (reelTopBarHeight + lineOffset), centerX + dis, height - (reelTopBarHeight + lineOffset), paint)
    }

    private fun drawText(canvas: Canvas)
    {
        textSize = Math.min(textSize, (height - reelTopBarHeight * 2 - lineOffset * 2))
        paint.isFakeBoldText = true
        paint.textSize=textSize
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, centerX - rect.width() / 2, centerY + rect.height() / 2, paint)
    }


    private fun drawReels(canvas: Canvas)
    {
        drawLeftReel(canvas)
        drawRightReel(canvas)
    }

    private fun drawLeftReel(canvas: Canvas)
    {
        val centerX = width / 2.toFloat()
        val left = centerX - dis - reelWidth
        val right = centerX - dis
        drawReel(canvas, left, right)
    }

    private fun drawRightReel(canvas: Canvas)
    {
        val centerX = width / 2.toFloat()
        val left = centerX + dis
        val right = left + reelWidth
        drawReel(canvas, left, right)
    }

    private fun drawReel(canvas: Canvas, left: Float, right: Float)
    {

        paint.color = reelColor
        canvas.drawRect(left, reelTopBarHeight, right, height.toFloat() - reelTopBarHeight, paint)
        paint.color = Color.BLACK
        canvas.drawRect(left + reelWidth / 4, 0f, right - reelWidth / 4, reelTopBarHeight, paint)
        canvas.drawRect(left + reelWidth / 4, height.toFloat() - reelTopBarHeight, right - reelWidth / 4, height.toFloat(), paint)
    }

}