package com.dabenxiang.mimi.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.dabenxiang.mimi.R
import kotlinx.android.synthetic.main.dialog_single_btn_with_icon.*

class SingleBtnWithIconDialog(
    context: Context,
    private val icon: Int,
    private val message: Int,
    private val btnText: Int,
    private val clickBtnListener: OnSingleBtnListener? = null
) : Dialog(context, R.style.dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_single_btn_with_icon)

        iv_icon.setImageResource(icon)
        tv_msg.text = context.resources.getText(message)
        btn_confirm.text = context.resources.getText(btnText)
        btn_confirm.setOnClickListener {
            dismiss()
            clickBtnListener?.onClick()
        }
    }
}