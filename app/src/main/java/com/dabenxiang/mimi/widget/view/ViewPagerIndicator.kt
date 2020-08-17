package com.dabenxiang.mimi.widget.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback

class ViewPagerIndicator(context: Context, attrs: AttributeSet) : View(context, attrs) {
    companion object {
        /**
         * 常量用來計算繪製圓形貝塞爾曲線控製點的位置
         */
        private const val M = 0.551915024494f
    }

    init {
        setDefaultStyleable(context, attrs)
        initPaint()
    }

    private var mPath: Path? = null
    private var paintFill: Paint? = null
    private var paintStroke: Paint? = null
    private var mNum = 0 //個數
    private var mRadius = 0f //半徑
    private var mRadiusSelected = 0f //選中半徑，默認為mRadius
    private var mLength = 0f //線長
    private val mHeight = 0f //線寬
    private var mOffset = 0f //偏移量
    private var mSelected_color = 0 //選中顏色
    private var mDefault_color = 0 //默認顏色
    private var mIndicatorType = 0 //點類型
    private var mDistanceType = 0 //距離類型
    private var mDistance = 0f //間隔距離
    private var mPosition = 0//第幾張
    private var mPercent = 0f
    private var mIsLeft = false
    private var mIsInfiniteCircle = false//無限循環
    private var mAnimation = false

    private fun setDefaultStyleable(context: Context, attrs: AttributeSet) {
        mSelected_color = Color.BLACK
        mDefault_color = Color.parseColor("#ffcdcdcd")
        mRadius = 12f
        mRadiusSelected = mRadius
        mLength = 2 * mRadius
        mDistance = 3 * mRadius
        mDistanceType = DistanceType.BY_RADIUS
        mIndicatorType = IndicatorType.CIRCLE_LINE
        mNum = 0
        mAnimation = true
        when (mIndicatorType) {
            IndicatorType.BEZIER -> mControlPoint =
                arrayOf(
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point()
                )
            IndicatorType.SPRING -> mSpringPoint =
                arrayOf(
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point(),
                    Point()
                )
        }
        invalidate()
    }

    /**
     * 初始化畫筆
     */
    private fun initPaint() {
        paintStroke = Paint()
        paintFill = Paint()
        mPath = Path()
        //實心
        paintFill!!.style = Paint.Style.FILL_AND_STROKE
        paintFill!!.color = mSelected_color
        paintFill!!.isAntiAlias = true
        paintFill!!.strokeWidth = 3f
        //空心
        paintStroke!!.style = Paint.Style.FILL
        paintStroke!!.color = mDefault_color
        paintStroke!!.isAntiAlias = true
        paintStroke!!.strokeWidth = 3f
    }

