package com.dabenxiang.mimi.view.login

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.login.LoginDialogFragment
import com.dabenxiang.mimi.view.dialog.login.OnLoginDialogListener
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.widget.utility.AppUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*
import org.koin.android.viewmodel.ext.android.viewModel
import retrofit2.HttpException

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
                it.putInt(KEY_TYPE, type)
            }
        }

    }

    private var dialog: LoginDialogFragment? = null

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    override fun fetchViewModel(): LoginViewModel? {
        return viewModel
    }

    override fun setupObservers() {
        viewModel.apiResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Empty -> {
                    progressHUD?.dismiss()
                    navigateTo(NavigateItem.Up)
                }
                is ApiResult.Error -> {
                    progressHUD?.dismiss()
                    when (it.throwable) {
                        is HttpException -> {
                            val data = AppUtils.getHttpExceptionData(it.throwable)
                            data.errorItem.message?.also { message ->
                                GeneralDialog.newInstance(
                                    GeneralDialogData(
                                        titleRes = R.string.login_yet,
                                        message = message,
                                        messageIcon = R.drawable.ico_default_photo,
                                        secondBtn = getString(R.string.btn_confirm)
                                    )
                                ).show(parentFragmentManager)
                            }
                        }
                    }
                }
            }
        })

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

        viewModel.friendlyNameError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                edit_friendly_name.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_friendly_name_error.visibility = View.INVISIBLE
            } else {
                edit_friendly_name.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_friendly_name_error.text = getString(it)
                tv_friendly_name_error.visibility = View.VISIBLE
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

        viewModel.loginResult.observe(viewLifecycleOwner, Observer {
            dialog = LoginDialogFragment.newInstance(onLoginDialogListener, LoginDialogFragment.TYPE_SUCCESS)
            dialog?.show(activity!!.supportFragmentManager, LoginDialogFragment::class.java.simpleName)
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
            viewModel.toastData.value = "Remember: $isChecked"
            // todo: save Account to SharePreference...
        }
    }

    private fun initSettings() {
        viewModel.registerAccount.bindingEditText = edit_register_account
        viewModel.email.bindingEditText = edit_email
        viewModel.friendlyName.bindingEditText = edit_friendly_name
        viewModel.registerPw.bindingEditText = edit_register_pw
        viewModel.confirmPw.bindingEditText = edit_register_confirm_pw
        viewModel.loginAccount.bindingEditText = edit_login_account
        viewModel.loginPw.bindingEditText = edit_login_pw

        tl_type.selectTab(this.arguments?.getInt(KEY_TYPE, TYPE_REGISTER)?.let { tl_type.getTabAt(it) })
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
                is NavigateItem.PopBackStack -> navController.popBackStack(item.fragmentId, item.inclusive)
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
}