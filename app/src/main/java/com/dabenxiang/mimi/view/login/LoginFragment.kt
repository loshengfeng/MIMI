package com.dabenxiang.mimi.view.login

import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.MIMI_INVITE_CODE
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.error.*
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*
import timber.log.Timber

class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by viewModels()

    companion object {
        const val KEY_TYPE = "TYPE"
        const val TYPE_REGISTER = 0
        const val TYPE_LOGIN = 1

        fun createBundle(type: Int): Bundle {
            return Bundle().also { it.putInt(KEY_TYPE, type) }
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        initSettings()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isLogin()) {
            navigateTo(NavigateItem.Up)
        }

        setCopyText()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    override fun setupObservers() {
        viewModel.accountError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_account_error.visibility = View.INVISIBLE
            } else {
                edit_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_account_error.text = it
                tv_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.mobileError.observe(viewLifecycleOwner, {
            if (it == "") {
                layout_mobile.setBackgroundResource(R.drawable.layout_rectangle)
                tv_mobile_error.visibility = View.INVISIBLE
            } else {
                layout_mobile.setBackgroundResource(R.drawable.layout_rectangle_error)
                tv_mobile_error.text = it
                tv_mobile_error.visibility = View.VISIBLE
            }
        })

        viewModel.validateCodeError.observe(viewLifecycleOwner, {
            if (it == "") {
                if (viewModel.clickType == TYPE_REGISTER) {
                    layout_verification_code.setBackgroundResource(R.drawable.layout_rectangle)
                    tv_validate_code_error.visibility = View.INVISIBLE
                } else {
                    layout_login_verification_code.setBackgroundResource(R.drawable.layout_rectangle)
                    tv_login_validate_code_error.visibility = View.INVISIBLE
                }
            } else {
                if (viewModel.clickType == TYPE_REGISTER) {
                    layout_verification_code.setBackgroundResource(R.drawable.layout_rectangle_error)
                    tv_validate_code_error.text = it
                    tv_validate_code_error.visibility = View.VISIBLE
                } else {
                    layout_login_verification_code.setBackgroundResource(R.drawable.layout_rectangle_error)
                    tv_login_validate_code_error.text = it
                    tv_login_validate_code_error.visibility = View.VISIBLE
                }
            }
        })

        viewModel.registerPasswordError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_register_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_pw_error.visibility = View.INVISIBLE
            } else {
                edit_register_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_pw_error.text = it
                tv_register_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.confirmPasswordError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_register_confirm_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_confirm_pw_error.visibility = View.INVISIBLE
            } else {
                edit_register_confirm_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_confirm_pw_error.text = it
                tv_register_confirm_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginAccountError.observe(viewLifecycleOwner, {
            if (it == "") {
                layout_login.setBackgroundResource(R.drawable.layout_rectangle)
                tv_login_account_error.visibility = View.INVISIBLE
            } else {
                layout_login.setBackgroundResource(R.drawable.layout_rectangle_error)
                tv_login_account_error.text = it
                tv_login_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginPasswordError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_login_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_login_pw_error.visibility = View.INVISIBLE
            } else {
                edit_login_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_login_pw_error.text = it
                tv_login_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginVerificationCodeError.observe(viewLifecycleOwner, {
            if (it == "") {
                layout_login_verification_code.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_login_validate_code_error.visibility = View.INVISIBLE
            } else {
                layout_login_verification_code.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_login_validate_code_error.text = it
                tv_login_validate_code_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerResult.observe(viewLifecycleOwner, {
            when (it) {
                is Empty -> {
                    viewModel.mobile.value?.let { mobile ->
                        viewModel.verificationCode.value?.let { code ->
                            viewModel.doLogin((tv_call_prefix.text.toString() + mobile), code = code)
                        }

                    }
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.loginResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    Timber.i("loginResult $it")
                    progressHUD?.dismiss()
                    mainViewModel?.startMQTT()
                    navigateTo(NavigateItem.Up)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.mobile.observe(viewLifecycleOwner, {
            it?.let {
                val callPrefix = tv_call_prefix.text.toString()
                if (callPrefix == getString(R.string.login_mobile_call_prefix_taiwan) && it.length == 9) {
                    validateMobile(it, tv_get_code, tv_call_prefix)
                } else if (callPrefix == getString(R.string.login_mobile_call_prefix_china) && it.length == 11) {
                    validateMobile(it, tv_get_code, tv_call_prefix)
                } else {
                    viewModel.onResetMobileError()
                    tv_get_code.isEnabled = false
                    tv_get_code.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_black_1_30))
                }
            }
        })

        viewModel.loginAccount.observe(viewLifecycleOwner, {
            it?.let {
                val callPrefix = tv_login_call_prefix.text.toString()
                if (callPrefix == getString(R.string.login_mobile_call_prefix_taiwan) && it.length == 9) {
                    validateMobile(it, tv_get_login_code, tv_login_call_prefix)
                } else if (callPrefix == getString(R.string.login_mobile_call_prefix_china) && it.length == 11) {
                    validateMobile(it, tv_get_login_code, tv_login_call_prefix)
                } else {
                    viewModel.onResetMobileError()
                    tv_get_login_code.isEnabled = false
                    tv_get_login_code.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_black_1_30))
                }
            }
        })

        viewModel.validateMessageResult.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    countDownTimer.start()
                    loginCountDownTimer.start()
                    GeneralUtils.showToast(requireContext(), getString(R.string.send_msg))
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.invitedCodeError.observe(viewLifecycleOwner, {
            if (it == "") {
                edit_invite_code.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_invite_code_error.visibility = View.INVISIBLE
            } else {
                edit_invite_code.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_invite_code_error.text = it
                tv_invite_code_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerExistResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Empty -> {
                    viewModel.onMobileError(getString(R.string.error_mobile_duplicate))
                }
                is Error -> {
                    onApiError(it.throwable)
                }
            }
        })

        viewModel.loginExistResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD.show()
                is Loaded -> progressHUD.dismiss()
                is Empty -> {
                    viewModel.callValidateMessage(tv_login_call_prefix.text.toString(), viewModel.loginAccount.value
                        ?: "")                }
                is Error -> {
                    onApiError(it.throwable)
                }
            }
        })

        viewModel.loginMobileErrorResult.observe(viewLifecycleOwner, Observer {
            tv_login_account_error.text = it
            tv_login_account_error.visibility = View.VISIBLE
        })
    }

    private fun validateMobile(mobile: String, getCodeView: TextView, prefixView: TextView) {
        val errMsg = viewModel.isValidateMobile(mobile, prefixView.text.toString())
        if (errMsg.isNotBlank()) {
            getCodeView.isEnabled = false
            getCodeView.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_black_1_30))
            viewModel.onMobileError(errMsg)
        } else {
            getCodeView.isEnabled = true
            getCodeView.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_black_1))
        }
    }

    private val countDownTimer =  object : CountDownTimer( 60000, 1000) {
        override fun onFinish() {
            tv_get_code?.isEnabled = true
            tv_get_code?.text = getString(R.string.login_get_code)
        }

        override fun onTick(p0: Long) {
            tv_get_code?.isEnabled = false
            tv_get_code?.text = String.format(getString(R.string.send_code_count_down), p0 / 1000)
        }
    }

    private val loginCountDownTimer = object : CountDownTimer(60000, 1000) {
        override fun onFinish() {
            tv_get_login_code?.isEnabled = true
            tv_get_login_code?.text = getString(R.string.login_get_code)
        }

        override fun onTick(p0: Long) {
            tv_get_login_code?.isEnabled = false
            tv_get_login_code?.text = String.format(getString(R.string.send_code_count_down), p0 / 1000)
        }
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
            if (System.currentTimeMillis() - viewModel.clickTime > 500L) {
                viewModel.clickTime = System.currentTimeMillis()
            } else {
                return@OnClickListener
            }

            when (buttonView.id) {
                R.id.btn_register_cancel, R.id.btn_login_cancel, R.id.btnClose -> navigateTo(NavigateItem.Up)

                R.id.btn_register -> {
                    viewModel.doRegisterValidateAndSubmit(tv_call_prefix.text.toString())
                }
//                R.id.btn_forget -> navigateTo(NavigateItem.Destination(R.id.action_loginFragment_to_forgetPasswordFragment))
                R.id.btn_login -> viewModel.doLoginValidateAndSubmit(tv_login_call_prefix.text.toString(), group_pwd.isVisible)
            }
        }.also {
            btnClose.setOnClickListener(it)
            btn_register_cancel.setOnClickListener(it)
            btn_login_cancel.setOnClickListener(it)
            btn_register.setOnClickListener(it)
//            btn_forget.setOnClickListener(it)
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
            viewModel.accountManager.keepAccount = isChecked
        }

        tv_get_code.setOnClickListener {
            viewModel.callRegisterIsMemberExist(tv_call_prefix.text.toString(), viewModel.mobile.value ?: "")
        }

        tv_get_login_code.setOnClickListener {
            viewModel.callLoginIsMemberExist(tv_login_call_prefix.text.toString(), viewModel.loginAccount.value
                ?: "")
        }

