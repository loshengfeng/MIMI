package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.enums.PostType
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils.dpToPx
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.android.synthetic.main.fragment_dialog_report.*
import timber.log.Timber

class ReportDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(
            item: BaseMemberPostItem,
            listener: OnReportDialogListener
        ): ReportDialogFragment {
            val fragment = ReportDialogFragment()
            fragment.item = item
            fragment.listener = listener
            return fragment
        }
    }

    var item: BaseMemberPostItem? = null
    var listener: OnReportDialogListener? = null

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_report
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var reportContent = ""

        val problems = when (item) {
            is MemberPostItem -> {
                val id = when ((item as MemberPostItem).type) {
                    PostType.IMAGE -> R.array.picture_problem_report_item
                    PostType.TEXT -> R.array.text_problem_report_item
                    else -> R.array.video_problem_report_item
                }
                requireContext().resources?.getStringArray(id)
            }
            else -> {
                requireContext().resources?.getStringArray(R.array.text_problem_report_item)
            }
        }

        problems?.forEach {
            Timber.i("problems= $it")
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, dpToPx(requireContext(), 16))
            val radioButton = MaterialRadioButton(requireContext())
            radioButton.text = it
            radioButton.setTextColor(requireContext().getColor(R.color.color_black_1))
            radioButton.layoutParams = params
            rg_report_problem.addView(radioButton)
        }

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
        fun onSend(item: BaseMemberPostItem, content: String)
        fun onCancel()
    }

}