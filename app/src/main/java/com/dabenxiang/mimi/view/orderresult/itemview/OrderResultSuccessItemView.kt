package com.dabenxiang.mimi.view.orderresult.itemview

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.item_order_result_successful.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class OrderResultSuccessItemView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.item_order_result_successful, this)
    }

    @ModelProp
    fun setupTimeout(text: String) {
        tv_timeout.text = text
    }

    @ModelProp
    fun setupName(text: String) {
        tv_name.text = text
    }

    @ModelProp
    fun setupBank(text: String) {
        tv_back.text = text
    }

    @ModelProp
    fun setupCity(text: String) {
        tv_city.text = text
    }

    @ModelProp
    fun setupAccount(text: String) {
        tv_account.text = text
    }

    @ModelProp
    fun setupAmount(text: String) {
        tv_amount.text = text
    }

    @CallbackProp
    fun setupClickListener(listener: OrderResultSuccessListener?) {
        tv_submit.setOnClickListener {
            listener?.onConfirm()
        }
        tv_close.setOnClickListener {
            listener?.onClose()
        }
    }

    interface OrderResultSuccessListener {
        fun onConfirm()
        fun onClose()
    }

}