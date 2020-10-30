package com.dabenxiang.mimi.view.dialog.dailycheckin

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_daily_check_in.*

class DailyCheckInDialogFragment : BaseDialogFragment() {

    private val viewModel: DailyCheckInDialogViewModel by viewModels()

    companion object {
        fun newInstance(): DailyCheckInDialogFragment {
            return DailyCheckInDialogFragment()
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_daily_check_in
    }

    override fun setupView() {
        val hint = SpannableString(
            getString(
                R.string.daily_check_in_hint,
                viewModel.getVideoOnDemandCount(),
                viewModel.getVideoCount()
            )
        )
        hint.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ), hint.indexOf("秘密视频 ") + 5, hint.indexOf(" 部"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        hint.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_red_1
                )
            ), hint.indexOf("短视频 ") + 4, hint.lastIndexOf(" 部"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv_message_info.text = hint
    }

    override fun setupListeners() {
        super.setupListeners()
        btn_confirm.setOnClickListener {
            dismiss()
        }
    }
}