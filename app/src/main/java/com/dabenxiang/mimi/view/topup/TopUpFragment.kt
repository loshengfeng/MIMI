package com.dabenxiang.mimi.view.topup

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.holder.TopUpOnlinePayItem
import com.dabenxiang.mimi.model.holder.TopUpProxyPayItem
import com.dabenxiang.mimi.view.adapter.TopUpAgentAdapter
import com.dabenxiang.mimi.view.adapter.TopUpOnlinePayAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import com.dabenxiang.mimi.view.listener.AdapterEventListener
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_top_up.*
import timber.log.Timber

class TopUpFragment : BaseFragment() {

    private val viewModel: TopUpViewModel by viewModels()

    private val agentAdapter by lazy { TopUpAgentAdapter(agentListener) }

    private var interactionListener: InteractionListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(R.id.navigation_home)
        }

        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_top_up
    }

    override fun setupObservers() {
        viewModel.meItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    tv_name.text = it.result.friendlyName
                    tv_coco.text = it.result.availablePoint.toString()
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.avatar.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    val options: RequestOptions = RequestOptions()
                        .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                        .placeholder(R.drawable.ico_default_photo)
                        .error(R.drawable.ico_default_photo)
                        .priority(Priority.NORMAL)


                    Glide.with(this).load(it.result)
                        .apply(options)
                        .into(iv_photo)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.createChatRoomResult.observe(viewLifecycleOwner, Observer {
            when(it){
                is Success -> {
                    viewModel.currentItem?.let { item ->
                        ChatListItem(item.agentId?.toLong(), item.merchantName, avatarAttachmentId = item.avatarAttachmentId?.toLong())
                    }?.also {
                        val bundle = ChatContentFragment.createBundle(it)
                        navigateTo(
                                NavigateItem.Destination(
                                        R.id.action_topupFragment_to_chatContentFragment,
                                        bundle
                                )
                        )
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })
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

        // TODO: 尚未測試，因為目前沒有資料可以做測試
        rv_proxy_pay.adapter = agentAdapter

        viewModel.initData()

        // TODO: 目前這階段無需開發訂單管理功能, 因此暫時隱藏起來
        tv_record_top_up.visibility = View.GONE

        tv_record_top_up.setOnClickListener {
            navigateTo(NavigateItem.Destination(R.id.action_topupFragment_to_orderFragment))
        }

        viewModel.getMe()

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

    private val agentListener = object : AdapterEventListener<AgentItem> {
        override fun onItemClick(view: View, item: AgentItem) {
            viewModel.createChatRoom(item)
        }
    }

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
}