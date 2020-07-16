package com.dabenxiang.mimi.view.topup

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.model.holder.TopUpOnlinePayItem
import com.dabenxiang.mimi.model.holder.TopUpProxyPayItem
import com.dabenxiang.mimi.view.adapter.TopUpAgentAdapter
import com.dabenxiang.mimi.view.adapter.TopUpOnlinePayAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_top_up.*
import timber.log.Timber
import java.lang.ClassCastException

class TopUpFragment : BaseFragment() {

    private val viewModel: TopUpViewModel by viewModels()

    private val agentAdapter by lazy { TopUpAgentAdapter(agentListener) }

    private var interactionListener: InteractionListener? = null

    private val onlinePayListener = object : AdapterEventListener<TopUpOnlinePayItem> {
        override fun onItemClick(view: View, item: TopUpOnlinePayItem) {
            Timber.d("${TopUpFragment::class.java.simpleName}_onlinePayListener_onItemClick_item: $item")
        }
    }

    private val proxyPayListener = object : AdapterEventListener<TopUpProxyPayItem> {
        override fun onItemClick(view: View, item: TopUpProxyPayItem) {
            Timber.d("${TopUpFragment::class.java.simpleName}_proxyPayListener_onItemClick_item: $item")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
//            backToDesktop()
            interactionListener?.changeNavigationPosition(R.id.navigation_home)
        }

        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_top_up
    }

    override fun setupObservers() {
        Timber.d("${TopUpFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${TopUpFragment::class.java.simpleName}_setupListeners")

        rg_Type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_online_pay -> {
                    // 暫時不做
//                    layout_online_pay.visibility = View.VISIBLE
//                    rv_proxy_pay.visibility = View.GONE
                }
                R.id.rb_proxy_pay -> {
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
                R.id.btn_pay -> GeneralUtils.showToast(requireContext(), "btnPay")
            }
        }.also {
            btn_pay.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        val userItem = viewModel.getUserData()
        tv_name.text = userItem.friendlyName
        tv_coco.text = userItem.point.toString()
        tv_subtitle.text = getString(R.string.topup_subtitle)
        tv_total.text = "¥ 50.00"

        GridLayoutManager(context, 2).also { layoutManager ->
            rv_online_pay.layoutManager = layoutManager
        }

        val onlinePayList = mutableListOf<TopUpOnlinePayItem>(
            TopUpOnlinePayItem(1, "300", "¥ 50.00", "¥ 55.00"),
            TopUpOnlinePayItem(0, "900+90", "¥ 150.00", "¥ 165.00"),
            TopUpOnlinePayItem(0, "1500+150", "¥ 250.00", "¥ 275.00"),
            TopUpOnlinePayItem(0, "3000+300", "¥ 500.00", "¥ 500.00")
        )

        rv_online_pay.adapter = TopUpOnlinePayAdapter(onlinePayListener)
        val onlinePayAdapter = rv_online_pay.adapter as TopUpOnlinePayAdapter
        onlinePayAdapter.setDataSrc(onlinePayList)

        activity?.also { activity ->
            LinearLayoutManager(activity).also { layoutManager ->
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                rv_proxy_pay.layoutManager = layoutManager
            }
        }

        //todo 尚未測試，因為目前沒有資料可以做測試
        rv_proxy_pay.adapter = agentAdapter

        viewModel.initData()

        tv_record_top_up.setOnClickListener {
            navigateTo(NavigateItem.Destination(R.id.action_topupFragment_to_orderFragment))
        }

        viewModel.agentList.observe(viewLifecycleOwner, Observer {
            agentAdapter.submitList(it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("TopUpFragment interaction listener can't cast")
        }
    }

    private val agentListener = object : AdapterEventListener<AgentItem>{
        override fun onItemClick(view: View, item: AgentItem) {

        }
    }
}