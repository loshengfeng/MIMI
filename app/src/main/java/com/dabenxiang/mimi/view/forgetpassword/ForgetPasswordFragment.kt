package com.dabenxiang.mimi.view.forgetpassword

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ErrorCode
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import kotlinx.android.synthetic.main.fragment_forget_password.*
import kotlinx.android.synthetic.main.fragment_forget_password.edit_email
import kotlinx.android.synthetic.main.fragment_forget_password.tv_email_error
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgetPasswordFragment : BaseFragment<ForgetPasswordViewModel>() {
    private val viewModel by viewModel<ForgetPasswordViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_forget_password }

    override fun fetchViewModel(): ForgetPasswordViewModel? { return viewModel }

    override val bottomNavigationVisibility = View.GONE

    override fun setupObservers() {
        viewModel.accountError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_account_error.visibility = View.INVISIBLE
            } else {
                edit_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_account_error.text = it
                tv_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_email.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_email_error.visibility = View.INVISIBLE
            } else {
                edit_email.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_email_error.text = it
                tv_email_error.visibility = View.VISIBLE
            }
        })

        viewModel.result.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Empty -> {
                    progressHUD?.dismiss()
                    GeneralDialog.newInstance(
                        GeneralDialogData(
                            titleRes = R.string.reset_pw_success,
                            message = getString(R.string.desc_email, viewModel.email.value),
                            messageIcon = R.drawable.ico_email,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = { navigateTo(NavigateItem.Up) }
                        )
                    ).setCancel(false)
                        .show(requireActivity().supportFragmentManager)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()

            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btn_cancel -> navigateTo(NavigateItem.Up)
                R.id.btn_send -> viewModel.doValidateAndSubmit()
            }
        }.also {
            btn_cancel.setOnClickListener(it)
            btn_send.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.account.bindingEditText = edit_account
        viewModel.email.bindingEditText = edit_email
    }

    override fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        progressHUD?.dismiss()

        when (errorHandler.httpExceptionItem.errorItem.code) {
            ErrorCode.NOT_FOUND -> {
                GeneralDialog.newInstance(
                    GeneralDialogData(
                        titleRes = R.string.login_yet,
                        message = getString(R.string.desc_email_account_not_match),
                        messageIcon = R.drawable.ico_email,
                        secondBtn = getString(R.string.btn_confirm)
                    )
                ).show(requireActivity().supportFragmentManager)
            }
        }
    }
}