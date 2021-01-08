package com.dabenxiang.mimi.view.paymentInfo

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_payment_info.*
import kotlinx.android.synthetic.main.item_order_result_detail_successful.*
import kotlinx.android.synthetic.main.item_order_result_url_successful.*
import java.text.SimpleDateFormat
import java.util.*

class PaymentInfoFragment : BaseFragment() {
    companion object {
        private const val KEY_ORDER_ITEM = "order_item"
        fun createBundle(
            item: OrderItem
        ): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_ORDER_ITEM, item)
            }
        }
    }

    override val bottomNavigationVisibility: Int = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun initSettings() {
        arguments?.getSerializable(KEY_ORDER_ITEM)?.also { orderItem ->
            orderItem as OrderItem

            if (TextUtils.isEmpty(orderItem.paymentInfos[0].paymentUrl)) {
                layout_order_detail.visibility = View.VISIBLE
                layout_order_url_info.visibility = View.GONE
                setupPaymentDetailUi(orderItem)
            } else {
                layout_order_detail.visibility = View.GONE
                layout_order_url_info.visibility = View.VISIBLE
                setupPaymentUrlUi(orderItem)
            }
        }

        ib_close.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_payment_info
    }

    override fun setupObservers() {}

    override fun setupListeners() {
    }

    private fun setupPaymentDetailUi(orderItem: OrderItem) {
        val paymentInfoItem = orderItem.paymentInfos[0]

        tv_timeout.text = getTimeout(orderItem)

        val bank = StringBuilder(paymentInfoItem.bankName)
            .append("(")
            .append(paymentInfoItem.bankCode)
            .append(")")
            .toString()

        val city = StringBuilder(paymentInfoItem.bankBranchProvince)
            .append("/")
            .append(paymentInfoItem.bankBranchCity)
            .append("/")
            .append(paymentInfoItem.bankBranchName)
            .toString()

        tv_name.text = paymentInfoItem.accountName
        tv_bank.text = bank
        tv_city.text = city
        tv_account.text = paymentInfoItem.accountNumber
        tv_amount.text = GeneralUtils.getAmountFormat(orderItem.sellingPrice)

        tv_close.visibility = View.GONE
        tv_submit.visibility = View.GONE
    }

    private fun setupPaymentUrlUi(orderItem: OrderItem) {
        val paymentInfoItem = orderItem.paymentInfos[0]

        url_tv_timeout.text = getTimeout(orderItem)

        tv_payment_countdown.visibility = View.GONE

        url_tv_amount.text = GeneralUtils.getAmountFormat(orderItem.sellingPrice)

        when (paymentInfoItem.paymentType) {
            PaymentType.BANK -> {
                iv_payment.setImageResource(R.drawable.ico_bank_160_px)
                tv_payment_go.setBackgroundResource(R.drawable.bg_black_2_radius_6)
            }
            PaymentType.ALI -> {
                iv_payment.setImageResource(R.drawable.ico_alipay_160_px)
                tv_payment_go.setBackgroundResource(R.drawable.bg_blue_2_radius_6)
            }
            PaymentType.WX -> {
                iv_payment.setImageResource(R.drawable.ico_wechat_pay_160_px)
                tv_payment_go.setBackgroundResource(R.drawable.bg_green_2_radius_6)
            }
            PaymentType.TIK_TOK -> {
                iv_payment.setImageResource(R.drawable.ico_tiktokpay_160_px)
                tv_payment_go.setBackgroundResource(R.drawable.bg_black_2_radius_6)
            }
        }

        tv_payment_go.setOnClickListener {
            GeneralUtils.openWebView(requireContext(), paymentInfoItem.paymentUrl)
        }

        url_tv_close.visibility = View.GONE
        url_tv_submit.visibility = View.GONE
    }

    private fun getTimeout(orderItem: OrderItem): SpannableStringBuilder {
        val calendar = Calendar.getInstance()
        calendar.time = orderItem.createTime ?: Date()
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm", Locale.getDefault())
        val time = sdf.format(calendar.time)

        val timeout = StringBuilder("请于 ")
            .append(time)
            .append(" 前完成打款动作，避免订单超时")
            .toString()

        val builder = SpannableStringBuilder(timeout)
        builder.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_red_1)),
            builder.indexOf("于") + 1,
            builder.lastIndexOf("前") - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return builder
    }
}