    /**
     * 繪製   invalidate()後 執行
     *
     * @param canvas
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mNum <= 0) {
            return
        }
        val width = canvas.width
        val height = canvas.height
        canvas.translate(width / 2.toFloat(), height / 2.toFloat())
        //初始化畫筆
        initPaint()
        when (mDistanceType) {
            DistanceType.BY_DISTANCE -> {
            }
            DistanceType.BY_RADIUS -> mDistance = 3 * mRadius
            DistanceType.BY_LAYOUT -> mDistance = if (mIndicatorType == IndicatorType.CIRCLE_LINE) {
                width / (mNum + 1).toFloat()
            } else {
                width / mNum.toFloat()
            }
        }
        when (mIndicatorType) {
            IndicatorType.CIRCLE -> {
                var i = 0
                while (i < mNum) {
                    //默認點 -(mNum - 1) * 0.5f * mDistance 第一個點
                    canvas.drawCircle(
                        -(mNum - 1) * 0.5f * mDistance + i * mDistance,
                        0f,
                        mRadius,
                        paintStroke!!
                    )
                    i++
                }
                //選中
                canvas.drawCircle(
                    -(mNum - 1) * 0.5f * mDistance + mOffset,
                    0f,
                    mRadiusSelected,
                    paintFill!!
                )
            }
            IndicatorType.LINE -> {
                paintStroke!!.strokeWidth = mRadius
                val startX = -(mNum - 1) * 0.5f * mDistance - mLength / 2
                val stopX = -(mNum - 1) * 0.5f * mDistance + mLength / 2
                //默認
                var i = 0
                while (i < mNum) {
                    canvas.drawLine(
                        startX + i * mDistance,
                        0f,
                        stopX + i * mDistance,
                        0f,
                        paintStroke!!
                    )
                    i++
                }
                //選中
                paintFill!!.strokeWidth = mRadius
                val startF = -(mNum - 1) * 0.5f * mDistance - mLength / 2 + mOffset
                val stopF = -(mNum - 1) * 0.5f * mDistance + mLength / 2 + mOffset
                canvas.drawLine(startF, 0f, stopF, 0f, paintFill!!)
            }
            IndicatorType.CIRCLE_LINE -> if (mPosition == mNum - 1) { //最後一個 右滑
                //第一個 線 選中 消失
                val leftClose = -mNum * 0.5f * mDistance - mRadius
                val rightClose = leftClose + 2 * mRadius + mOffset
                val topClose = -mRadius
                val bottomClose = mRadius
                val rectClose = RectF(leftClose, topClose, rightClose, bottomClose) // 設置個新的長方形
                canvas.drawRoundRect(rectClose, mRadius, mRadius, paintStroke!!)
                //最後一個 線  顯示
                val rightOpen = -mNum * 0.5f * mDistance + mNum * mDistance + mRadius
                val leftOpen = rightOpen - 2 * mRadius - mDistance + mOffset
                val topOpen = -mRadius
                val bottomOpen = mRadius
                val rectOpen = RectF(leftOpen, topOpen, rightOpen, bottomOpen) // 設置個新的長方形
                canvas.drawRoundRect(rectOpen, mRadius, mRadius, paintStroke!!)
                //圓
                var i = 1
                while (i < mNum) {
                    canvas.drawCircle(
                        rightClose - mRadius + i * mDistance,
                        0f,
                        mRadius,
                        paintStroke!!
                    )
                    i++
                }
            } else {
                //第一個 線 選中 消失
                val leftClose = -mNum * 0.5f * mDistance + mPosition * mDistance - mRadius
                val rightClose = leftClose + 2 * mRadius + mDistance - mOffset
                val topClose = -mRadius
                val bottomClose = mRadius
                val rectClose = RectF(leftClose, topClose, rightClose, bottomClose) // 設置個新的長方形
                canvas.drawRoundRect(rectClose, mRadius, mRadius, paintStroke!!)
                //第二個 線  顯示
                if (mPosition < mNum - 1) {
                    val rightOpen = -mNum * 0.5f * mDistance + (mPosition + 2) * mDistance + mRadius
                    val leftOpen = rightOpen - 2 * mRadius - mOffset
                    val topOpen = -mRadius
                    val bottomOpen = mRadius
                    val rectOpen = RectF(leftOpen, topOpen, rightOpen, bottomOpen) // 設置個新的長方形
                    canvas.drawRoundRect(rectOpen, mRadius, mRadius, paintStroke!!)
                }
                //圓
                run {
                    var i = mPosition + 3
                    while (i <= mNum) {
                        canvas.drawCircle(
                            -mNum * 0.5f * mDistance + i * mDistance,
                            0f,
                            mRadius,
                            paintStroke!!
                        )
                        i++
                    }
                }
                var i = mPosition - 1
                while (i >= 0) {
                    canvas.drawCircle(
                        -mNum * 0.5f * mDistance + i * mDistance,
                        0f,
                        mRadius,
                        paintStroke!!
                    )
                    i--
                }
            }
            IndicatorType.BEZIER -> {
                var i = 0
                while (i < mNum) {
                    //默認點 -(mNum - 1) * 0.5f * mDistance 第一個點
                    canvas.drawCircle(
                        -(mNum - 1) * 0.5f * mDistance + i * mDistance,
                        0f,
                        mRadius,
                        paintStroke!!
                    )
                    i++
                }
                //選中
                drawCubicBezier(canvas)
            }
            IndicatorType.SPRING -> {
                var i = 0
                while (i < mNum) {
                    //默認點 -(mNum - 1) * 0.5f * mDistance 第一個點
                    canvas.drawCircle(
                        -(mNum - 1) * 0.5f * mDistance + i * mDistance,
                        0f,
                        mRadius,
                        paintStroke!!
                    )
                    i++
                }
                drawSpringBezier(canvas)
            }
            IndicatorType.PROGRESS -> {
                var i = 0
                while (i < mNum) {
                    //默認點 -(mNum - 1) * 0.5f * mDistance 第一個點
                    canvas.drawCircle(
                        -(mNum - 1) * 0.5f * mDistance + i * mDistance,
                        0f,
                        mRadius,
                        paintStroke!!
                    )
                    i++
                }
                //選中
                val rightOpen = -(mNum - 1) * 0.5f * mDistance + mOffset + mRadius
                val leftOpen = -(mNum - 1) * 0.5f * mDistance - mRadius
                val topOpen = -mRadius
                val bottomOpen = mRadius
                val rectOpen = RectF(leftOpen, topOpen, rightOpen, bottomOpen) // 設置個新的長方形
                canvas.drawRoundRect(rectOpen, mRadius, mRadius, paintFill!!)
            }
        }
    }

    private var mSpringPoint =
        arrayOfNulls<Point>(6)

    /**
     * 繪製彈性
     *
     * @param canvas
     */
    private fun drawSpringBezier(canvas: Canvas) {
        //右圓圓心
        val right_circle_x: Float
        //右圓半徑
        val right_circle_radius: Float
        //左圓圓心
        val left_circle_x: Float
        //左圓半徑
        val left_circle_radius: Float
        //最大半徑
        val max_radius = mRadius
        //最小半徑
        val min_radius = mRadius / 2
        //控製點
        if (mPosition == mNum - 1 && !mIsLeft) { //第一個 右滑  0---4
            if (mPercent <= 0.5) {
                right_circle_x = -(mNum - 1) * 0.5f * mDistance + (mNum - 1) * mDistance
                left_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + (0.5f - mPercent) / 0.5f * (mNum - 1) * mDistance
                right_circle_radius =
                    min_radius + (max_radius - min_radius) * (0.5f - mPercent) / 0.5f
            } else {
                right_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + (1f - mPercent) / 0.5f * (mNum - 1) * mDistance
                left_circle_x = -(mNum - 1) * 0.5f * mDistance
                right_circle_radius = min_radius
            }
            left_circle_radius = mRadius * mPercent
        } else if (mPosition == mNum - 1 && mIsLeft) { //最後一個 左滑 4--0
            //0-1
            if (mPercent >= 0.5) { //左亭
                left_circle_radius =
                    min_radius + (max_radius - min_radius) * (-0.5f + mPercent) / 0.5f
                left_circle_x = -(mNum - 1) * 0.5f * mDistance
                right_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + (1 - mPercent) / 0.5f * (mNum - 1) * mDistance
            } else { //左動
                left_circle_radius = min_radius
                left_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + (0.5f - mPercent) / 0.5f * (mNum - 1) * mDistance
                right_circle_x = -(mNum - 1) * 0.5f * mDistance + (mNum - 1) * mDistance
            }
            right_circle_radius = mRadius * (1 - mPercent)
        } else if (mIsLeft) { //中間的 左滑
            mOffset = (mPercent + mPosition) * mDistance
            if (mPercent >= 0.5) {
                left_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + ((mPercent - 0.5f) / 0.5f + mPosition) * mDistance
                right_circle_x = -(mNum - 1) * 0.5f * mDistance + (1 + mPosition) * mDistance
                right_circle_radius =
                    min_radius + (max_radius - min_radius) * (mPercent - 0.5f) / 0.5f
            } else {
                right_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + (mPercent / 0.5f + mPosition) * mDistance
                left_circle_x = -(mNum - 1) * 0.5f * mDistance + mPosition * mDistance
                right_circle_radius = min_radius
            }
            left_circle_radius = mRadius * (1 - mPercent)
        } else { //右滑
            mOffset = (mPercent + mPosition) * mDistance
            if (mPercent <= 0.5) {
                left_circle_x = -(mNum - 1) * 0.5f * mDistance + mPosition * mDistance
                right_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + (mPercent / 0.5f + mPosition) * mDistance
                left_circle_radius =
                    min_radius + (max_radius - min_radius) * (0.5f - mPercent) / 0.5f
            } else {
                left_circle_x =
                    -(mNum - 1) * 0.5f * mDistance + ((mPercent - 0.5f) / 0.5f + mPosition) * mDistance
                right_circle_x = -(mNum - 1) * 0.5f * mDistance + (mPosition + 1) * mDistance
                left_circle_radius = min_radius
            }
            right_circle_radius = mRadius * mPercent
        }
        //右圓
        canvas.drawCircle(right_circle_x, 0f, right_circle_radius, paintFill!!)
        //左圓
        canvas.drawCircle(left_circle_x, 0f, left_circle_radius, paintFill!!)
        //貝塞爾
        //控製點
        mSpringPoint[0]!!.x = left_circle_x
        mSpringPoint[0]!!.y = -left_circle_radius
        mSpringPoint[5]!!.x = mSpringPoint[0]!!.x
        mSpringPoint[5]!!.y = left_circle_radius
        //
        mSpringPoint[1]!!.x = (left_circle_x + right_circle_x) / 2
        mSpringPoint[1]!!.y = -left_circle_radius / 2
        mSpringPoint[4]!!.x = mSpringPoint[1]!!.x
        mSpringPoint[4]!!.y = left_circle_radius / 2
        //
        mSpringPoint[2]!!.x = right_circle_x
        mSpringPoint[2]!!.y = -right_circle_radius
        mSpringPoint[3]!!.x = mSpringPoint[2]!!.x
        mSpringPoint[3]!!.y = right_circle_radius
        mPath!!.reset()
        mPath!!.moveTo(mSpringPoint[0]!!.x, mSpringPoint[0]!!.y)
        mPath!!.quadTo(
            mSpringPoint[1]!!.x,
            mSpringPoint[1]!!.y,
            mSpringPoint[2]!!.x,
            mSpringPoint[2]!!.y
        )
        mPath!!.lineTo(mSpringPoint[3]!!.x, mSpringPoint[3]!!.y)
        mPath!!.quadTo(
            mSpringPoint[4]!!.x,
            mSpringPoint[4]!!.y,
            mSpringPoint[5]!!.x,
            mSpringPoint[5]!!.y
        )
        canvas.drawPath(mPath!!, paintFill!!)
    }

