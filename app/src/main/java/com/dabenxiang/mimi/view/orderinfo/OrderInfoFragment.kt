package com.dabenxiang.mimi.view.orderinfo

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Empty
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.vo.OrderingPackageItem
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.orderresult.OrderResultFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_order_info.*
import kotlinx.android.synthetic.main.fragment_text_detail.toolbarContainer
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class OrderInfoFragment : BaseFragment() {

    companion object {
        const val KEY_DATA = "data"
        fun createBundle(item: OrderingPackageItem?): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_DATA, item)
            }
        }
    }

    private val viewModel: OrderInfoViewModel by viewModels()

    private lateinit var orderingPackageItem: OrderingPackageItem

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_toolbar_title.text = getString(R.string.order_detail_title)

        toolbar.setBackgroundColor(requireContext().getColor(R.color.color_gray_2))
        text_toolbar_title.setTextColor(requireContext().getColor(R.color.color_black_1))
        toolbarContainer.toolbar.navigationIcon = ContextCompat.getDrawable(
            requireContext(), R.drawable.btn_back_black_n
        )

        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        orderingPackageItem = arguments?.getSerializable(KEY_DATA) as OrderingPackageItem

        if (orderingPackageItem.paymentType == null) {
            navigateTo(NavigateItem.Up)
        } else {
            val productName = getString(
                R.string.order_detail_content, orderingPackageItem.name,
                getString(
                    when (orderingPackageItem.paymentType) {
                        PaymentType.BANK -> R.string.order_detail_payment_bank
                        PaymentType.ALI -> R.string.order_detail_payment_ali
                        PaymentType.TIK_TOK -> R.string.order_detail_payment_tiktok
                        PaymentType.WX -> R.string.order_detail_payment_wx
                    }
                )
            )

            tv_product_name.text = productName

            val price = GeneralUtils.getAmountFormat(orderingPackageItem.price)

            tv_product_count.text = StringBuilder("¥ ")
                .append(price)
                .append(" x 1")
                .toString()

            tv_total.text = StringBuilder("¥ ")
                .append(price)
                .toString()

            tv_total_amount.text = StringBuilder("¥ ")
                .append(price)
                .toString()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_info
    }

    override fun setupObservers() {
        viewModel.createOrderResult.observe(viewLifecycleOwner, {
            when (it) {
                is Empty -> {
                    navigateTo(NavigateItem.Destination(R.id.action_orderInfoFragment_to_orderResultFragment))
                }
                is Error -> {
                    navigateTo(
                        NavigateItem.Destination(
                            R.id.action_orderInfoFragment_to_orderResultFragment,
                            OrderResultFragment.createBundle(true)
                        )
                    )
                }
            }
        })
    }

    override fun setupListeners() {
        btn_create_order.setOnClickListener {
            viewModel.createOrder(orderingPackageItem.paymentType, orderingPackageItem.id)
        }
    }

}