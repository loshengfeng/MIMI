package com.dabenxiang.mimi.view.topup

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.holder.TopupOnlinePayItem
import com.dabenxiang.mimi.model.holder.TopupProxyPayItem
import com.dabenxiang.mimi.view.adapter.TopupOnlinePayAdapter
import com.dabenxiang.mimi.view.adapter.TopupProxyPayAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_topup.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TopupFragment : BaseFragment<TopupViewModel>() {
    private val viewModel by viewModel<TopupViewModel>()

    private val onlinePayListener = object : AdapterEventListener<TopupOnlinePayItem> {
        override fun onItemClick(view: View, item: TopupOnlinePayItem) {
            Timber.d("${TopupFragment::class.java.simpleName}_onlinePayListener_onItemClick_item: $item")
        }
    }

    private val proxyPayListener = object : AdapterEventListener<TopupProxyPayItem> {
        override fun onItemClick(view: View, item: TopupProxyPayItem) {
            Timber.d("${TopupFragment::class.java.simpleName}_proxyPayListener_onItemClick_item: $item")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_topup
    }

    override fun fetchViewModel(): TopupViewModel? {
        return viewModel
    }

    override fun setupObservers() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupListeners")

        rg_Type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_online_pay-> {
                    layout_online_pay.visibility = View.VISIBLE
                    rv_proxy_pay.visibility = View.GONE
                }
                R.id.rb_proxy_pay-> {
                    layout_online_pay.visibility = View.GONE
                    rv_proxy_pay.visibility = View.VISIBLE
                }
            }
        }

        tl_type.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> GeneralUtils.showToast(context!!, "Wechat")
                    1 -> GeneralUtils.showToast(context!!, "Alipay")
                    2 -> GeneralUtils.showToast(context!!, "ChinaPay")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btn_pay -> GeneralUtils.showToast(context!!, "btnPay")
            }
        }.also {
            btn_pay.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        tv_name.text = "好大一棵洋梨"
        tv_coco.text = "200"
        tv_subtitle.text = "副标内容副标内容"
        tv_total.text = "¥ 50.00"

        GridLayoutManager(context, 2).also { layoutManager ->
            rv_online_pay.layoutManager = layoutManager
        }

        val onlinePayList = mutableListOf<TopupOnlinePayItem>(
            TopupOnlinePayItem(1, "300", "¥ 50.00", "¥ 55.00"),
            TopupOnlinePayItem(0, "900+90", "¥ 150.00", "¥ 165.00"),
            TopupOnlinePayItem(0, "1500+150", "¥ 250.00", "¥ 275.00"),
            TopupOnlinePayItem(0, "3000+300", "¥ 500.00", "¥ 500.00")
        )

        rv_online_pay.adapter = TopupOnlinePayAdapter(onlinePayListener)
        val onlinePayAdapter = rv_online_pay.adapter as TopupOnlinePayAdapter
        onlinePayAdapter.setDataSrc(onlinePayList)

        activity?.also { activity ->
            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                rv_proxy_pay.layoutManager = layoutManager
            }
        }

        val proxyPayList = mutableListOf<TopupProxyPayItem>(
            TopupProxyPayItem("photo", "火热代理1", "密密1"),
            TopupProxyPayItem("photo", "火热代理2", "密密2"),
            TopupProxyPayItem("photo", "火热代理3", "密密3"),
            TopupProxyPayItem("photo", "火热代理4", "密密4")
        )

        rv_proxy_pay.adapter = TopupProxyPayAdapter(proxyPayListener)
        val proxyAdapter = rv_proxy_pay.adapter as TopupProxyPayAdapter
        proxyAdapter.setDataSrc(proxyPayList)

    }
}