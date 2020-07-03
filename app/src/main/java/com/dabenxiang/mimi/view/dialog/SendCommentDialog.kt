package com.dabenxiang.mimi.view.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_send_comment.*
import java.io.Serializable

class SendCommentDialog(private val listener: SendCommentDialogListener) : BaseDialogFragment() {

    interface SendCommentDialogListener {
        fun onSuccess(replyId: Long?, content: String)
    }

    companion object {
        private const val KEY_DATA = "data"

        fun newInstance(
            isAdult: Boolean,
            replyId: Long? = null,
            replyName: String? = null,
            listener: SendCommentDialogListener
        ): SendCommentDialog {
            val fragment = SendCommentDialog(listener)
            val args = Bundle()
            args.putSerializable(KEY_DATA, SendCommentData(isAdult, replyId, replyName))
            fragment.arguments = args
            return fragment
        }
    }

    private class SendCommentData(
        val isAdult: Boolean,
        val replyId: Long?,
        val replyName: String?
    ) : Serializable

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_send_comment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = arguments?.getSerializable(KEY_DATA) as SendCommentData

        btn_send.setOnClickListener {
            closeKeyboard()
            listener.onSuccess(data.replyId, edit_message.text.toString())
        }

        val textColor = if (data.isAdult) {
            R.color.color_white_1
        } else {
            R.color.color_black_1
        }.let {
            requireContext().getColor(it)
        }

        if (data.replyName == null) {
            tv_replay_name.visibility = View.GONE
        } else {
            tv_replay_name.setTextColor(textColor)
            tv_replay_name.text = "@${data.replyName}"
            tv_replay_name.visibility = View.VISIBLE
        }

        edit_message.setTextColor(textColor)

        bg_layout.background =
            if (data.isAdult) {
                R.color.adult_color_status_bar
            } else {
                R.color.normal_color_status_bar
            }.let {
                requireActivity().getDrawable(it)
            }

        bg_bar.setImageResource(
            if (data.isAdult) {
                R.drawable.bg_black_1_30_radius_18
            } else {
                R.drawable.bg_white_1_65625_border_gray_11_radius_18
            }
        )
    }

    override fun onStart() {
        super.onStart()

        showKeyboard()
    }

    private fun showKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun closeKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}