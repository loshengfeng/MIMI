package com.dabenxiang.mimi.view.forgetpassword

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ToastUtils
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.error.NOT_FOUND
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import kotlinx.android.synthetic.main.fragment_forget_password.*
import kotlinx.android.synthetic.main.item_login.*

class ForgetPasswordFragment : BaseFragment() {

    private val viewModel: ForgetPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_forget_password
    }

    override val bottomNavigationVisibility = View.GONE

    override fun setupObservers() {
        viewModel.accountError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                tv_account_error.visibility = View.INVISIBLE
            } else {
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
                R.id.btn_send -> viewModel.doValidateAndSubmit(tv_call_prefix.text.toString())
            }
        }.also {
            btn_cancel.setOnClickListener(it)
            btn_send.setOnClickListener(it)
        }

        tv_call_prefix.setOnClickListener {
            viewModel.changePrefixCount++
            if (viewModel.changePrefixCount == 10) {
                viewModel.changePrefixCount = 0

                if (tv_call_prefix.text == getString(R.string.login_mobile_call_prefix_taiwan)) {
                    tv_call_prefix.text = getString(R.string.login_mobile_call_prefix_china)
                    ToastUtils.showShort("Change to +86")
                } else {
                    tv_call_prefix.text = getString(R.string.login_mobile_call_prefix_taiwan)
                    ToastUtils.showShort("Change to +886")
                }
            }

            if (viewModel.timer == null) viewModel.startTimer()
        }
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.email.bindingEditText = edit_email
        viewModel.mobile.bindingEditText = edit_mobile
    }

    override fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        progressHUD?.dismiss()

        when (errorHandler.httpExceptionItem.errorItem.code) {
            NOT_FOUND -> {
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