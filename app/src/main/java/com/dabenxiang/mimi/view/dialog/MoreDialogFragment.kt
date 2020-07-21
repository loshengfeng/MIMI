package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.MoreDialogData
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_more.*

class MoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(item: MoreDialogData, listener: OnMoreDialogListener): MoreDialogFragment {
            val fragment = MoreDialogFragment()
            fragment.item = item
            fragment.listener = listener
            return fragment
        }
    }

    var item: MoreDialogData? = null
    var listener: OnMoreDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_more
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isReport = item?.reported ?: false
        if (isReport) {
            tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1_50))
        } else {
            tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1))
            tv_problem_report.setOnClickListener {
                item?.let { moreDialogData -> listener?.onProblemReport(moreDialogData) }
            }
        }

        tv_cancel.setOnClickListener {
            listener?.onCancel()
        }

        background.setOnClickListener {
            listener?.onCancel()
        }
    }

    interface OnMoreDialogListener {
        fun onProblemReport(item: MoreDialogData)
        fun onCancel()
    }
}