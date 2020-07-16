package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_report.*
import kotlinx.android.synthetic.main.fragment_dialog_report.background

class ReportDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(
            item: MemberPostItem,
            listener: OnReportDialogListener
        ): ReportDialogFragment {
            val fragment = ReportDialogFragment()
            fragment.item = item
            fragment.listener = listener
            return fragment
        }
    }

    var item: MemberPostItem? = null
    var listener: OnReportDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_report
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var reportContent = rb_report1.text.toString()

        rg_report_problem.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = rg_report_problem.findViewById<RadioButton>(checkedId)
            reportContent = radioButton.text.toString()
        }

        tv_send.setOnClickListener {
            listener?.onSend(item!!, reportContent)
        }

        tv_close.setOnClickListener {
            listener?.onCancel()
        }

        background.setOnClickListener {
            listener?.onCancel()
        }
    }

    interface OnReportDialogListener {
        fun onSend(item: MemberPostItem, content: String)
        fun onCancel()
    }

}