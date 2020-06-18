package com.dabenxiang.mimi.view.dialog.login

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import kotlinx.android.synthetic.main.fragment_dialog_login.*
import timber.log.Timber

class LoginDialogFragment : BaseDialogFragment() {

    companion object {
        const val KEY_TYPE = "KEY_TYPE"
        const val TYPE_VALIDATION = 0
        const val TYPE_RESET = 1
        const val TYPE_SUCCESS = 2
        fun newInstance(listener: OnLoginDialogListener? = null,
        type : Int = TYPE_VALIDATION): LoginDialogFragment {
            val fragment = LoginDialogFragment()
            val args = Bundle()
            args.putInt(KEY_TYPE, type)
            fragment.arguments = args
            fragment.onLoginDialogListener = listener
            return fragment
        }
    }

    var onLoginDialogListener: OnLoginDialogListener? = null

//    private val viewModel by viewModel<LoginDialogViewModel>()

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dialog_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.loginDialogStyle)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val type = arguments?.getInt(KEY_TYPE)
        refreshUI(type)
    }

    override fun setupObservers() {
        Timber.d("${LoginDialogFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${LoginDialogFragment::class.java.simpleName}_setupListeners")
        btnConfirm?.setOnClickListener {
            onLoginDialogListener?.onConfirm()
        }

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btnConfirm -> onLoginDialogListener?.onConfirm()
            }
        }.also {
            btnConfirm.setOnClickListener(it)
        }
    }

    private fun refreshUI(type: Int?) {
        when(type) {
            TYPE_VALIDATION -> {
                Timber.d("${LoginDialogFragment::class.java.simpleName}_TYPE_VALIDATION")
                ivTitle.setBackgroundResource(R.drawable.verification_mail)
                tv_title.text = getString(R.string.receive_mail)
                ivIcon.setBackgroundResource(R.drawable.ico_email)
                tv_desc.text = getString(R.string.desc_register)
            }
            TYPE_RESET -> {
                Timber.d("${LoginDialogFragment::class.java.simpleName}_TYPE_RESET")
                ivTitle.setBackgroundResource(R.drawable.verification_mail)
                tv_title.text = getString(R.string.receive_mail)
                ivIcon.setBackgroundResource(R.drawable.ico_email)
                tv_desc.text = getString(R.string.desc_email, "xxx@silkrode.com.tw")
            }
            TYPE_SUCCESS -> {
                Timber.d("${LoginDialogFragment::class.java.simpleName}_TYPE_SUCCESS")
                ivTitle.setBackgroundResource(R.drawable.img_login_success)
                tv_title.text = getString(R.string.desc_success)
                ivIcon.setBackgroundResource(R.drawable.ico_default_photo)
                tv_desc.text = "好大一棵洋梨"
            }
        }
    }

}
