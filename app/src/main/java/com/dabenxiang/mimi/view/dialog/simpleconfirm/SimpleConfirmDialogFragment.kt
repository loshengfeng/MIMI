package com.dabenxiang.mimi.view.dialog.simpleconfirm

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_simple_confirm_dialog.*
import timber.log.Timber


class SimpleConfirmDialogFragment : BaseDialogFragment() {

    companion object {
        private const val KEY_MESSAGE = "KEY_MESSAGE"
        private const val KEY_POSITIVE_BTN_TEXT = "KEY_POSITIVE_BTN_TEXT"
        private const val KEY_NEGATIVE_BTN_TEXT = "KEY_NEGATIVE_BTN_TEXT"
        fun newInstance(
            message: String,
            positiveBtnText: String,
            negativeBtnText: String,
            listener: OnSimpleDialogListener? = null
        ): SimpleConfirmDialogFragment {
            val fragment = SimpleConfirmDialogFragment()
            val args = Bundle()
            args.putString(KEY_MESSAGE, message)
            args.putString(KEY_POSITIVE_BTN_TEXT, positiveBtnText)
            args.putString(KEY_NEGATIVE_BTN_TEXT, negativeBtnText)
            fragment.arguments = args
            fragment.onSimpleDialogListener = listener
            return fragment
        }
    }

    var onSimpleDialogListener: OnSimpleDialogListener? = null

    override fun getLayoutId(): Int {
        return R.layout.fragment_simple_confirm_dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val message = arguments?.getString(KEY_MESSAGE)
        val positiveBtnText = arguments?.getString(KEY_POSITIVE_BTN_TEXT)
        val negativeBtnText = arguments?.getString(KEY_NEGATIVE_BTN_TEXT)

        tv_message.text = message
        btn_confirm.text = positiveBtnText
        btn_cancel.text = negativeBtnText

        btn_confirm.setOnClickListener {
            dismiss()
            onSimpleDialogListener?.onConfirm()
        }

        btn_cancel.setOnClickListener {
            dismiss()
            onSimpleDialogListener?.onCancle()
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun setupObservers() {
        Timber.d("${SimpleConfirmDialogFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${SimpleConfirmDialogFragment::class.java.simpleName}_setupListeners")
    }
}
