package com.dabenxiang.mimi.view.forgetpassword

import android.os.Bundle
import android.text.InputFilter
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
        viewModel.mobileError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                layout_mobile.setBackgroundResource(R.drawable.layout_rectangle)
                tv_mobile_error.visibility = View.INVISIBLE
            } else {
                layout_mobile.setBackgroundResource(R.drawable.layout_rectangle_error)
                tv_mobile_error.text = it
                tv_mobile_error.visibility = View.VISIBLE
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
                            message = getString(R.string.desc_mobile, tv_call_prefix.text.toString() + viewModel.mobile.value),
                            messageIcon = R.drawable.ico_email,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = { navigateTo(NavigateItem.Up)},
                            isMessageIcon = false
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
                    edit_mobile.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(11))
                } else {
                    tv_call_prefix.text = getString(R.string.login_mobile_call_prefix_taiwan)
                    ToastUtils.showShort("Change to +886")
                    edit_mobile.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(9))
                }
            }

            if (viewModel.timer == null) viewModel.startTimer()
        }
    }

    override fun initSettings() {
        super.initSettings()
        viewModel.mobile.bindingEditText = edit_mobile
    }

    override fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        progressHUD?.dismiss()

        when (errorHandler.httpExceptionItem.errorItem.code) {
            NOT_FOUND -> {
                viewModel.mobileNotFoundError()
            }
        }
    }
}