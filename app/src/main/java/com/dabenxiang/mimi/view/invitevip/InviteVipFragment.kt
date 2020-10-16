package com.dabenxiang.mimi.view.invitevip

import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment

class InviteVipFragment : BaseFragment() {
    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun getLayoutId(): Int {
        return R.layout.fragment_invite_vip
    }

    override fun setupObservers() {

    }

    override fun setupListeners() {

    }
}