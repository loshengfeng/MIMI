package com.dabenxiang.mimi.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.listener.OnSimpleDialogListener
import kotlinx.android.synthetic.main.dialog_update_message_alert.*


class UpdateMessageAlertDialog(
    context: Context,
    private val titleRes: Int,
    private val confirmRes: Int,
    private val cancelRes: Int,
    private val dialogListener: OnSimpleDialogListener
) : Dialog(context, R.style.dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_message_alert)

        setCancelable(false)
        tv_msg.setText(titleRes)
        btn_confirm.setText(confirmRes)
        btn_cancel.setText(cancelRes)

        btn_confirm.setOnClickListener {
            dismiss()
            dialogListener.onConfirm()
        }

        btn_cancel.setOnClickListener {
            dismiss()
            dialogListener.onCancle()
        }
    }
}