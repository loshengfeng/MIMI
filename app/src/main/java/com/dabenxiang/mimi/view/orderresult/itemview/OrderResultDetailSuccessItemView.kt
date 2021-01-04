package com.dabenxiang.mimi.view.orderresult.itemview

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.orderresult.OrderResultSuccessListener
import kotlinx.android.synthetic.main.item_order_result_detail_successful.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class OrderResultDetailSuccessItemView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.item_order_result_detail_successful, this)
    }

    @ModelProp
    fun setupTimeout(text: String) {
        val builder = SpannableStringBuilder(text)
        builder.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    context,
                    R.color.color_red_1
                )
            ), builder.indexOf("于") + 1,
            builder.lastIndexOf("前") - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv_timeout.text = builder
    }

    @ModelProp
    fun setupName(text: String) {
        tv_name.text = text
    }

    @ModelProp
    fun setupBank(text: String) {
        tv_bank.text = text
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
        tv_submit.setOnClickListener { listener?.onConfirm() }
        tv_close.setOnClickListener { listener?.onClose() }
    }
}