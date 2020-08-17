package com.dabenxiang.mimi.view.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.OrderAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.item_order_no_data.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class OrderFragment : BaseFragment() {

    companion object {
        const val NO_DATA = 0
        const val TYPE_ALL = 0
        const val TYPE_ONLINE_PAY = 1
        const val TYPE_PROXY_PAY = 2
    }

    private val viewModel: OrderViewModel by viewModels()

    private val orderAdapter by lazy { OrderAdapter() }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order
    }

    override fun setupObservers() {
        viewModel.orderList.observe(viewLifecycleOwner, Observer {
            refreshUi(it.size)
            orderAdapter.submitList(it)
        })
    }

    override fun setupListeners() {
        tl_type.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.getOrder(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.tv_topup -> GeneralUtils.showToast(requireContext(), "btn_topup")
            }
        }.also {
            tv_back.setOnClickListener(it)
            tv_topup.setOnClickListener(it)
        }

        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getOrder(tl_type.selectedTabPosition)
        }
    }

    override fun initSettings() {
        tv_title.text = getString(R.string.personal_order)
        tv_text.text = "文字文字"
        viewModel.getOrder(TYPE_ALL)
        rv_content.adapter = orderAdapter
    }

    private fun refreshUi(size: Int) {
        layout_refresh.visibility = when (size) {
            NO_DATA -> View.GONE
            else -> View.VISIBLE
        }

        item_no_data.visibility = when (size) {
            NO_DATA -> View.VISIBLE
            else -> View.GONE
        }

        val title = when (tl_type.selectedTabPosition) {
            TYPE_ALL -> getString(R.string.topup_all)
            TYPE_ONLINE_PAY -> getString(R.string.topup_online_pay)
            else -> getString(R.string.topup_proxy_pay)
        }

        tl_type.getTabAt(tl_type.selectedTabPosition)?.text =
            StringBuilder(title).append("(").append(size).append(")").toString()
    }
}