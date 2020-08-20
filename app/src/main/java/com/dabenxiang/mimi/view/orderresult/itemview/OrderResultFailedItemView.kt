package com.dabenxiang.mimi.view.orderresult.itemview

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.item_order_result_failed.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class OrderResultFailedItemView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.item_order_result_failed, this)
    }

    @CallbackProp
    fun setupClickListener(listener: OrderResultFailedListener?) {
        btn_confirm.setOnClickListener {
            listener?.onConfirm()
        }
    }

    interface OrderResultFailedListener {
        fun onConfirm()
    }
}