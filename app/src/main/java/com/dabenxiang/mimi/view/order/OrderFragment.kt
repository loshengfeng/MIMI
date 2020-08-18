package com.dabenxiang.mimi.view.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.OrderAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.fragment_order.viewPager
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.android.synthetic.main.item_setting_bar.tv_title

class OrderFragment : BaseFragment() {

    companion object {
        const val NO_DATA = 0
        const val TYPE_ALL = 0
        const val TYPE_ONLINE_PAY = 1
        const val TYPE_PROXY_PAY = 2

        val tabTitle = arrayListOf(
            App.self.getString(R.string.topup_all),
            App.self.getString(R.string.topup_online_pay),
            App.self.getString(R.string.topup_proxy_pay)
        )
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
            }
        }.also {
            tv_back.setOnClickListener(it)
        }
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        tv_title.text = getString(R.string.personal_order)

        viewPager.adapter = OrderPagerAdapter()

        TabLayoutMediator(tl_type, viewPager) { tab, position ->
            tab.text = tabTitle[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    private fun refreshUi(size: Int) {
        val title = when (tl_type.selectedTabPosition) {
            TYPE_ALL -> getString(R.string.topup_all)
            TYPE_ONLINE_PAY -> getString(R.string.topup_online_pay)
            else -> getString(R.string.topup_proxy_pay)
        }

        tl_type.getTabAt(tl_type.selectedTabPosition)?.text =
            StringBuilder(title).append("(").append(size).append(")").toString()
    }
}