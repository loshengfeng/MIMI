package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_general.*
import java.io.Serializable

class GeneralDialogData(
    @DrawableRes var titleIcon: Int = R.drawable.ic_verification_mail,
    @StringRes var titleRes: Int,
    @DrawableRes var messageIcon: Int,
    var message: String,
    var firstBtn: String,
    var firstBlock: (() -> Unit)? = null,
    var secondBtn: String,
    var secondBlock: (() -> Unit)? = null
) : Serializable

class GeneralDialog : BaseDialogFragment() {

    companion object {
        private const val KEY_DATA = "data"

        fun newInstance(data: GeneralDialogData): GeneralDialog {
            val fragment = GeneralDialog()
            val args = Bundle()
            args.putSerializable(KEY_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireArguments().getSerializable(KEY_DATA) as? GeneralDialogData)?.also { data ->
            iv_title.setImageResource(data.titleIcon)
            tv_title.setText(data.titleRes)
            iv_message.setImageResource(data.messageIcon)
            tv_message.text = data.message

            btn_first.visibility =
                if (data.firstBtn.isBlank()) {
                    View.GONE
                } else {
                    btn_first.text = data.firstBtn
                    btn_first.setOnClickListener(btnOnClickListener)
                    View.VISIBLE
                }

            btn_second.visibility =
                if (data.secondBtn.isBlank()) {
                    View.GONE
                } else {
                    btn_second.text = data.secondBtn
                    btn_second.setOnClickListener(btnOnClickListener)
                    View.VISIBLE
                }
        }
    }

    private val btnOnClickListener = View.OnClickListener {
        val data = requireArguments().getSerializable(KEY_DATA) as? GeneralDialogData

        when (it) {
            btn_first -> {
                if (data?.firstBlock != null) {
                    data.firstBlock!!()
                }
                dismiss()
            }
            btn_second -> {
                if (data?.secondBlock != null) {
                    data.secondBlock!!()
                }
                dismiss()
            }
        }
    }

    override fun isFullLayout(): Boolean {
        return false
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_general
    }
}