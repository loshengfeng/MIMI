package com.dabenxiang.mimi.view.orderresult.itemview

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.item_order_result_waiting.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class OrderResultWaitingItemView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.item_order_result_waiting, this)
    }

    @SuppressLint("SetTextI18n")
    @ModelProp
    fun setupCountDown(time: Long) {
        tv_count_down_hint.text = context.getString(R.string.order_result_create_order_waiting) + "(" + time.toString() + ")"
    }
}