package com.dabenxiang.mimi.view.more

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_more.*

class MoreDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(listener: OnMoreDialogListener): MoreDialogFragment {
            val fragment = MoreDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

    var listener: OnMoreDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_more
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_problem_report.setOnClickListener {
            listener?.onProblemReport()
        }

        tv_cancel.setOnClickListener {
            listener?.onCancel()
        }
    }

    interface OnMoreDialogListener {
        fun onProblemReport()
        fun onCancel()
    }
}