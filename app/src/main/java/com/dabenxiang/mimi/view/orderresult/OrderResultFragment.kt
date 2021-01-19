package com.dabenxiang.mimi.view.orderresult

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.model.vo.mqtt.OrderItem
import com.dabenxiang.mimi.model.vo.mqtt.OrderPayloadItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils.openWebView
import kotlinx.android.synthetic.main.fragment_order_result.*
import java.util.*
import kotlin.concurrent.timerTask

class OrderResultFragment : BaseFragment() {

    companion object {
        private const val KEY_ERROR = "error"
        const val DELAY_TOP_UP_TIME = 20L

        fun createBundle(isError: Boolean): Bundle {
            return Bundle().also { it.putBoolean(KEY_ERROR, isError) }
        }
    }

    private val viewModel: OrderResultViewModel by viewModels()

    private var topUpTimer: Timer? = null

    lateinit var epoxyController: OrderResultEpoxyController

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        epoxyController = OrderResultEpoxyController(
            requireContext(), failedListener, successListener
        )

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
            epoxyController.setData(OrderPayloadItem())
        } else {
            startTopUpTimer()
            epoxyController.setData(null)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order_result
    }

    private fun navigateToPaymentUrl(item: OrderItem) {
        val orderPayloadItem = item.orderPayloadItem
        if (orderPayloadItem?.isSuccessful == true) {
            when (orderPayloadItem.paymentType) {
                PaymentType.BANK.value -> {
                    if (TextUtils.isEmpty(orderPayloadItem.paymentUrl)) {
                        openWebView(requireContext(), orderPayloadItem.paymentUrl)
                    }
                }
                PaymentType.ALI.value -> {
                    openWebView(requireContext(), orderPayloadItem.paymentUrl)
                }
                PaymentType.WX.value -> {
                    openWebView(requireContext(), orderPayloadItem.paymentUrl)
                }
                PaymentType.TIK_TOK.value -> {
                    openWebView(requireContext(), orderPayloadItem.paymentUrl)
                }
            }
        }
    }

    override fun setupObservers() {
        mainViewModel?.orderItem?.observe(viewLifecycleOwner, {
            if (it != null) {
                setupStepUi(it.orderPayloadItem?.isSuccessful)
                stopTopUpTimer()

                epoxyController.setData(it.orderPayloadItem)

                navigateToPaymentUrl(it)

                if (viewModel.isOpenPaymentWebView(it.orderPayloadItem)) {
                    viewModel.setupOrderPayloadItem(it.orderPayloadItem)
                }
            }
        })
    }

    override fun setupListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(this) { }
    }

    override fun onDestroyView() {
        stopTopUpTimer()
        super.onDestroyView()
    }

    private val failedListener = object : OrderResultFailedListener {
        override fun onConfirm() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_topupFragment))
        }
    }

    private val successListener = object : OrderResultSuccessListener {
        override fun onConfirm() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_orderFragment))
        }

        override fun onClose() {
            navigateTo(NavigateItem.Destination(R.id.action_orderResultFragment_to_topupFragment))
        }

        override fun onOpenPaymentWebView(url: String) {
            viewModel.getOrderPayloadItem()?.also {
                it.isCountdownVisible = false
                epoxyController.setData(it)
            }
            GeneralUtils.openWebView(requireContext(), url)
        }
    }

    private fun setupStepUi(isSuccessful: Boolean?) {
        if (isSuccessful == true) {
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

    private fun startTopUpTimer() {
        var count = 1L
        val task = timerTask {
            if (count >= DELAY_TOP_UP_TIME) {
                epoxyController.setData(OrderPayloadItem())
                topUpTimer?.cancel()
            } else {
                epoxyController.setData(null)
                count++
            }
        }
        topUpTimer = Timer()
        topUpTimer?.schedule(task, 1000, 1000)
    }

    private fun stopTopUpTimer() {
        topUpTimer?.cancel()
    }

}