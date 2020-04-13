package com.dabenxiang.mimi.view.forgetpassword

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_forget_password.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ForgetPasswordFragment : BaseFragment<ForgetPasswordViewModel>() {
    private val viewModel by viewModel<ForgetPasswordViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edit_account.setText("jeff7788")
        edit_email.setText("jeff@silkrode.com.tw")
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_forget_password
    }

    override fun fetchViewModel(): ForgetPasswordViewModel? {
        return viewModel
    }

    override fun setupObservers() {
        Timber.d("${ForgetPasswordFragment::class.java.simpleName}_setupObservers")
        viewModel.accountError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tv_account_error.visibility = View.INVISIBLE
            } else {
                tv_account_error.text = getString(it)
                tv_account_error.visibility = View.VISIBLE
            }
            if (it == null) {
                edit_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_account_error.visibility = View.INVISIBLE
            } else {
                edit_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_account_error.text = getString(it)
                tv_account_error.visibility = View.VISIBLE
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

        viewModel.result.observe(viewLifecycleOwner, Observer {
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
        Timber.d("${ForgetPasswordFragment::class.java.simpleName}_setupListeners")
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back, R.id.btn_cancel -> Navigation.findNavController(view!!).navigateUp()
                R.id.btn_send -> {
                    viewModel.doValidateAndSubmit(
                        edit_account.text.toString(),
                        edit_email.text.toString()
                    )
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_cancel.setOnClickListener(it)
            btn_send.setOnClickListener(it)
        }

        /*cb_show_pw.setOnCheckedChangeListener { _, isChecked ->
            edit_pw.transformationMethod = when {
                isChecked -> HideReturnsTransformationMethod.getInstance()
                else -> PasswordTransformationMethod.getInstance()
            }
        }*/
    }
}