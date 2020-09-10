package com.dabenxiang.mimi.view.order

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.OrderType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_order.*
import kotlinx.android.synthetic.main.item_setting_bar.*
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

    private var updateProxyTab: ((Int, Boolean) -> Unit)? = null

    private val orderPagerAdapter by lazy {
        OrderPagerAdapter(
            OrderFuncItem(
                getOrderByPaging3 = { update -> getOrderByPaging3(update) },
                getOrderByPaging2 = { type, update -> viewModel.getOrderByPaging2(type, update) },
                getChatList = { update -> viewModel.getChatList(update) },
                getChatAttachment = { id, view -> viewModel.loadImage(id, view, LoadImageType.AVATAR_CS) },
                onChatItemClick = { item -> onChatItemClick(item) },
                getOrderProxyAttachment = { id, view -> viewModel.loadImage(id, view, LoadImageType.AVATAR_CS) },
                onContactClick = { chatListItem, orderItem -> onContactClick(chatListItem, orderItem) },
                getProxyUnread = { update -> getProxyUnread(update) },
                onTopUpClick = { onTopUpClick() }
            ))
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private var interactionListener: InteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("OrderFragment interaction listener can't cast")
        }
    }

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
                        1 -> "$title(${it.user2Online})"
                        else -> "$title(${it.merchant2UserCount})"
                    }
                }
            }
        })

        viewModel.unreadResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    viewModel.unreadCount = it.result
                    tl_type.getTabAt(2)?.also { tab ->
                        tab.customView?.findViewById<ImageView>(R.id.iv_new)?.visibility =
                            takeIf { viewModel.unreadCount > 0 }?.let { View.VISIBLE }
                                ?: let { View.GONE }
                    }
                }
            }
            viewModel.getUnReadOrderCount()
        })

        viewModel.unreadOrderResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Success -> {
                    viewModel.unreadOrderCount = it.result
                    tl_type.getTabAt(1)?.also { tab ->
                        tab.customView?.findViewById<ImageView>(R.id.iv_new)?.visibility =
                            takeIf { viewModel.unreadOrderCount > 0 }?.let { View.VISIBLE }
                                ?: let { View.GONE }
                    }

                    tl_type.getTabAt(0)?.also { tab ->
                        tab.customView?.findViewById<ImageView>(R.id.iv_new)?.visibility =
                            takeIf { viewModel.unreadCount + viewModel.unreadOrderCount > 0 }?.let { View.VISIBLE }
                                ?: let { View.GONE }
                    }
                }
            }
        })

        viewModel.createOrderChatResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Success -> {
                    val createOrderChatItem = it.result.first
                    val chatListItem = it.result.second
                    val orderItem = it.result.third
                    onChatItemClick(
                        ChatListItem(
                            id = createOrderChatItem.chatId,
                            name = chatListItem.name,
                            avatarAttachmentId = chatListItem.avatarAttachmentId,
                            lastReadTime = chatListItem.lastReadTime
                        ),
                        OrderItem(traceLogId = createOrderChatItem.id, type = orderItem.type)
                    )
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Error -> onApiError(it.throwable)
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
        updateProxyTab?.also { getProxyUnread(it) }
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

    private fun onChatItemClick(item: ChatListItem, orderItem: OrderItem = OrderItem()) {
        navigateTo(
            NavigateItem.Destination(
                R.id.action_orderFragment_to_chatContentFragment,
                ChatContentFragment.createBundle(item, orderItem.traceLogId, orderItem.type == OrderType.USER2ONLINE)
            )
        )
    }

    private fun onContactClick(chatListItem: ChatListItem, orderItem: OrderItem) {
        if (chatListItem.id != 0L && orderItem.traceLogId != 0L) {
            onChatItemClick(chatListItem, orderItem)
        } else {
            viewModel.createOrderChat(chatListItem, orderItem)
        }
    }

    private fun getProxyUnread(update: ((Int, Boolean) -> Unit)) {
        updateProxyTab = update
        viewModel.getProxyOrderUnread(update)
        viewModel.getChatUnread(update)
    }

    private fun onTopUpClick() {
        findNavController().navigateUp()
        interactionListener?.changeNavigationPosition(R.id.navigation_topup)
    }
}