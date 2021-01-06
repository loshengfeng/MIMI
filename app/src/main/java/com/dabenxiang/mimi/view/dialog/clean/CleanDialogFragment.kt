package com.dabenxiang.mimi.view.dialog.clean

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_clean.*

class CleanDialogFragment : BaseDialogFragment() {

    private var onCleanDialogListener: OnCleanDialogListener? = null
    private var msgResId: Int? = 0
    private var msg:String = ""

    companion object {

        fun newInstance(
            listener: OnCleanDialogListener? = null,
            msgResId: Int? = R.string.favorite_btn_clean
        ): CleanDialogFragment {
            val fragment = CleanDialogFragment()
            fragment.onCleanDialogListener = listener
            fragment.msgResId = msgResId
            fragment.isCancelable = false
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_clean
    }

    override fun setupView() {
        msgResId?.let { tv_msg.setText(it) }
    }

    fun setMsg(msg:String){
        this.msg = msg
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!TextUtils.isEmpty(msg))
            tv_msg.text = msg
    }

    override fun setupListeners() {
        super.setupListeners()
        View.OnClickListener { btnView ->
            dismiss()
            when (btnView.id) {
                R.id.btn_confirm -> onCleanDialogListener?.onClean()
                R.id.btn_cancel -> onCleanDialogListener?.onCancel()
            }
        }.also {
            layout_root.setOnClickListener(it)
            btn_cancel.setOnClickListener(it)
            btn_confirm.setOnClickListener(it)
        }
    }
}