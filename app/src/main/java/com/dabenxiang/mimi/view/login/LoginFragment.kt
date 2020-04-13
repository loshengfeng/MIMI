package com.dabenxiang.mimi.view.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.login.LoginDialogFragment
import com.dabenxiang.mimi.view.dialog.login.OnLoginDialogListener
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginFragment : BaseFragment<LoginViewModel>() {
    private val viewModel by viewModel<LoginViewModel>()

    private val onLoginDialogListener = object : OnLoginDialogListener {
        override fun onConfirm() {
            dialog?.dismiss()
        }
    }

    companion object {
        private const val KEY_TYPE = "TYPE"
        const val TYPE_REGISTER = 0
        const val TYPE_LOGIN = 1

        fun createBundle(type: Int): Bundle {
            return Bundle().also {
                it.putSerializable(KEY_TYPE, type)
            }
        }

    }

    private var dialog: LoginDialogFragment? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tl_type.selectTab(this.arguments?.getInt(KEY_TYPE, TYPE_REGISTER)?.let { tl_type.getTabAt(it) })

        edit_register_account.setText("jeff7788")
        edit_login_account.setText("jeff7788")
        edit_email.setText("jeff@silkrode.com.tw")
        edit_register_pw.setText("12345678")
        edit_register_confirm_pw.setText("12345678")
        edit_login_pw.setText("12345678")

        // todo: for API response testing
        /*Handler().postDelayed({
            dialog = LoginDialogFragment.newInstance(onLoginDialogListener,
                LoginDialogFragment.TYPE_SUCCESS
            )
            dialog?.show(activity!!.supportFragmentManager, LoginDialogFragment::class.java.simpleName)
        },1500)*/
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    override fun fetchViewModel(): LoginViewModel? {
        return viewModel
    }

    override fun setupObservers() {
        viewModel.registerAccountError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_register_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_account_error.visibility = View.INVISIBLE
            } else {
                edit_register_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_account_error.text = getString(it)
                tv_register_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginAccountError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_login_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_login_account_error.visibility = View.INVISIBLE
            } else {
                edit_login_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_login_account_error.text = getString(it)
                tv_login_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_email.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_email_error.visibility = View.INVISIBLE
            } else {
                edit_email.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_email_error.text = getString(it)
                tv_email_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_register_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_pw_error.visibility = View.INVISIBLE
            } else {
                edit_register_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_pw_error.text = getString(it)
                tv_register_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.confirmPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_register_confirm_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_confirm_pw_error.visibility = View.INVISIBLE
            } else {
                edit_register_confirm_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_confirm_pw_error.text = getString(it)
                tv_register_confirm_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_login_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_login_pw_error.visibility = View.INVISIBLE
            } else {
                edit_login_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_login_pw_error.text = getString(it)
                tv_login_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerResult.observe(viewLifecycleOwner, Observer {
            // todo
            /*when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> onLoginDialogListener?.onConfirm()
                is Error -> {
                    Timber.e("Error: $it")
                    when (val errorHandler =
                        it.throwable.handleException { ex -> viewModel.processException(ex) }) {
                        is ExceptionResult.HttpError -> {
                            when (errorHandler.httpExceptionData.errorItem.code) {
                                ErrorCode.WRONG_NAME, ErrorCode.WRONG_PW -> {
                                    showErrorDialog(getString(R.string.username_or_password_incorrect))
                                }
                                else -> {
                                    showHttpErrorDialog(HttpErrorMsgType.CHECK_NETWORK)
                                    showHttpErrorToast(errorHandler.httpExceptionData.httpExceptionClone)
                                }
                            }
                        }
                        is ExceptionResult.Crash -> {
                            if (errorHandler.throwable is UnknownHostException) {
                                showHttpErrorDialog(HttpErrorMsgType.CHECK_NETWORK)
                            } else {
                                GeneralUtils.showToast(context!!, "${errorHandler.throwable}")
                            }
                        }
                    }
                }
            }*/
        })

        viewModel.loginResult.observe(viewLifecycleOwner, Observer {
            // todo
            /*when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> onLoginDialogListener?.onConfirm()
                is Error -> {
                    Timber.e("Error: $it")
                    when (val errorHandler =
                        it.throwable.handleException { ex -> viewModel.processException(ex) }) {
                        is ExceptionResult.HttpError -> {
                            when (errorHandler.httpExceptionData.errorItem.code) {
                                ErrorCode.WRONG_NAME, ErrorCode.WRONG_PW -> {
                                    showErrorDialog(getString(R.string.username_or_password_incorrect))
                                }
                                else -> {
                                    showHttpErrorDialog(HttpErrorMsgType.CHECK_NETWORK)
                                    showHttpErrorToast(errorHandler.httpExceptionData.httpExceptionClone)
                                }
                            }
                        }
                        is ExceptionResult.Crash -> {
                            if (errorHandler.throwable is UnknownHostException) {
                                showHttpErrorDialog(HttpErrorMsgType.CHECK_NETWORK)
                            } else {
                                GeneralUtils.showToast(context!!, "${errorHandler.throwable}")
                            }
                        }
                    }
                }
            }*/
        })
    }

    override fun setupListeners() {
        tl_type.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.type = tab.position
                when (tab.position) {
                    0 -> {
                        item_register.visibility = View.VISIBLE
                        item_login.visibility = View.GONE
                    }
                    1 -> {
                        item_register.visibility = View.GONE
                        item_login.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btnClose, R.id.btn_register_cancel, R.id.btn_login_cancel -> Navigation.findNavController(view!!).navigateUp()
                R.id.btn_register -> {
                    viewModel.doRegisterValidateAndSubmit(
                        edit_register_account.text.toString(),
                        edit_email.text.toString(),
                        edit_register_pw.text.toString(),
                        edit_register_confirm_pw.text.toString()
                    )
                }
                R.id.btn_forget -> {
                    viewModel.toastData.value = "Forget"
                    Navigation.findNavController(view!!).navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
                }
                R.id.btn_login -> {
                    viewModel.doLoginValidateAndSubmit(
                        edit_login_account.text.toString(),
                        edit_login_pw.text.toString()
                    )
                }
            }
        }.also {
            btnClose.setOnClickListener(it)
            btn_register_cancel.setOnClickListener(it)
            btn_login_cancel.setOnClickListener(it)
            btn_register.setOnClickListener(it)
            btn_forget.setOnClickListener(it)
            btn_login.setOnClickListener(it)
        }

        cb_show_register_pw.setOnCheckedChangeListener { _, isChecked ->
            edit_register_pw.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
            edit_register_pw.setSelection(edit_register_pw.length())
        }

        cb_show_register_confirm_pw.setOnCheckedChangeListener { _, isChecked ->
            edit_register_confirm_pw.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
            edit_register_confirm_pw.setSelection(edit_register_confirm_pw.length())
        }

        cb_show_login_pw.setOnCheckedChangeListener { _, isChecked ->
            edit_login_pw.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
            edit_login_pw.setSelection(edit_login_pw.length())
        }

        cb_keep_account.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toastData.value = "Remember: $isChecked"
            // todo: save Account to SharePreference...
        }
    }
}