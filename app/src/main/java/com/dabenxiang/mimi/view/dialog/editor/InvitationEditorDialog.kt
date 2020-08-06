package com.dabenxiang.mimi.view.dialog.editor

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.listener.OnSimpleEditorDialogListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
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
        setCanceledOnTouchOutside(true)

        tv_editor.setHint(hintRes)
        btn_confirm.setText(confirmRes)
        btn_cancel.setText(cancelRes)

        btn_confirm.setOnClickListener {
            tv_editor.text.toString().let {
                when (it.length) {
                    5 -> {
                        dismiss()
                        dialogListener.onConfirm(it)
                    }
                    0 -> GeneralUtils.showToast(
                        context,
                        context.getString(R.string.setting_enter_invitation)
                    )
                    else -> GeneralUtils.showToast(
                        context,
                        context.getString(R.string.setting_binding_enter_5_digits)
                    )
                }

            }
        }

        btn_cancel.setOnClickListener {
            dismiss()
            dialogListener.onCancel()
        }
    }
}