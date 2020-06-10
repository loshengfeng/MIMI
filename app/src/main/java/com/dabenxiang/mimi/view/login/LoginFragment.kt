package com.dabenxiang.mimi.view.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.ErrorCode
import com.dabenxiang.mimi.model.api.ExceptionResult
import com.dabenxiang.mimi.model.api.vo.handleException
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*
import kotlinx.android.synthetic.main.item_register.edit_email
import kotlinx.android.synthetic.main.item_register.tv_email_error
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<LoginViewModel>() {
    private val viewModel by viewModel<LoginViewModel>()

    companion object {
        private const val KEY_TYPE = "TYPE"
        const val TYPE_REGISTER = 0
        const val TYPE_LOGIN = 1

        fun createBundle(type: Int): Bundle { return Bundle().also { it.putInt(KEY_TYPE, type) } }
    }


    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.accountManager.isLogin.value == true) { navigateTo(NavigateItem.Up) }
    }

    override fun getLayoutId(): Int { return R.layout.fragment_login }

    override fun fetchViewModel(): LoginViewModel? { return viewModel }

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
            cb_email.visibility = View.VISIBLE
            if (it == "") {
                edit_email.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_email_error.visibility = View.INVISIBLE
                cb_email.isChecked = true
            } else {
                edit_email.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_email_error.text = it
                tv_email_error.visibility = View.VISIBLE
                cb_email.isChecked = false
            }
        })

        viewModel.registerAccountError.observe(viewLifecycleOwner, Observer {
            cb_register_account.visibility = View.VISIBLE
            if (it == "") {
                edit_register_account.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_register_account_error.visibility = View.INVISIBLE
                cb_register_account.isChecked = true
            } else {
                edit_register_account.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_register_account_error.text = it
                tv_register_account_error.visibility = View.VISIBLE
                cb_register_account.isChecked = false
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
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Empty -> {
                    progressHUD?.dismiss()
                    GeneralDialog.newInstance(
                        GeneralDialogData(
                            titleRes = R.string.receive_mail,
                            message = getString(R.string.desc_register),
                            messageIcon = R.drawable.ico_default_photo,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = {
                                viewModel.registerAccount.value?.let { it1 -> viewModel.registerPw.value?.let { it2 ->
                                    viewModel.doLogin(it1, it2)
                                } }
                            }
                        )
                    ).show(requireActivity().supportFragmentManager)
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.loginResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Empty -> {
                    progressHUD?.dismiss()
                    viewModel.getProfile()
                }
                is ApiResult.Error -> onApiError(it.throwable)
            }
        })

        viewModel.profileItem.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Success -> {
                    progressHUD?.dismiss()
                    GeneralDialog.newInstance(
                        GeneralDialogData(
                            titleRes = R.string.desc_success,
                            message = it.result.friendlyName.toString(),
                            messageIcon = R.drawable.ico_default_photo,
                            secondBtn = getString(R.string.btn_confirm),
                            secondBlock = { navigateTo(NavigateItem.Up) }
                        )
                    ).show(requireActivity().supportFragmentManager)
                }
                is ApiResult.Error -> {
                    when (val errorHandler = it.throwable.handleException { ex -> mainViewModel?.processException(ex) }) {
                        is ExceptionResult.HttpError -> showErrorMessageDialog(errorHandler.httpExceptionItem.errorItem.message.toString())
                        else -> onApiError(it.throwable)
                    }
                }
            }
        })
    }

    @ExperimentalCoroutinesApi
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
                R.id.btnClose, R.id.btn_register_cancel, R.id.btn_login_cancel -> navigateTo(NavigateItem.Up)
                R.id.btn_register -> viewModel.doRegisterValidateAndSubmit()
                R.id.btn_forget -> navigateTo(NavigateItem.Destination(R.id.action_loginFragment_to_forgetPasswordFragment))
                R.id.btn_login -> viewModel.doLoginValidateAndSubmit()
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
    }

    override fun initSettings() {
        viewModel.registerAccount.bindingEditText = edit_register_account
        viewModel.email.bindingEditText = edit_email
        viewModel.friendlyName.bindingEditText = edit_friendly_name
        viewModel.registerPw.bindingEditText = edit_register_pw
        viewModel.confirmPw.bindingEditText = edit_register_confirm_pw
        viewModel.loginAccount.bindingEditText = edit_login_account
        viewModel.loginPw.bindingEditText = edit_login_pw

        tl_type.selectTab(this.arguments?.getInt(KEY_TYPE, TYPE_REGISTER)?.let { tl_type.getTabAt(it) })

        val keepAccount = viewModel.accountManager.keepAccount

        cb_keep_account.isChecked = keepAccount

        viewModel.loginAccount.value = when (keepAccount) {
            true -> {
                val profile = viewModel.accountManager.getProfile()
                profile.account
            }
            false -> ""
        }
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
            ErrorCode.LOGIN_400000 -> {
                cb_email.isChecked = false
                showErrorMessageDialog(getString(R.string.error_email_duplicate))
            }
            ErrorCode.LOGIN_403001 -> showErrorMessageDialog(getString(R.string.error_username_or_password_incorrect))
            ErrorCode.LOGIN_403002 -> showErrorMessageDialog(getString(R.string.error_account_disable))
            ErrorCode.LOGIN_403004 -> showErrorMessageDialog(getString(R.string.error_validation))
            ErrorCode.LOGIN_403006 -> {
                GeneralDialog.newInstance(
                    GeneralDialogData(
                        titleRes = R.string.desc_success,
                        message = getString(R.string.error_first_login),
                        messageIcon = R.drawable.ico_default_photo,
                        secondBtn = getString(R.string.btn_confirm),
                        secondBlock = {navigateTo(NavigateItem.Destination(R.id.action_loginFragment_to_changePasswordFragment))}
                    )
                ).show(requireActivity().supportFragmentManager)
            }
            ErrorCode.LOGIN_409000 -> {
                cb_register_account.isChecked = false
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