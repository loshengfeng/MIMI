package com.dabenxiang.mimi.view.topup

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.adapter.TopupAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_topup.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TopupFragment : BaseFragment() {

    private val viewModel by viewModel<TopupViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_topup
    }

    override fun setupObservers() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${TopupFragment::class.java.simpleName}_setupListeners")

        rg_Type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_online_pay-> GeneralUtils.showToast(context!!, "rb_online_pay")
                R.id.rb_proxy_pay-> GeneralUtils.showToast(context!!, "rb_proxy_pay")
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

    private fun initSettings() {
        tv_name.text = "好大一棵洋梨"
        tv_coco.text = "200"
        tv_subtitle.text = "副标内容副标内容"
        tv_total.text = "¥ 50.00"

        GridLayoutManager(context, 2).also { layoutManager ->
            rv_content.layoutManager = layoutManager
        }
        rv_content.adapter = TopupAdapter()
    }
}