package com.dabenxiang.mimi.view.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import com.google.android.material.tabs.TabLayout
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
                getOrderProxyAttachment = { id, update -> viewModel.getProxyAttachment(id, update) }
            ))
    }

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
        viewModel.balanceResult.observe(viewLifecycleOwner, Observer {
            TabLayoutMediator(tl_type, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "${tabTitle[position]}(${it.allCount})"
                    1 -> "${tabTitle[position]}(${it.isOnlineCount})"
                    else -> "${tabTitle[position]}(${(it.allCount ?: 0) - (it.isOnlineCount ?: 0)})"
                }
                viewPager.setCurrentItem(tab.position, true)
            }.attach()
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
            tab.text = tabTitle[position]
            viewPager.setCurrentItem(tab.position, true)
        }.attach()
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