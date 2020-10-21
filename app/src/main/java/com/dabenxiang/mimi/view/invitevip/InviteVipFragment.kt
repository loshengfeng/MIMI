package com.dabenxiang.mimi.view.invitevip

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.widget.utility.GeneralUtils.copyToClipboard
import com.dabenxiang.mimi.widget.utility.QrCodeUtils
import kotlinx.android.synthetic.main.fragment_invite_vip.*

class InviteVipFragment : BaseFragment() {
    companion object {
        private const val SHOW_COPY_HINT_TIME = 3500L
        private const val SHOW_COPY_HINT_DURATION = 800L
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    private val viewModel: InviteVipViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_invite_vip
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun setupObservers() {
        viewModel.promotionItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Success -> {
                    tv_invite_code.text = viewModel.promotionData?.promotion_code
                    tv_invite_vip_days.text = viewModel.promotionData?.cumulativeDays.toString()
                    tv_invite_vip_people.text = viewModel.promotionData?.promotionNumber.toString()
                    iv_invite_vip_qrcode.setImageBitmap(QrCodeUtils.generateQrCodeImage(viewModel.promotionData?.promotion_url, 300))
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.showCopyHint.observe(viewLifecycleOwner, Observer {
                if(it == true) {
                    bt_invite_copy.isEnabled = false
                    tv_invite_vip_copy_hint.visibility = View.VISIBLE
                    tv_invite_vip_copy_hint?.animate()?.alpha(1.0f)?.setDuration(SHOW_COPY_HINT_DURATION)
                } else {
                    bt_invite_copy.isEnabled = true
                    tv_invite_vip_copy_hint?.animate()?.alpha(0.0f)?.setDuration(SHOW_COPY_HINT_DURATION)
                }
        })
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

        bt_invite_copy.setOnClickListener {
            copyToClipboard(requireContext(), tv_invite_code.text.toString())
            viewModel.setShowCopyHint(SHOW_COPY_HINT_TIME)
        }

    }

    override fun initSettings() {
        super.initSettings()
        viewModel.getPromotionItem()
    }
}