    /**
     * 繪製貝塞爾曲線
     *
     * @param canvas
     */
    private fun drawCubicBezier(canvas: Canvas) {
        //更換控製點
        changePoint()
        /** 清除Path中的內容
         * reset不保留內部數據結構，但會保留FillType.
         * rewind會保留內部的數據結構，但不保留FillType  */
        mPath!!.reset()

        //0
        mPath!!.moveTo(mControlPoint[0]!!.x, mControlPoint[0]!!.y)
        //0-3
        mPath!!.cubicTo(
            mControlPoint[1]!!.x,
            mControlPoint[1]!!.y,
            mControlPoint[2]!!.x,
            mControlPoint[2]!!.y,
            mControlPoint[3]!!.x,
            mControlPoint[3]!!.y
        )
        //3-6
        mPath!!.cubicTo(
            mControlPoint[4]!!.x,
            mControlPoint[4]!!.y,
            mControlPoint[5]!!.x,
            mControlPoint[5]!!.y,
            mControlPoint[6]!!.x,
            mControlPoint[6]!!.y
        )
        //6-9
        mPath!!.cubicTo(
            mControlPoint[7]!!.x,
            mControlPoint[7]!!.y,
            mControlPoint[8]!!.x,
            mControlPoint[8]!!.y,
            mControlPoint[9]!!.x,
            mControlPoint[9]!!.y
        )
        //9-0
        mPath!!.cubicTo(
            mControlPoint[10]!!.x,
            mControlPoint[10]!!.y,
            mControlPoint[11]!!.x,
            mControlPoint[11]!!.y,
            mControlPoint[0]!!.x,
            mControlPoint[0]!!.y
        )
        canvas.drawPath(mPath!!, paintFill!!)
    }

