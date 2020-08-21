package com.dabenxiang.mimi.view.orderresult

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.vo.mqtt.OrderItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.orderresult.itemview.OrderResultFailedItemView
import com.dabenxiang.mimi.view.orderresult.itemview.OrderResultSuccessItemView
import kotlinx.android.synthetic.main.fragment_order_result.*

class OrderResultFragment : BaseFragment() {

    companion object {
        private const val KEY_ERROR = "error"

        fun createBundle(isError: Boolean): Bundle {
            return Bundle().also { it.putBoolean(KEY_ERROR, isError) }
        }
    }

    private val viewModel: OrderResultViewModel by viewModels()

    private val epoxyController by lazy {
        OrderResultEpoxyController(failedListener, successListener)
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback { }

        tv_step1.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_blue_1_oval
        )

        tv_step2.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_black_1_oval
        )

        line.setBackgroundColor(requireContext().getColor(R.color.color_black_1_20))
        tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1))
        tv_create_order_complete.setTextColor(requireContext().getColor(R.color.color_black_1_30))

        tv_step1.text = "1"
        tv_step2.text = "2"

        recycler_order_result.layoutManager = LinearLayoutManager(requireContext())
        recycler_order_result.adapter = epoxyController.adapter

        if (arguments?.getBoolean(KEY_ERROR) == true) {
            setupStepUi(false)
            epoxyController.setData(OrderItem(isSuccessful = false))
        } else {
            epoxyController.setData(null)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_result
    }

    override fun setupObservers() {
        mainViewModel?.orderItem?.observe(viewLifecycleOwner, Observer {
            setupStepUi(it.isSuccessful)
            epoxyController.setData(it)
        })
    }

    override fun setupListeners() {

    }

    private val failedListener = object : OrderResultFailedItemView.OrderResultFailedListener {
        override fun onConfirm() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_topupFragment))
        }
    }

    private val successListener = object : OrderResultSuccessItemView.OrderResultSuccessListener {
        override fun onConfirm() {
            //TODO: navigate to 充值管理
        }

        override fun onClose() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_topupFragment))
        }
    }

    private fun setupStepUi(isSuccessful: Boolean) {
        if (isSuccessful) {
            tv_step1.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_blue_1_oval
            )

            tv_step2.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_blue_1_oval
            )

            line.setBackgroundColor(requireContext().getColor(R.color.color_blue_2))
            tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1_30))
            tv_create_order_complete.setTextColor(requireContext().getColor(R.color.color_black_1))

            val img = ContextCompat.getDrawable(requireContext(), R.drawable.ico_cheched)
            tv_step1.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null)
            tv_step1.text = ""

        } else {
            tv_step1.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_black_1_oval
            )

            tv_step2.background = ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_black_1_oval
            )

            line.setBackgroundColor(requireContext().getColor(R.color.color_black_1_20))
            tv_create_order.setTextColor(requireContext().getColor(R.color.color_black_1_30))
            tv_create_order_complete.setTextColor(requireContext().getColor(R.color.color_black_1_30))
        }
    }
}