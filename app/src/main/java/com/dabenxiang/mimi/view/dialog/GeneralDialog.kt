package com.dabenxiang.mimi.view.dialog

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_general.*
import java.io.Serializable
import java.util.*

class GeneralDialogData(
    @DrawableRes val titleIcon: Int = R.drawable.verification_mail,
    @StringRes val titleRes: Int? = null,
    val titleString: String = "",
    @DrawableRes val messageIcon: Int,
    val message: String = "",
    val isHtml: Boolean = false,
    val firstBtn: String = "",
    val firstBlock: (() -> Unit)? = null,
    val secondBtn: String = "",
    val secondBlock: (() -> Unit)? = null,
    val closeBlock: (() -> Unit)? = null
) : Serializable

fun GeneralDialog.show(manager: FragmentManager): GeneralDialog {
    this.show(manager, "${Calendar.getInstance().timeInMillis}")
    return this
}

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

    fun setCancel(isCancel: Boolean): GeneralDialog {
        isCancelable = isCancel
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireArguments().getSerializable(KEY_DATA) as? GeneralDialogData)?.also { data ->
            iv_title.setImageResource(data.titleIcon)
            if (data.titleRes != null) {
                tv_title.setText(data.titleRes)
            } else {
                tv_title.text = data.titleString
            }

            iv_message.setImageResource(data.messageIcon)

            if (data.isHtml)
                tv_message.text = Html.fromHtml(data.message, Html.FROM_HTML_MODE_COMPACT)
            else
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

            btn_close.visibility =
                if (data.closeBlock == null) {
                    View.GONE
                } else {
                    btn_close.setOnClickListener(btnOnClickListener)
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
            }
            btn_second -> {
                if (data?.secondBlock != null) {
                    data.secondBlock!!()
                }
            }
            btn_close -> {
                if (data?.closeBlock != null) {
                    data.closeBlock!!()
                }
            }
        }

        dismiss()
    }

    override fun isFullLayout(): Boolean { return false }

    override fun getLayoutId(): Int { return R.layout.fragment_dialog_general }
}