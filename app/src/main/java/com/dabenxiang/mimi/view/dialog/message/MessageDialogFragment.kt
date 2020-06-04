package com.dabenxiang.mimi.view.dialog.message

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_message.*
import java.io.Serializable

class MessageDialogFragment : BaseDialogFragment() {

    companion object {
        private const val KEY_CONTENT = "KEY_CONTENT"
        fun newInstance(content: Content): MessageDialogFragment {
            val fragment = MessageDialogFragment()
            val args = Bundle()
            args.putSerializable(KEY_CONTENT, content)
            fragment.arguments = args
            return fragment
        }
    }

    data class Content(val title: String? = null,
                       @DrawableRes val icon: Int? = null,
                       val message: String? = null,
                       val positiveBtnText: String? = null,
                       val negativeBtnText: String? = null,
                       val listener: OnMessageDialogListener? = null) : Serializable

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_message
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val content = arguments?.getSerializable(KEY_CONTENT) as Content

        if (content.title.isNullOrEmpty()) {
            textTitle.visibility = View.GONE
        } else {
            textTitle.visibility = View.VISIBLE
            textTitle.text = content.title
        }

        if (content.message.isNullOrEmpty()) {
            textMessage.visibility = View.GONE
        } else {
            textMessage.visibility = View.VISIBLE
            textMessage.text = content.message
            if (content.icon == null) {
                textMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0,0,0)
            } else {
                textMessage.setCompoundDrawablesWithIntrinsicBounds(0, content.icon,0,0)
            }
        }

        if (content.positiveBtnText.isNullOrEmpty()) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.visibility = View.VISIBLE
            btnPositive.text = content.positiveBtnText
            btnPositive.setOnClickListener {
                dismiss()
                content.listener?.onPositiveClick()
            }
        }

        if (content.negativeBtnText.isNullOrEmpty()) {
            btnNegative.visibility = View.GONE
        } else {
            btnNegative.visibility = View.VISIBLE
            btnNegative.text = content.negativeBtnText
            btnNegative.setOnClickListener {
                dismiss()
                content   .listener?.onNegativeClick()
            }
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }
}