//        layout_login_verification_code.setOnClickListener {
//            viewModel.changePWDCount++
//            if (viewModel.changePWDCount == 10) {
//                viewModel.changePWDCount = 0
//                group_pwd.visibility = View.VISIBLE
//
//            }
//        }

        tv_call_prefix.setOnClickListener {
            viewModel.changePrefixCount++
            if (viewModel.changePrefixCount == 10) {
                viewModel.changePrefixCount = 0

                if (tv_call_prefix.text == getString(R.string.login_mobile_call_prefix_taiwan)) {
                    tv_call_prefix.text = getString(R.string.login_mobile_call_prefix_china)
                    GeneralUtils.showToast(requireContext(), "Change to +86")
                    edit_mobile.filters = arrayOf<InputFilter>(LengthFilter(11))
                } else {
                    tv_call_prefix.text = getString(R.string.login_mobile_call_prefix_taiwan)
                    GeneralUtils.showToast(requireContext(), "Change to +886")
                    edit_mobile.filters = arrayOf<InputFilter>(LengthFilter(9))
                }
            }

            if (viewModel.timer == null) viewModel.startTimer()
        }

        tv_login_call_prefix.setOnClickListener {
            viewModel.changePrefixCount++
            if (viewModel.changePrefixCount == 10) {
                viewModel.changePrefixCount = 0

                if (tv_login_call_prefix.text == getString(R.string.login_mobile_call_prefix_taiwan)) {
                    tv_login_call_prefix.text = getString(R.string.login_mobile_call_prefix_china)
                    GeneralUtils.showToast(requireContext(), "Change to +86")
                    edit_login_account.filters = arrayOf<InputFilter>(LengthFilter(11))
                } else {
                    tv_login_call_prefix.text = getString(R.string.login_mobile_call_prefix_taiwan)
                    GeneralUtils.showToast(requireContext(), "Change to +886")
                    edit_login_account.filters = arrayOf<InputFilter>(LengthFilter(9))
                }
            }

            if (viewModel.timer == null) viewModel.startTimer()
        }

        secret_layout.setOnClickListener {
            viewModel.mobileValidCount++
            if (viewModel.mobileValidCount == 10) {
                viewModel.mobileValidCount = 0
                viewModel.isNeedValidMobile = !viewModel.isNeedValidMobile
                GeneralUtils.showToast(requireContext(), "is need valid mobile : " + viewModel.isNeedValidMobile)
            }
        }
    }

    override fun initSettings() {
        useAdultTheme(false)
        viewModel.mobile.bindingEditText = edit_mobile
        viewModel.verificationCode.bindingEditText = edit_verification_code
        viewModel.inviteCode.bindingEditText = edit_invite_code
        viewModel.account.bindingEditText = edit_account
        viewModel.registerPw.bindingEditText = edit_register_pw
        viewModel.confirmPw.bindingEditText = edit_register_confirm_pw
        viewModel.loginAccount.bindingEditText = edit_login_account
        viewModel.loginVerificationCode.bindingEditText = edit_login_verification_code
        viewModel.loginPw.bindingEditText = edit_login_pw

//        tl_type.selectTab(
//            this.arguments?.getInt(KEY_TYPE, TYPE_REGISTER)?.let { tl_type.getTabAt(it) })

        val keepAccount = viewModel.accountManager.keepAccount

        cb_keep_account.isChecked = keepAccount

        viewModel.loginAccount.value = when (keepAccount) {
            true -> {
                val profile = viewModel.accountManager.getProfile()
                if (profile.account.isBlank()) {
                    ""
                } else if (tv_login_call_prefix.text == "+86") {
                    profile.account.substring(3, profile.account.length)
                } else {
                    profile.account.substring(4, profile.account.length)
                }
            }
            false -> ""
        }

        tv_get_code.isEnabled = false
        tv_get_login_code.isEnabled = false
    }

    override fun navigateTo(item: NavigateItem) {
        findNavController().also { navController ->
            when (item) {
                NavigateItem.Up -> navController.navigateUp()
                is NavigateItem.PopBackStack -> navController.popBackStack(
                    item.fragmentId,
                    item.inclusive
                )
                is NavigateItem.Destination -> {
                    if (item.bundle == null) {
                        navController.navigate(item.action)
                    } else {
                        navController.navigate(item.action, item.bundle)
                    }
                }
            }
        }
    }

    override fun handleHttpError(errorHandler: ExceptionResult.HttpError) {
        when (errorHandler.httpExceptionItem.errorItem.code) {
            LOGIN_400000 -> {
                when (errorHandler.httpExceptionItem.errorItem.message){
                    "invalid referrerCode" -> viewModel.onInvitedCodeError(getString(R.string.invited_code_error_1))
                    "code is not exists", "invalid code" -> viewModel.validateCodeError(R.string.error_validation_code)
                    else -> showErrorMessageDialog(getString(R.string.error_validation))
                }
            }
            LOGIN_403001 -> showErrorMessageDialog(getString(R.string.error_username_or_password_incorrect))
            LOGIN_403002 -> showErrorMessageDialog(getString(R.string.error_account_disable))
            LOGIN_403004 -> showErrorMessageDialog(getString(R.string.error_validation))
            LOGIN_403006 -> {
                GeneralDialog.newInstance(
                    GeneralDialogData(
                        titleRes = R.string.desc_success,
                        message = getString(R.string.error_first_login),
                        messageIcon = R.drawable.ico_default_photo,
                        secondBtn = getString(R.string.btn_confirm),
                        secondBlock = { navigateTo(NavigateItem.Destination(R.id.action_loginFragment_to_changePasswordFragment)) }
                    )
                ).setCancel(false)
                    .show(requireActivity().supportFragmentManager)
            }
            LOGIN_404000 -> {
                if (viewModel.type == TYPE_REGISTER) {
                    viewModel.callValidateMessage(tv_call_prefix.text.toString(), viewModel.mobile.value ?: "")
                } else if (viewModel.type == TYPE_LOGIN) {
                    viewModel.onLoginMobileError(getString(R.string.error_mobile_not_exist))
                }
            }
            LOGIN_406000 -> {
                viewModel.validateCodeError(R.string.error_validation_code)
            }
            NOT_FOUND -> {
                showErrorMessageDialog(getString(R.string.error_validation))
//                viewModel.inviteCodeError()
            }
            SERVER_ERROR -> {
                showErrorMessageDialog(errorHandler.httpExceptionItem.errorItem.message
                        ?: "Server error")
            }
            LOGIN_409000 -> {
                countDownTimer.cancel()
                countDownTimer.onFinish()
                loginCountDownTimer.cancel()
                loginCountDownTimer.onFinish()
                viewModel.onMobileError(getString(R.string.error_mobile_duplicate))
                edit_verification_code.setText("")
                tv_login_validate_code_error.setText("")
            }
        }
    }

    private fun showErrorMessageDialog(message: String) {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.login_yet,
                message = message,
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_confirm)
            )
        ).show(requireActivity().supportFragmentManager)
    }

    private fun setCopyText() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        val clipDataItem = clipData?.getItemAt(0)
        val copyText = clipDataItem?.text.toString() ?: ""

        if (copyText.contains(MIMI_INVITE_CODE)) {
            edit_invite_code.isFocusable = false
            edit_invite_code.isClickable = false
            val startIndex = copyText.lastIndexOf(MIMI_INVITE_CODE) + MIMI_INVITE_CODE.length
            val inviteCode = copyText.substring(startIndex, copyText.length)
            viewModel.inviteCode.value = inviteCode
        }
    }
}