    /**
     * 控製點
     */
    private fun changePoint() {
        mCenterPoint.y = 0f
        var mc = M
        mControlPoint[2]!!.y = mRadius //底部
        mControlPoint[8]!!.y = -mRadius //頂部

        //圓心位置
        if (mPosition == mNum - 1 && !mIsLeft) { //第一個 右滑  0-->4
            if (mPercent <= 0.2) { //回彈 圓心到達
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + (mNum - 1) * mDistance //最後一個
            } else if (mPercent <= 0.8) { //加速 左凸起 扁平化M 最右端固定不變  圓心移動
                mCenterPoint.x =
                    -(mNum - 1) * 0.5f * mDistance + (1 - (mPercent - 0.2f) / 0.6f) * (mNum - 1) * mDistance
            } else if (mPercent > 0.8 && mPercent < 1) { //
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance //第一個
            } else if (mPercent == 1f) { //圓
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance
            }
            //控製點位置
            if (mPercent > 0.8 && mPercent <= 1) { //右凸起 圓心不變
                mControlPoint[5]!!.x =
                    mCenterPoint.x + mRadius * (2 - (mPercent - 0.8f) / 0.2f) //右半圓
                mControlPoint[0]!!.x = mCenterPoint.x - mRadius //左半圓
            } else if (mPercent > 0.5 && mPercent <= 0.8) { //加速 左凸起 扁平化M 最右端固定不變  圓心移動
                mControlPoint[5]!!.x = mCenterPoint.x + 2 * mRadius //右半圓
                mControlPoint[0]!!.x =
                    mCenterPoint.x - mRadius * (1 + (0.8f - mPercent) / 0.3f) //左半圓
                mControlPoint[2]!!.y = mRadius * (1 + (mPercent - 0.8f) / 0.3f * 0.1f) //底部
                mControlPoint[8]!!.y = -mRadius * (1 + (mPercent - 0.8f) / 0.3f * 0.1f) //頂部
                mc = mc * (1 + (-mPercent + 0.8f) / 0.3f * 0.3f)
            } else if (mPercent > 0.2 && mPercent <= 0.5) { //左右恢復 變圓M逐漸重置為原來大小  圓心移動
                mControlPoint[5]!!.x =
                    mCenterPoint.x + mRadius * (1 + (mPercent - 0.2f) / 0.3f) //右半圓
                mControlPoint[0]!!.x =
                    mCenterPoint.x - mRadius * (1 + (mPercent - 0.2f) / 0.3f) //左半圓
                mControlPoint[2]!!.y = mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //底部
                mControlPoint[8]!!.y = -mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //頂部
                mc = mc * (1 + (mPercent - 0.2f) / 0.3f * 0.3f)
            } else if (mPercent > 0.1 && mPercent <= 0.2) { //左凹 圓心到達.0
                mControlPoint[5]!!.x = mCenterPoint.x + mRadius //右半圓
                mControlPoint[0]!!.x =
                    mCenterPoint.x - mRadius * (1 - (0.2f - mPercent) / 0.1f * 0.5f) //左半圓
            } else if (mPercent >= 0 && mPercent <= 0.1) { //回彈 圓心到達
                mControlPoint[5]!!.x = mCenterPoint.x + mRadius //右半圓
                mControlPoint[0]!!.x = mCenterPoint.x - mRadius * (1 - mPercent / 0.1f * 0.5f) //左半圓
            }
        } else if (mPosition == mNum - 1 && mIsLeft) { //最後一個 左滑  4-->0
            if (mPercent <= 0.2) { //圓
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + (mNum - 1) * mDistance
            } else if (mPercent <= 0.8) { //加速 左凸起 扁平化M 最右端固定不變  圓心移動
                mCenterPoint.x =
                    -(mNum - 1) * 0.5f * mDistance + (1 - (mPercent - 0.2f) / 0.6f) * (mNum - 1) * mDistance
            } else if (mPercent > 0.8 && mPercent < 1) { //回彈 圓心到達
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance //第一個
            } else if (mPercent == 1f) { //圓
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + mPosition * mDistance
            }
            if (mPercent <= 0) { //圓
            } else if (mPercent <= 0.2 && mPercent >= 0) { //左凸起 圓心不變
                mControlPoint[5]!!.x = mCenterPoint.x + mRadius //右半圓
                mControlPoint[0]!!.x = mCenterPoint.x - mRadius * (1 + mPercent / 0.2f) //左半圓
            } else if (mPercent > 0.2 && mPercent <= 0.5) { //加速 右凸起 扁平化M 最左端固定不變  圓心移動
                mControlPoint[5]!!.x =
                    mCenterPoint.x + mRadius * (1 + (mPercent - 0.2f) / 0.3f) //右半圓
                mControlPoint[0]!!.x = mCenterPoint.x - 2 * mRadius //左半圓
                mControlPoint[2]!!.y = mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //底部
                mControlPoint[8]!!.y = -mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //頂部
                mc = mc * (1 + (mPercent - 0.2f) / 0.3f * 0.3f)
            } else if (mPercent > 0.5 && mPercent <= 0.8) { //左右恢復 變圓M逐漸重置為原來大小  圓心移動
                mControlPoint[5]!!.x =
                    mCenterPoint.x + mRadius * (1 + (0.8f - mPercent) / 0.3f) //右半圓
                mControlPoint[0]!!.x =
                    mCenterPoint.x - mRadius * (1 + (0.8f - mPercent) / 0.3f) //左半圓
                mControlPoint[2]!!.y = mRadius * (1 + (mPercent - 0.8f) / 0.3f * 0.1f) //底部
                mControlPoint[8]!!.y = -mRadius * (1 + (mPercent - 0.8f) / 0.3f * 0.1f) //頂部
                mc = mc * (1 + (0.8f - mPercent) / 0.3f * 0.3f)
            } else if (mPercent > 0.8 && mPercent <= 0.9) { //右凹 圓心到達
                mControlPoint[5]!!.x =
                    mCenterPoint.x + mRadius * (1 - (mPercent - 0.8f) / 0.1f * 0.5f) //右半圓
                mControlPoint[0]!!.x = mCenterPoint.x - mRadius //左半圓
            } else if (mPercent > 0.9 && mPercent <= 1) { //回彈 圓心到達
                mControlPoint[5]!!.x =
                    mCenterPoint.x + mRadius * (1 - (mPercent - 0.9f) / 0.1f * 0.5f) //右半圓
                mControlPoint[0]!!.x = mCenterPoint.x - mRadius //左半圓
            }
        } else {
            if (mPercent <= 0.2) { //圓
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + mPosition * mDistance
            } else if (mPercent <= 0.8) { //加速 左凸起 扁平化M 最右端固定不變  圓心移動
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + (mPosition + mPercent) * mDistance
                mCenterPoint.x =
                    -(mNum - 1) * 0.5f * mDistance + (mPosition + (mPercent - 0.2f) / 0.6f) * mDistance
            } else if (mPercent > 0.8 && mPercent < 1) { //回彈 圓心到達
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + (mPosition + 1) * mDistance
            } else if (mPercent == 1f) { //圓
                mCenterPoint.x = -(mNum - 1) * 0.5f * mDistance + mPosition * mDistance
            }
            //控製點位置
            if (mIsLeft) //左滑
            {
                if (mPercent >= 0 && mPercent <= 0.2) { //右凸起 圓心不變
                    mControlPoint[5]!!.x =
                        mCenterPoint.x + mRadius * (2 - (0.2f - mPercent) / 0.2f) //右半圓
                    mControlPoint[0]!!.x = mCenterPoint.x - mRadius //左半圓
                } else if (mPercent > 0.2 && mPercent <= 0.5) { //加速 左凸起 扁平化M 最右端固定不變  圓心移動
                    mControlPoint[5]!!.x = mCenterPoint.x + 2 * mRadius //右半圓
                    mControlPoint[0]!!.x =
                        mCenterPoint.x - mRadius * (1 + (mPercent - 0.2f) / 0.3f) //左半圓
                    mControlPoint[2]!!.y = mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //底部
                    mControlPoint[8]!!.y = -mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //頂部
                    mc = mc * (1 + (mPercent - 0.2f) / 0.3f * 0.3f)
                } else if (mPercent > 0.5 && mPercent <= 0.8) { //左右恢復 變圓M逐漸重置為原來大小  圓心移動
                    mControlPoint[5]!!.x =
                        mCenterPoint.x + mRadius * (1 + (0.8f - mPercent) / 0.3f) //右半圓
                    mControlPoint[0]!!.x =
                        mCenterPoint.x - mRadius * (1 + (0.8f - mPercent) / 0.3f) //左半圓
                    mControlPoint[2]!!.y = mRadius * (1 + (mPercent - 0.8f) / 0.3f * 0.1f) //底部
                    mControlPoint[8]!!.y = -mRadius * (1 + (mPercent - 0.8f) / 0.3f * 0.1f) //頂部
                    mc = mc * (1 + (-mPercent + 0.8f) / 0.3f * 0.3f)
                } else if (mPercent > 0.8 && mPercent <= 0.9) { //左凹 圓心到達
                    mControlPoint[5]!!.x = mCenterPoint.x + mRadius //右半圓
                    mControlPoint[0]!!.x =
                        mCenterPoint.x - mRadius * (1 - (mPercent - 0.8f) / 0.1f * 0.5f) //左半圓
                } else if (mPercent > 0.9 && mPercent <= 1) { //回彈 圓心到達
                    mControlPoint[5]!!.x = mCenterPoint.x + mRadius //右半圓
                    mControlPoint[0]!!.x =
                        mCenterPoint.x - mRadius * (1 - (1.0f - mPercent) / 0.1f * 0.5f) //左半圓
                }
            } else  //右滑
            {
                if (mPercent <= 1 && mPercent >= 0.8) { //左凸起 圓心不變
                    mControlPoint[5]!!.x = mCenterPoint.x + mRadius //右半圓
                    mControlPoint[0]!!.x =
                        mCenterPoint.x - mRadius * (2 - (mPercent - 0.8f) / 0.2f) //左半圓
                } else if (mPercent > 0.5 && mPercent <= 0.8) { //加速 右凸起 扁平化M 最左端固定不變  圓心移動
                    mControlPoint[5]!!.x =
                        mCenterPoint.x + mRadius * (2 - (mPercent - 0.5f) / 0.3f) //右半圓
                    mControlPoint[0]!!.x = mCenterPoint.x - 2 * mRadius //左半圓
                    mControlPoint[2]!!.y = mRadius * (1 - (0.8f - mPercent) / 0.3f * 0.1f) //底部
                    mControlPoint[8]!!.y = -mRadius * (1 - (0.8f - mPercent) / 0.3f * 0.1f) //頂部
                    mc = mc * (1 + (0.8f - mPercent) / 0.3f * 0.3f)
                } else if (mPercent > 0.2 && mPercent <= 0.5) { //左右恢復 變圓M逐漸重置為原來大小  圓心移動
                    mControlPoint[5]!!.x =
                        mCenterPoint.x + mRadius * (1 + (mPercent - 0.2f) / 0.3f) //右半圓
                    mControlPoint[0]!!.x =
                        mCenterPoint.x - mRadius * (1 + (mPercent - 0.2f) / 0.3f) //左半圓
                    mControlPoint[2]!!.y = mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //底部
                    mControlPoint[8]!!.y = -mRadius * (1 - (mPercent - 0.2f) / 0.3f * 0.1f) //頂部
                    mc = mc * (1 + (mPercent - 0.2f) / 0.3f * 0.3f)
                } else if (mPercent > 0.1 && mPercent <= 0.2) { //右凹 圓心到達
                    mControlPoint[5]!!.x =
                        mCenterPoint.x + mRadius * (1 - (0.2f - mPercent) / 0.1f * 0.5f) //右半圓
                    mControlPoint[0]!!.x = mCenterPoint.x - mRadius //左半圓
                } else if (mPercent >= 0 && mPercent <= 0.1) { //回彈 圓心到達
                    mControlPoint[5]!!.x =
                        mCenterPoint.x + mRadius * (1 - mPercent / 0.1f * 0.5f) //右半圓
                    mControlPoint[0]!!.x = mCenterPoint.x - mRadius //左半圓
                }
            }
        }

        //11 0 1
        mControlPoint[0]!!.y = 0f
        mControlPoint[1]!!.x = mControlPoint[0]!!.x
        mControlPoint[1]!!.y = mRadius * mc
        mControlPoint[11]!!.x = mControlPoint[0]!!.x
        mControlPoint[11]!!.y = -mRadius * mc
        //2 3 4
        mControlPoint[2]!!.x = mCenterPoint.x - mRadius * mc
        mControlPoint[3]!!.x = mCenterPoint.x
        mControlPoint[3]!!.y = mControlPoint[2]!!.y
        mControlPoint[4]!!.x = mCenterPoint.x + mRadius * mc
        mControlPoint[4]!!.y = mControlPoint[2]!!.y
        //5 6 7
        mControlPoint[5]!!.y = mRadius * mc
        mControlPoint[6]!!.x = mControlPoint[5]!!.x
        mControlPoint[6]!!.y = 0f
        mControlPoint[7]!!.x = mControlPoint[5]!!.x
        mControlPoint[7]!!.y = -mRadius * mc
        //8 9 10
        mControlPoint[8]!!.x = mCenterPoint.x + mRadius * mc
        mControlPoint[9]!!.x = mCenterPoint.x
        mControlPoint[9]!!.y = mControlPoint[8]!!.y
        mControlPoint[10]!!.x = mCenterPoint.x - mRadius * mc
        mControlPoint[10]!!.y = mControlPoint[8]!!.y
    }

