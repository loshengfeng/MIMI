package com.dabenxiang.mimi.view.invitevip

import android.view.View
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_invite_vip.*

class InviteVipFragment : BaseFragment() {
    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_invite_vip
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {
        iv_invite_vip_back.setOnClickListener {
            findNavController().navigateUp()
        }

        tv_to_invite_vip_record.setOnClickListener {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_inviteVipFragment_to_inviteVipRecordFragment,
                    null
                )
            )
        }

    }
}