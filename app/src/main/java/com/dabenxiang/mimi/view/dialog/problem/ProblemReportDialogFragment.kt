package com.dabenxiang.mimi.view.dialog.problem

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.BaseItem
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_problem_report.*
import timber.log.Timber


class ProblemReportDialogFragment : BaseDialogFragment() {

    private var onMoreDialogListener: OnProblemReportDialogListener? = null
    private var selectedItem = ""


    companion object {
        private const val KEY_DATA = "KEY_DATA"

        fun newInstance(
                listener: OnProblemReportDialogListener? = null
        ): ProblemReportDialogFragment {
            val fragment = ProblemReportDialogFragment()
            val args = Bundle()
            fragment.onMoreDialogListener = listener
            fragment.arguments = args
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_problem_report
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resources.getStringArray(R.array.problem_report_item).forEach {
            val rdbtn = RadioButton(context)
            rdbtn.id = View.generateViewId()
            rdbtn.text = it
            rdbtn.setOnClickListener { view ->
                selectedItem = (view as TextView).text.toString()
            }
            rdbtn.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.color_red_8))
            rdbtn.setTextColor(Color.BLACK)
            val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.topMargin = 27
            rdbtn.layoutParams = params
            radio_group.addView(rdbtn)
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        View.OnClickListener { btnView ->
            when (btnView.id) {
                R.id.btn_send -> {
                    if (selectedItem.isNotBlank()) {
                        onMoreDialogListener?.onReport(selectedItem)
                        dismiss()
                    }
                }
                R.id.btn_close -> {
                    dismiss()
                }
            }
        }.also {
            btn_send.setOnClickListener(it)
            btn_close.setOnClickListener(it)
        }
    }
}