    private var mControlPoint =
        arrayOfNulls<Point>(9)
    private val mCenterPoint =
        CenterPoint()

    internal inner class CenterPoint {
        var x = 0f
        var y = 0f
    }

    internal inner class Point {
        var x = 0f
        var y = 0f
    }

    /**
     * 移動指示點
     *
     * @param percent  比例
     * @param position 第幾個
     * @param isLeft   是否左滑
     */
    fun move(percent: Float, position: Int, isLeft: Boolean) {
        mPosition = position
        mPercent = percent
        mIsLeft = isLeft
        when (mIndicatorType) {
            IndicatorType.CIRCLE_LINE -> {
                if (mPosition == mNum - 1 && !isLeft) { //第一個 右滑
                    mOffset = percent * mDistance
                }
                mOffset = if (mPosition == mNum - 1 && isLeft) { //最後一個 左滑
                    percent * mDistance
                } else { //中間
                    percent * mDistance
                }
            }
            IndicatorType.CIRCLE, IndicatorType.LINE, IndicatorType.PROGRESS -> mOffset =
                if (mPosition == mNum - 1 && !isLeft) { //第一個 右滑
                    (1 - percent) * (mNum - 1) * mDistance
                } else if (mPosition == mNum - 1 && isLeft) { //最後一個 左滑
                    (1 - percent) * (mNum - 1) * mDistance
                } else { //中間的
                    (percent + mPosition) * mDistance
                }
            IndicatorType.BEZIER -> {
            }
            IndicatorType.SPRING -> {
            }
        }
        invalidate()
    }

