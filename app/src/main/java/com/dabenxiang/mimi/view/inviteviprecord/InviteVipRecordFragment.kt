package com.dabenxiang.mimi.view.inviteviprecord

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.ReferrerHistoryItem
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_invite_vip_record.*
import kotlinx.android.synthetic.main.fragment_text_detail.toolbarContainer
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*
import timber.log.Timber

class InviteVipRecordFragment : BaseFragment() {
    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private val inviteVipRecordAdapter by lazy { InviteVipRecordAdapter(listener) }
    private val listener = object : InviteVipRecordAdapter.EventListener {
        override fun onClickListener(item: ReferrerHistoryItem, position: Int) {
            Timber.d("catkingg click")
        }
    }

    private val viewModel: InviteVipRecordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_toolbar_title.text = getString(R.string.invite_vip_record_title)

        toolbar.setBackgroundColor(requireContext().getColor(R.color.color_gray_2))
        text_toolbar_title.setTextColor(requireContext().getColor(R.color.color_black_1))
        toolbarContainer.toolbar.navigationIcon = ContextCompat.getDrawable(
            requireContext(), R.drawable.btn_back_black_n
        )

        toolbarContainer.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_invite_vip_record
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {
        layout_refresh.setOnRefreshListener {
            layout_refresh.isRefreshing = false
            viewModel.getChatList(inviteVipRecordAdapter)
        }

    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        rv_record.adapter = inviteVipRecordAdapter
        viewModel.getChatList(inviteVipRecordAdapter)
    }
}