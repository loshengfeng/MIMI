package com.dabenxiang.mimi.view.dialog.more

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.vo.BaseItem
import kotlinx.android.synthetic.main.fragment_dialog_more.*
import kotlinx.android.synthetic.main.fragment_dialog_more.btn_close
import kotlinx.android.synthetic.main.fragment_dialog_more.layout_root

class MoreDialogFragment : BaseDialogFragment() {

    private var onMoreDialogListener: OnMoreDialogListener? = null
    private var data: BaseItem?= null

    companion object {
        private const val KEY_DATA = "KEY_DATA"

        fun newInstance(
            item: BaseItem,
            listener: OnMoreDialogListener? = null
        ): MoreDialogFragment {
            val fragment = MoreDialogFragment()
            val args = Bundle()
            args.putSerializable(KEY_DATA, item)
            fragment.onMoreDialogListener = listener
            fragment.arguments = args
            return fragment
        }
    }

    override fun isFullLayout(): Boolean { return true }

    override fun getLayoutId(): Int { return R.layout.fragment_dialog_more }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data = arguments?.getSerializable(KEY_DATA) as BaseItem
    }

    override fun setupListeners() {
        super.setupListeners()
        View.OnClickListener { btnView ->
            dismiss()
            when(btnView.id) {
                R.id.btn_report -> data?.let { onMoreDialogListener?.onReport(data!!) }
            }
        }.also {
            layout_root.setOnClickListener(it)
            btn_close.setOnClickListener(it)
            btn_report.setOnClickListener(it)
        }
    }
}