    /**
     * 個數
     *
     * @param num
     */
    fun setNum(num: Int): ViewPagerIndicator {
        mNum = num
        invalidate()
        return this
    }

    /**
     * 類型
     *
     * @param indicatorType
     */
    fun setType(indicatorType: Int): ViewPagerIndicator {
        mIndicatorType = indicatorType
        invalidate()
        return this
    }

    /**
     * 線,圓,圓線,貝塞爾,彈性球,進度條
     */
    interface IndicatorType {
        companion object {
            const val LINE = 0
            const val CIRCLE = 1
            const val CIRCLE_LINE = 2
            const val BEZIER = 3
            const val SPRING = 4
            const val PROGRESS = 5
        }
    }

    /**
     * 半徑
     *
     * @param radius
     */
    fun setRadius(radius: Float): ViewPagerIndicator {
        mRadius = radius
        invalidate()
        return this
    }

    /**
     * 距離 在IndicatorDistanceType為BYDISTANCE下作用
     *
     * @param distance
     */
    fun setDistance(distance: Float): ViewPagerIndicator {
        mDistance = distance
        invalidate()
        return this
    }

    /**
     * 距離類型
     *
     * @param mDistanceType
     */
    fun setDistanceType(mDistanceType: Int): ViewPagerIndicator {
        this.mDistanceType = mDistanceType
        invalidate()
        return this
    }

