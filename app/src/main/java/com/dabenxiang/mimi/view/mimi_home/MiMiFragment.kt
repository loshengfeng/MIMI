package com.dabenxiang.mimi.view.mimi_home

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.Error
import com.dabenxiang.mimi.model.api.ApiResult.Success
import com.dabenxiang.mimi.model.api.vo.SecondMenuItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.announce.AnnounceDialogFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_mimi_home.*

class MiMiFragment : BaseFragment() {

    companion object {
        private const val ANIMATE_INTERVAL = 6500L
    }

    private val viewModel: MiMiViewModel by viewModels()

    override fun setupFirstTime() {
        super.setupFirstTime()

        viewModel.adWidth = GeneralUtils.getAdSize(requireActivity()).first
        viewModel.adHeight = GeneralUtils.getAdSize(requireActivity()).second

        btn_retry.setOnClickListener { viewModel.getMenu() }

        viewModel.inviteVipShake.observe(this, {
            if (layout_invitevip.visibility != View.GONE) {
                if (it == true) {
                    iv_invitevip.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.anim_shake
                        )
                    )
                } else {
                    viewModel.startAnim(ANIMATE_INTERVAL)
                }
            }
        })

        viewModel.menusItems.observe(this, {
            when (it) {
                is Success -> {
                    setupUi(it.result)

                    AnnounceDialogFragment.newInstance().also { fragment ->
                        fragment.show(
                            requireActivity().supportFragmentManager,
                            AnnounceDialogFragment::class.java.simpleName
                        )
                    }
                }
                is Error -> {
                    onApiError(it.throwable)
                    layout_server_error.visibility = View.VISIBLE
                }
                else -> {
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
            val view = View.inflate(requireContext(), R.layout.custom_tab, null)
            val textView = view?.findViewById<TextView>(R.id.tv_title)
            textView?.text = menusItems[position].name
            tab.customView = view
        }.attach()
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
