package com.dabenxiang.mimi.view.dialog.editor

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.listener.OnSimpleEditorDialogListener
import kotlinx.android.synthetic.main.dialog_editor.*


class InvitationEditorDialog(
    context: Context,
    private val hintRes: Int,
    private val confirmRes: Int,
    private val cancelRes: Int,
    private val dialogListener: OnSimpleEditorDialogListener
) : Dialog(context, R.style.dialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_editor)

        setCancelable(false)
        tv_editor.setHint(hintRes)
        btn_confirm.setText(confirmRes)
        btn_cancel.setText(cancelRes)

        btn_confirm.setOnClickListener {
            tv_editor.text.toString().let {
                if(it.length ==5)  {
                    dismiss()
                    dialogListener.onConfirm(it)
                }
            }
        }

        btn_cancel.setOnClickListener {
            dismiss()
            dialogListener.onCancel()
        }
    }
}