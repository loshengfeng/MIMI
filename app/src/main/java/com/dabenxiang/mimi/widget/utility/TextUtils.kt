package com.dabenxiang.mimi.widget.utility

import android.graphics.Paint
import android.widget.TextView

object TextUtils {
    fun autoSplitText(tv: TextView): String? {
        val rawText = tv.text.toString() //原始文本
        val tvPaint: Paint = tv.paint //paint，包含字體等信息
        val tvWidth = tv.width - tv.paddingLeft - tv.paddingRight.toFloat() //控件可用寬度

        //將原始文本按行拆分
        val rawTextLines: Array<String?> = rawText.replace("\r".toRegex(), "").split("\n".toRegex()).toTypedArray()
        val sbNewText = StringBuilder()
        for (rawTextLine in rawTextLines) {
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                //如果整行寬度在控件可用寬度之內，就不處理了
                sbNewText.append(rawTextLine)
            } else {
                //如果整行寬度超過控件可用寬度，則按字符測量，在超過可用寬度的前一個字符處手動換行
                var lineWidth = 0f
                var cnt = 0
                while (cnt != rawTextLine!!.length) {
                    val ch = rawTextLine[cnt]
                    lineWidth += tvPaint.measureText(ch.toString())
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch)
                    } else {
                        sbNewText.append("\n")
                        lineWidth = 0f
                        --cnt
                    }
                    ++cnt
                }
            }
            sbNewText.append("\n")
        }

        //把結尾多余的\n去掉
        if (!rawText.endsWith("\n")) {
            sbNewText.deleteCharAt(sbNewText.length - 1)
        }
        return sbNewText.toString()
    }
}