    /**
     * 佈局,距離,半徑
     */
    interface DistanceType {
        companion object {
            //
            const val BY_RADIUS = 0
            const val BY_DISTANCE = 1
            const val BY_LAYOUT = 2
        }
    }

    /**
     * 一般 不循環 固定
     *
     * @param viewPager 適配的viewpager
     * @return
     */
    fun setViewPager2(viewPager: ViewPager2): ViewPagerIndicator {
        setViewPager(viewPager, viewPager.adapter!!.itemCount, false)
        return this
    }

    /**
     * @param viewpager   適配的viewpager
     * @param CycleNumber 偽無限循環 真實個數
     * @return
     */
    fun setViewPager2(viewpager: ViewPager2, CycleNumber: Int): ViewPagerIndicator {
        setViewPager(viewpager, CycleNumber, false)
        return this
    }

    /**
     * @param viewpager        適配的viewpager
     * @param CycleNumber      真/偽無限循環都必須輸入
     * @param isInfiniteCircle 真無限循環 配合Banner
     * @return
     */
    private fun setViewPager(
        viewpager: ViewPager2,
        CycleNumber: Int,
        isInfiniteCircle: Boolean
    ): ViewPagerIndicator {
        mNum = CycleNumber
        mIsInfiniteCircle = isInfiniteCircle
        viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            //記錄上一次滑動的positionOffsetPixels值
            private var lastValue = -1
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                var position = position
                if (!mAnimation) {
                    //不需要動畫
                    return
                }
                var isLeft = mIsLeft
                if (lastValue / 10 > positionOffsetPixels / 10) {
                    //右滑
                    isLeft = false
                } else if (lastValue / 10 < positionOffsetPixels / 10) {
                    //左滑
                    isLeft = true
                }
                if (mNum > 0 && !mIsInfiniteCircle) {
                    move(positionOffset, position % mNum, isLeft)
                } else if (mNum > 0 && mIsInfiniteCircle) {
                    if (position == 0) {
                        position = mNum - 1
                    } else if (position == mNum + 1) {
                        position = 0
                    } else {
                        position--
                    }
                    move(positionOffset, position, isLeft)
                }
                lastValue = positionOffsetPixels
            }

            override fun onPageSelected(position: Int) {
                var position = position
                if (mAnimation) {
                    //需要動畫
                    return
                }
                if (mNum > 0 && !mIsInfiniteCircle) {
                    move(0f, position % mNum, false)
                } else if (mNum > 0 && mIsInfiniteCircle) {
                    if (position == 0) {
                        position = mNum - 1
                    } else if (position == mNum + 1) {
                        position = 0
                    } else {
                        position--
                    }
                    move(0f, position, false)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        return this
    }
}