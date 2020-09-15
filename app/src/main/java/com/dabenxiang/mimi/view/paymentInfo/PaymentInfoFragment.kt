package com.dabenxiang.mimi.view.paymentInfo

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_payment_info.*
import kotlinx.android.synthetic.main.item_order_result_successful.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PaymentInfoFragment: BaseFragment() {
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
        requireActivity().onBackPressedDispatcher.addCallback {
            navigateTo(NavigateItem.Up)
        }
        initSettings()
    }

    override fun initSettings() {
        tv_close.visibility = View.GONE
        tv_submit.visibility = View.GONE

        arguments?.getSerializable(KEY_ORDER_ITEM)?.also { orderItem ->
            orderItem as OrderItem
            val paymentInfoItem = orderItem.paymentInfos[0]

            val calendar = Calendar.getInstance()
            calendar.time = orderItem.createTime ?: Date()
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm", Locale.getDefault())
            val time = sdf.format(calendar.time)

            val timeout = StringBuilder("请于 ")
                .append(time)
                .append(" 前完成打款动作，避免订单超时")
                .toString()

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

            setupTimeout(timeout)
            tv_name.text = paymentInfoItem.accountName
            tv_bank.text = bank
            tv_city.text = city
            tv_account.text = paymentInfoItem.accountNumber
            tv_amount.text = GeneralUtils.getAmountFormat(orderItem.sellingPrice)
        }

        ib_close.setOnClickListener {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_payment_info
    }

    override fun setupObservers() {}

    override fun setupListeners() {}

    private fun setupTimeout(text: String) {
        val builder = SpannableStringBuilder(text)
        builder.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ), builder.indexOf("于") + 1,
            builder.lastIndexOf("前") - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv_timeout.text = builder
    }
}