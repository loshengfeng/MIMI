package com.dabenxiang.mimi.view.forgetpassword

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_dialog_login.*
import kotlinx.android.synthetic.main.fragment_forget_password.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ForgetPasswordFragment : BaseFragment() {

    private val viewModel by viewModel<ForgetPasswordViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_forget_password
    }

    override fun setupObservers() {
        Timber.d("${ForgetPasswordFragment::class.java.simpleName}_setupObservers")
        viewModel.accountError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tvAccountError.visibility = View.INVISIBLE
            } else {
                tvAccountError.text = getString(it)
                tvAccountError.visibility = View.VISIBLE
            }
        })

        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tvEmailError.visibility = View.INVISIBLE
            } else {
                tvEmailError.text = getString(it)
                tvEmailError.visibility = View.VISIBLE
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
                R.id.btnBack, R.id.btnCancel -> Navigation.findNavController(view!!).navigateUp()
                R.id.btnSend -> {
                    GeneralUtils.showToast(context!!, "btnSend")
                    viewModel.doValidateAndSubmit(
                        edtAccount.text.toString(),
                        edtEmail.text.toString()
                    )
                }
            }
        }.also {
            btnBack.setOnClickListener(it)
            btnCancel.setOnClickListener(it)
            btnSend.setOnClickListener(it)
        }
    }
}