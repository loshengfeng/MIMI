package com.dabenxiang.mimi.view.dialog.clean

import android.view.View
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.fragment_dialog_clean.*

class CleanDialogFragment : BaseDialogFragment() {

    private var onCleanDialogListener: OnCleanDialogListener? = null

    companion object {

        fun newInstance(
            listener: OnCleanDialogListener? = null
        ): CleanDialogFragment {
            val fragment = CleanDialogFragment()
            fragment.onCleanDialogListener = listener
            return fragment
        }
    }

    override fun isFullLayout(): Boolean { return true }

    override fun getLayoutId(): Int { return R.layout.fragment_dialog_clean }

    override fun setupListeners() {
        super.setupListeners()
        View.OnClickListener { btnView ->
            dismiss()
            when(btnView.id) {
                R.id.btn_confirm -> onCleanDialogListener?.onClean()
            }
        }.also {
            layout_root.setOnClickListener(it)
            btn_cancel.setOnClickListener(it)
            btn_confirm.setOnClickListener(it)
        }
    }
}