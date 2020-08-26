package com.dabenxiang.mimi.view.order

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.fragment_order.viewPager
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.android.synthetic.main.item_setting_bar.tv_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class OrderFragment : BaseFragment() {

    companion object {
        val tabTitle = arrayListOf(
            App.self.getString(R.string.topup_all),
            App.self.getString(R.string.topup_online_pay),
            App.self.getString(R.string.topup_proxy_pay)
        )
    }

    private val viewModel: OrderViewModel by viewModels()

    private val orderPagerAdapter by lazy {
        OrderPagerAdapter(
            OrderFuncItem(
                getOrderByPaging3 = { update -> getOrderByPaging3(update) },
                getOrderByPaging2 = { isOnline, update ->
                    viewModel.getOrderByPaging2(
                        isOnline,
                        update
                    )
                },
                getChatList = { update -> viewModel.getChatList(update) },
                getChatAttachment = { id, pos, update -> viewModel.getAttachment(id, pos, update) },
                onChatItemClick = { item -> onChatItemClick(item) },
                getOrderProxyAttachment = { id, update -> viewModel.getProxyAttachment(id, update) },
                onContactClick = { id, chatId -> Timber.d("onContactClick $id, $chatId") }
            ))
    }



    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            navigateTo(NavigateItem.Up)
        }
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_order
    }

    override fun setupObservers() {
        viewModel.balanceResult.observe(viewLifecycleOwner, Observer {
            for (i in 0 until tl_type.tabCount) {
                val title = tabTitle[i]
                tl_type.getTabAt(i)?.also { tab ->
                    tab.customView?.findViewById<TextView>(R.id.tv_title)?.text = when (i) {
                        0 -> "$title(${it.allCount})"
                        1 -> "$title(${it.isOnlineCount})"
                        else -> "$title(${(it.allCount ?: 0) - (it.isOnlineCount ?: 0)})"
                    }
                }
            }
        })

        viewModel.unreadResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    viewModel.unreadCount = it.result
                    tl_type.getTabAt(2)?.takeIf { viewModel.unreadCount > 0 }?.also { tab ->
                        tab.customView?.findViewById<ImageView>(R.id.iv_new)?.visibility = View.VISIBLE
                    }
                }
            }
            viewModel.getUnReadOrderCount()
        })

        viewModel.unreadOrderResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    viewModel.unreadOrderCount = it.result
                    tl_type.getTabAt(1)?.takeIf { viewModel.unreadOrderCount > 0 }?.also { tab ->
                        tab.customView?.findViewById<ImageView>(R.id.iv_new)?.visibility = View.VISIBLE
                    }

                    tl_type.getTabAt(0)?.takeIf { viewModel.unreadCount + viewModel.unreadOrderCount > 0 }?.also { tab ->
                        tab.customView?.findViewById<ImageView>(R.id.iv_new)?.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    override fun setupListeners() {
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

        viewPager.adapter = orderPagerAdapter

        TabLayoutMediator(tl_type, viewPager) { tab, position ->
            tab.setCustomView(R.layout.badged_tab)
            tab.customView?.findViewById<TextView>(R.id.tv_title)?.text = tabTitle[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.getUnread()
    }

    private var getOrderJob: Job? = null
    private fun getOrderByPaging3(update: ((PagingData<OrderItem>, CoroutineScope) -> Unit)) {
        getOrderJob?.cancel()
        getOrderJob = lifecycleScope.launch {
            viewModel.getOrderByPaging3().collectLatest {
                update(it, this)
            }
        }
    }

    private fun onChatItemClick(item: ChatListItem) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_orderFragment_to_chatContentFragment,
                ChatContentFragment.createBundle(item)
            )
        )
    }
}