package com.dabenxiang.mimi.view.login

import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ToastUtils
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.error.*
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*

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
        requireActivity().onBackPressedDispatcher.addCallback { navigateTo(NavigateItem.Up) }
        initSettings()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isLogin()) {
            navigateTo(NavigateItem.Up)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    override fun setupObservers() {
        viewModel.friendlyNameError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_friendly_name.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_friendly_name_error.visibility = View.INVISIBLE
            } else {
                edit_friendly_name.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_friendly_name_error.text = it
                tv_friendly_name_error.visibility = View.VISIBLE
            }
        })

        viewModel.emailError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                tv_email_error.visibility = View.INVISIBLE
            } else {
                tv_email_error.text = it
                tv_email_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerAccountError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                tv_register_account_error.visibility = View.INVISIBLE
            } else {
                tv_register_account_error.text = it
                tv_register_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_register_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_pw_error.visibility = View.INVISIBLE
            } else {
                edit_register_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_pw_error.text = it
                tv_register_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.confirmPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_register_confirm_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_confirm_pw_error.visibility = View.INVISIBLE
            } else {
                edit_register_confirm_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_confirm_pw_error.text = it
                tv_register_confirm_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginAccountError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_login_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_login_account_error.visibility = View.INVISIBLE
            } else {
                edit_login_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_login_account_error.text = it
                tv_login_account_error.visibility = View.VISIBLE
            }
        })

        viewModel.loginPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_login_pw.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_login_pw_error.visibility = View.INVISIBLE
            } else {
                edit_login_pw.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_login_pw_error.text = it
                tv_login_pw_error.visibility = View.VISIBLE
            }
        })

        viewModel.registerResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    GeneralDialog.newInstance(
                        GeneralDialogData(
                            titleRes = R.string.receive_mail,
                            message = getString(R.string.desc_register),
                            messageIcon = R.drawable.ico_default_photo,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = {
                                viewModel.registerAccount.value?.let { it1 ->
                                    viewModel.registerPw.value?.let { it2 ->
                                        viewModel.doLogin(it1, it2)
                                    }
                                }
                            }
                        )
                    ).setCancel(false)
                        .show(requireActivity().supportFragmentManager)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.loginResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    progressHUD?.dismiss()
                    mainViewModel?.startMQTT()
                    GeneralDialog.newInstance(
                        GeneralDialogData(
                            titleIcon = R.drawable.img_login_success,
                            titleRes = R.string.desc_success,
                            message = viewModel.accountManager.getProfile().friendlyName,
                            messageIcon = R.drawable.ico_default_photo,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = { navigateTo(NavigateItem.Up) },
                            attachmentId = viewModel.accountManager.getProfile().avatarAttachmentId
                        )
                    ).setCancel(false).show(requireActivity().supportFragmentManager)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.mobile.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrBlank()) {
                tv_get_code.isEnabled = false
                tv_get_code.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_black_1_30))
            } else {
                tv_get_code.isEnabled = true
                tv_get_code.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_black_1))
            }
        })

        viewModel.validateMessageResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    object : CountDownTimer( 60000, 1000) {
                        override fun onFinish() {
                            tv_get_code.isEnabled = true
                            tv_get_code.text = getString(R.string.login_get_code)
                        }

                        override fun onTick(p0: Long) {
                            tv_get_code.isEnabled = false
                            tv_get_code.text = String.format(getString(R.string.send_code_count_down), p0 / 1000)
                        }
                    }.start()
                }
                is Error -> onApiError(it.throwable)
            }
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
                R.id.btnClose, R.id.btn_register_cancel, R.id.btn_login_cancel -> navigateTo(
                    NavigateItem.Up
                )
                R.id.btn_register -> {
                    viewModel.doRegisterValidateAndSubmit(tv_call_prefix.text.toString())
                }
                R.id.btn_forget -> navigateTo(NavigateItem.Destination(R.id.action_loginFragment_to_forgetPasswordFragment))
                R.id.btn_login -> viewModel.doLoginValidateAndSubmit(tv_login_call_prefix.text.toString())
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
            viewModel.accountManager.keepAccount = isChecked
        }

        tv_get_code.setOnClickListener {
            viewModel.callValidateMessage(edit_mobile.text.toString())
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

        tv_login_call_prefix.setOnClickListener {
            viewModel.changePrefixCount++
            if (viewModel.changePrefixCount == 10) {
                viewModel.changePrefixCount = 0

                if (tv_login_call_prefix.text == getString(R.string.login_mobile_call_prefix_taiwan)) {
                    tv_login_call_prefix.text = getString(R.string.login_mobile_call_prefix_china)
                    ToastUtils.showShort("Change to +86")
                } else {
                    tv_login_call_prefix.text = getString(R.string.login_mobile_call_prefix_taiwan)
                    ToastUtils.showShort("Change to +886")
                }
            }

            if (viewModel.timer == null) viewModel.startTimer()
        }
    }

    override fun initSettings() {
        useAdultTheme(false)
        viewModel.mobile.bindingEditText = edit_mobile
        viewModel.verificationCode.bindingEditText = edit_verification_code
        viewModel.inviteCode.bindingEditText = edit_invite_code
        viewModel.friendlyName.bindingEditText = edit_friendly_name
        viewModel.registerPw.bindingEditText = edit_register_pw
        viewModel.confirmPw.bindingEditText = edit_register_confirm_pw
        viewModel.loginAccount.bindingEditText = edit_login_account
        viewModel.loginPw.bindingEditText = edit_login_pw

        tl_type.selectTab(
            this.arguments?.getInt(KEY_TYPE, TYPE_REGISTER)?.let { tl_type.getTabAt(it) })

        val keepAccount = viewModel.accountManager.keepAccount

        cb_keep_account.isChecked = keepAccount

        viewModel.loginAccount.value = when (keepAccount) {
            true -> {
                val profile = viewModel.accountManager.getProfile()
                profile.account
            }
            false -> ""
        }

        tv_get_code.isEnabled = false
    }

    override fun navigateTo(item: NavigateItem) {
        findNavController().also { navController ->
            when (item) {
                NavigateItem.Up -> {
                    when (navController.navigateUp()) {
                        false -> when (val activity = requireActivity()) {
                            is LoginActivity -> activity.finish()
                        }
                    }
                }
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
                showErrorMessageDialog(getString(R.string.error_email_duplicate))
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
            LOGIN_409000 -> {
                showErrorMessageDialog(getString(R.string.error_account_duplicate))
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
}