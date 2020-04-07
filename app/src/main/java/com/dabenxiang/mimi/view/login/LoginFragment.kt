package com.dabenxiang.mimi.view.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.dialog.login.LoginDialogFragment
import com.dabenxiang.mimi.view.dialog.login.OnLoginDialogListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginFragment : BaseFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    private var dialog: LoginDialogFragment? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun setupObservers() {
        Timber.d("${LoginFragment::class.java.simpleName}_setupObservers")
        viewModel.accountError.observe(viewLifecycleOwner, Observer {

            // todo
            /*val type = it.keys.indexOf(0)

            Timber.d("${LoginFragment::class.java.simpleName}_type: $type")

            val result = it[type]

            Timber.d("${LoginFragment::class.java.simpleName}_result: $result")

            when(type) {
                TYPE_REGISTER -> {
                    if(result == 0) {
                        tvRegisterAccError.visibility = View.INVISIBLE
                    } else {
                        tvRegisterAccError.text = result?.let { it1 -> getString(it1) }
                        tvLoginAccError.visibility = View.VISIBLE
                    }
                }
                TYPE_LOGIN -> {
                    if(result == 0) {
                        tvLoginAccError.visibility = View.INVISIBLE
                    } else {
                        tvLoginAccError.text = result?.let { it1 -> getString(it1) }
                        tvLoginAccError.visibility = View.VISIBLE
                    }
                }
            }*/
        })

        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tv_email_error.visibility = View.INVISIBLE
            } else {
                tv_email_error.text = getString(it)
                tv_email_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tv_register_account_error.visibility = View.INVISIBLE
            } else {
                tv_register_account_error.text = getString(it)
                tv_register_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.confirmPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tv_confirm_pw_error.visibility = View.INVISIBLE
            } else {
                tv_confirm_pw_error.text = getString(it)
                tv_confirm_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tv_login_pw_error.visibility = View.INVISIBLE
            } else {
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
        Timber.d("${LoginFragment::class.java.simpleName}_setupListeners")

        typeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
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
                    GeneralUtils.showToast(context!!, "Register")
                    viewModel.doRegisterValidateAndSubmit(
                        edit_register_account.text.toString(),
                        edit_email.text.toString(),
                        edit_register_pw.text.toString(),
                        edit_confirm_pw.text.toString()
                    )
                }
                R.id.btn_forget -> {
                    GeneralUtils.showToast(context!!, "Forget")
                    Navigation.findNavController(view!!).navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
                }
                R.id.btn_login -> {
                    GeneralUtils.showToast(context!!, "btnLogin")
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

        cb_keep_account.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("${LoginFragment::class.java.simpleName}_isChecked = $isChecked")
            GeneralUtils.showToast(context!!, "Remember")
        }
    }

    private val onLoginDialogListener = object : OnLoginDialogListener {
        override fun onConfirm() {
            dialog?.dismiss()
        }
    }
}