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
import kotlinx.android.synthetic.main.item_order_result_url_successful.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class OrderResultUrlSuccessItemView(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.item_order_result_url_successful, this)
    }

    var orderResultSuccessListener: OrderResultSuccessListener? = null

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
    fun setupAmount(text: String) {
        tv_amount.text = text
    }

    @ModelProp
    fun setupPaymentImg(resId: Int) {
        iv_payment.setImageResource(resId)
    }

    @ModelProp
    fun setupPaymentCountdown(second: Int) {
        tv_payment_countdown.text = String.format(
            context.getString(R.string.order_result_payment_guide),
            second
        )
    }

    @ModelProp
    fun setupPaymentCountdownColor(color: Int) {
        tv_payment_countdown.setTextColor(color)
    }

    @ModelProp
    fun setupPaymentCountdownBackground(background: Int) {
        tv_payment_countdown.setBackgroundResource(background)
    }

    @ModelProp
    fun setupPaymentCountdownVisibility(isVisible: Boolean) {
        tv_payment_countdown.visibility = when {
            isVisible -> View.VISIBLE
            else -> View.GONE
        }
    }

    @ModelProp
    fun setupPaymentGoBackground(background: Int) {
        tv_payment_go.setBackgroundResource(background)
    }

    @ModelProp
    fun setupPaymentPageListener(url: String) {
        tv_payment_go.setOnClickListener { orderResultSuccessListener?.onOpenPaymentWebView(url) }
    }

    @CallbackProp
    fun setupClickListener(listener: OrderResultSuccessListener?) {
        orderResultSuccessListener = listener
        tv_submit.setOnClickListener { listener?.onConfirm() }
        tv_close.setOnClickListener { listener?.onClose() }
    }
}