package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_more.*

class MoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(
            item: BaseMemberPostItem,
            listener: OnMoreDialogListener
        ): MoreDialogFragment {
            val fragment = MoreDialogFragment()
            fragment.item = item
            fragment.listener = listener
            return fragment
        }
    }

    var item: BaseMemberPostItem? = null
    var listener: OnMoreDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_more
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isReport = when (item) {
            is MemberPostItem -> (item as MemberPostItem).reported
            else -> (item as MembersPostCommentItem).reported
        } ?: false

        if (isReport) {
            tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1_50))
        } else {
            tv_problem_report.setTextColor(requireContext().getColor(R.color.color_black_1))
            tv_problem_report.setOnClickListener {
                listener?.onProblemReport(item!!)
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
        fun onProblemReport(item: BaseMemberPostItem)
        fun onCancel()
    }
}