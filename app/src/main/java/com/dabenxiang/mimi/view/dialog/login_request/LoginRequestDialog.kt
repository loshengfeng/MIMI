package com.dabenxiang.mimi.view.dialog.login_request

import android.os.Bundle
import android.view.View
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseDialogFragment
import com.dabenxiang.mimi.view.listener.OnLoginRequestDialogListener
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import timber.log.Timber

class LoginRequestDialog : BaseDialogFragment()  {

    var dialogListener: OnLoginRequestDialogListener? = null
    companion object {
        fun newInstance(listener: OnLoginRequestDialogListener? = null): LoginRequestDialog {
            val fragment = LoginRequestDialog()
            fragment.dialogListener = listener
            return fragment
        }
    }

    override fun isFullLayout(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.item_personal_is_not_login
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_close.visibility =View.VISIBLE

        tv_login.setOnClickListener {
            Timber.i("tv_login")
            this.dismiss()
            dialogListener?.onLogin()
        }
        tv_register.setOnClickListener {
            Timber.i("tv_register")
            this.dismiss()
            dialogListener?.onRegister()
        }
        iv_close.setOnClickListener {
            this.dismiss()
            dialogListener?.onCancel()
        }
    }

}