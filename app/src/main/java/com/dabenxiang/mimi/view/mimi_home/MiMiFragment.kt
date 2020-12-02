package com.dabenxiang.mimi.view.mimi_home

import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils.getScreenSize
import com.dabenxiang.mimi.widget.utility.GeneralUtils.pxToDp
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mimi_home.*
import kotlinx.android.synthetic.main.fragment_mimi_home.iv_invitevip
import kotlinx.android.synthetic.main.fragment_mimi_home.layout_invitevip

class MiMiFragment : BaseFragment() {

    private val viewModel: MiMiViewModel by viewModels()

    companion object {
        private const val ANIMATE_INTERVAL = 6500L
    }

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = pxToDp(requireContext(), getScreenSize(requireActivity()).first)
        viewModel.adHeight = (viewModel.adWidth / 7)

        btn_retry.setOnClickListener { viewModel.getMenu() }

        viewModel.menusItems.observe(this, {
            when (it) {
                is Success -> setupUi(it.result)
                is Error -> {
                    onApiError(it.throwable)
                    layout_server_error.visibility = View.VISIBLE
                }
            }
        })

        viewModel.getMenu()
        viewModel.startAnim(ANIMATE_INTERVAL)
    }

    override fun getLayoutId() = R.layout.fragment_mimi_home

    private fun setupUi(menusItems: List<SecondMenuItem>) {
        layout_server_error.visibility = View.INVISIBLE
        viewpager.offscreenPageLimit = menusItems.size
        viewpager.isSaveEnabled = false
        viewpager.adapter = MiMiViewPagerAdapter(this, menusItems)
        TabLayoutMediator(layout_tab, viewpager) { tab, position ->
            tab.text = menusItems[position].name
        }.attach()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.inviteVipShake.observe(this, Observer {
            if (layout_invitevip.visibility != View.GONE) {
                if (it == true)
                    iv_invitevip.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.anim_shake
                        )
                    )
                else
                    viewModel.startAnim(ANIMATE_INTERVAL)
            }
        })
    }

    override fun setupListeners() {
        super.setupListeners()
        iv_invitevip.setOnClickListener {
            navigateTo(
                NavigateItem.Destination(
                    R.id.action_to_inviteVipFragment,
                    null
                )
            )
        }

        iv_invitevip_close.setOnClickListener {
            layout_invitevip.visibility = View.GONE
        }
    }

}
