package com.dabenxiang.mimi.view.orderresult.itemview

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelView
import com.dabenxiang.mimi.R

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class OrderResultWaitingItemView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.item_order_result_waiting, this)
    }

}