package com.dabenxiang.mimi.view.report

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_report.*

class ReportDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(listener: OnReportDialogListener): ReportDialogFragment {
            val fragment = ReportDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

    var listener: OnReportDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_report
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_close.setOnClickListener {
            listener?.onCancel()
        }
    }

    interface OnReportDialogListener {
        fun onCancel()
    }

}