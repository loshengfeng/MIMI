package com.dabenxiang.mimi.view.topup

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.AgentItem
import com.dabenxiang.mimi.model.api.vo.ChatListItem
import com.dabenxiang.mimi.model.api.vo.OrderingPackageItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.model.enums.PaymentType
import com.dabenxiang.mimi.view.adapter.TopUpAgentAdapter
import com.dabenxiang.mimi.view.adapter.TopUpOnlinePayAdapter
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.chatcontent.ChatContentFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.orderinfo.OrderInfoFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_top_up.*
import kotlinx.android.synthetic.main.fragment_top_up.iv_photo
import kotlinx.android.synthetic.main.fragment_top_up.tv_expiry_date
import kotlinx.android.synthetic.main.fragment_top_up.tv_name
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TopUpFragment : BaseFragment() {

    private val viewModel: TopUpViewModel by viewModels()

    private val agentAdapter by lazy { TopUpAgentAdapter(agentListener) }
    private val onlinePayAdapter by lazy { TopUpOnlinePayAdapter(requireContext()) }

    private var interactionListener: InteractionListener? = null

    private var orderPackageMap: HashMap<PaymentType, ArrayList<OrderingPackageItem>>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("TopUpFragment interaction listener can't cast")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(R.id.navigation_adult)
        }
        initSettings()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getTotalUnread()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_top_up
    }

    override fun setupObservers() {
        viewModel.meItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    tv_name.text = it.result.friendlyName
                    it.result.expiryDate?.let {date ->
                        tv_expiry_date.visibility =View.VISIBLE
                        tv_expiry_date.text = getString(R.string.vip_expiry_date,
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).format(date)
                        )
                    }

                    viewModel.loadImage(
                        it.result.avatarAttachmentId,
                        iv_photo,
                        LoadImageType.AVATAR
                    )
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.createChatRoomResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    ChatListItem(
                        it.result.toLong(),
                        viewModel.currentItem?.merchantName,
                        avatarAttachmentId = viewModel.currentItem?.avatarAttachmentId?.toLong()
                    ).also {
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

        viewModel.isEmailConfirmed.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    if (!it.result) {
                        interactionListener?.changeNavigationPosition(R.id.navigation_personal)
                    } else {
                        initTopUp()
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.orderPackageResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loaded -> tv_proxy_empty.visibility = View.GONE
                is Success -> {
                    orderPackageMap = it.result
                    updateOrderPackages(PaymentType.BANK)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.pendingOrderResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val item = it.result
                    if (item.pendingOrders >= item.pendingOrderLimit) {
                        layout_error.visibility = View.VISIBLE
                        layout_next.visibility = View.GONE
                        tv_pending_order.text = StringBuilder()
                            .append(getString(R.string.topup_pending_order_1))
                            .append(item.pendingOrders)
                            .append(getString(R.string.topup_pending_order_2))
                            .toString()
                    } else {
                        layout_error.visibility = View.GONE
                        layout_next.visibility = View.VISIBLE
                        val selectItem = onlinePayAdapter.getSelectItem()
                        val bundle = OrderInfoFragment.createBundle(selectItem)
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_orderInfoFragment,
                                bundle
                            )
                        )
                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.agentList.observe(viewLifecycleOwner, Observer {
            agentAdapter.submitList(it)
        })

        viewModel.agentListIsEmpty.observe(viewLifecycleOwner, Observer {
            if (it) {
                tv_proxy_empty.visibility = View.VISIBLE
            } else {
                tv_proxy_empty.visibility = View.GONE
            }
        })

        viewModel.totalUnreadResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    iv_new_badge.visibility = if (it.result == 0) View.INVISIBLE else View.VISIBLE
                    interactionListener?.refreshBottomNavigationBadge(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

        tv_record_top_up.setOnClickListener {
            navigateTo(NavigateItem.Destination(R.id.action_topupFragment_to_orderFragment))
        }

        tv_goto_top_up_manage.setOnClickListener {
            navigateTo(NavigateItem.Destination(R.id.action_topupFragment_to_orderFragment))
        }

        rg_type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_online_pay -> {
                    layout_online_pay.visibility = View.VISIBLE
                    rv_proxy_pay.visibility = View.GONE
                    tv_proxy_empty.visibility = View.GONE
                }
                R.id.rb_proxy_pay -> {
                    layout_online_pay.visibility = View.GONE
                    rv_proxy_pay.visibility = View.VISIBLE
                    tv_proxy_empty.visibility = View.VISIBLE
                    viewModel.getProxyPayList()
                }
            }
        }

        tl_type.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> updateOrderPackages(PaymentType.BANK)
                    1 -> updateOrderPackages(PaymentType.ALI)
                    2 -> updateOrderPackages(PaymentType.WX)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btn_next -> {
                    val selectItem = onlinePayAdapter.getSelectItem()
                    if (selectItem == null) {
                        GeneralUtils.showToast(
                            requireContext(),
                            getString(R.string.topup_select_error)
                        )
                    } else {
                        viewModel.getPendingOrderCount()
                    }
                }
                R.id.tv_login -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_loginFragment,
                        LoginFragment.createBundle(LoginFragment.TYPE_LOGIN)
                    )
                )
                R.id.tv_register -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_to_loginFragment,
                        LoginFragment.createBundle(LoginFragment.TYPE_REGISTER)
                    )
                )
            }
        }.also {
            btn_next.setOnClickListener(it)
            tv_login.setOnClickListener(it)
            tv_register.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        rg_type.check(R.id.rb_online_pay)

        when (viewModel.isLogin()) {
            true -> {
                //TODO: 目前先不判斷是否有驗證過
//                viewModel.checkEmailConfirmed()
                initTopUp()
            }
            false -> {
                tv_record_top_up.visibility = View.GONE
                tv_version_is_not_login.text = BuildConfig.VERSION_NAME
                item_is_Login.visibility = View.GONE
                item_is_not_Login.visibility = View.VISIBLE
            }
        }
    }

    private fun initTopUp() {
        tv_record_top_up.visibility = View.VISIBLE

        item_is_Login.visibility = View.VISIBLE
        item_is_not_Login.visibility = View.GONE

        layout_error.visibility = View.GONE
        layout_next.visibility = View.VISIBLE

        tv_subtitle.text = getString(R.string.topup_subtitle)

        val userItem = viewModel.getUserData()
        tv_name.text = userItem.friendlyName
        tv_expiry_date.visibility = View.GONE

        rv_online_pay.layoutManager = GridLayoutManager(context, 2)
        rv_online_pay.adapter = onlinePayAdapter

        rv_proxy_pay.layoutManager = LinearLayoutManager(context)
        rv_proxy_pay.adapter = agentAdapter

        tl_type.getTabAt(0)?.select()

        onlinePayAdapter.clearSelectItem()
        viewModel.getMe()
        viewModel.getOrderingPackage()
    }

    private val agentListener = object : TopUpAgentAdapter.EventListener {
        override fun onItemClick(view: View, item: AgentItem) {
            viewModel.createChatRoom(item)
        }

        override fun onGetAvatarAttachment(id: Long?, view: ImageView) {
            viewModel.loadImage(id, view, LoadImageType.AVATAR)
        }
    }

    private fun updateOrderPackages(paymentType: PaymentType) {
        val orderPackages = orderPackageMap?.get(paymentType) ?: arrayListOf()
        if (orderPackages.isEmpty()) {
            tv_online_empty.visibility = View.VISIBLE
        } else {
            tv_online_empty.visibility = View.GONE
        }
        onlinePayAdapter.setupData(orderPackages)
        onlinePayAdapter.notifyDataSetChanged()
    }
}