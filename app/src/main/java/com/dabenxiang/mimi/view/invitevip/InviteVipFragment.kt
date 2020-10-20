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
import com.dabenxiang.mimi.widget.utility.QrCodeUtils
import kotlinx.android.synthetic.main.fragment_invite_vip.*

class InviteVipFragment : BaseFragment() {
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
                    iv_invite_vip_qrcode.setImageBitmap(QrCodeUtils.generateQrCodeImage("www.google.com", 300))
                }
                is ApiResult.Error -> onApiError(it.throwable)
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

    }

    override fun initSettings() {
        super.initSettings()
        viewModel.getPromotionItem()
    }
}