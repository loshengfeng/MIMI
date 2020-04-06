package com.dabenxiang.mimi.view.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.item_login.*
import kotlinx.android.synthetic.main.item_register.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginFragment : BaseFragment() {

    private val viewModel by viewModel<LoginViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_login
    }

    override fun setupObservers() {
        Timber.d("${LoginFragment::class.java.simpleName}_setupObservers")
        viewModel.accountError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tvLoginAccError.visibility = View.INVISIBLE
            } else {
                tvLoginAccError.text = getString(it)
                tvLoginAccError.visibility = View.VISIBLE
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

        viewModel.registerPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tvRegisterAccError.visibility = View.INVISIBLE
            } else {
                tvRegisterAccError.text = getString(it)
                tvRegisterAccError.visibility = View.VISIBLE
            }
        })

        viewModel.confirmPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tvPwConfirmError.visibility = View.INVISIBLE
            } else {
                tvPwConfirmError.text = getString(it)
                tvPwConfirmError.visibility = View.VISIBLE
            }
        })

        viewModel.loginPasswordError.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                tvLoginPwError.visibility = View.INVISIBLE
            } else {
                tvLoginPwError.text = getString(it)
                tvLoginPwError.visibility = View.VISIBLE
            }
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
                R.id.btnClose -> Navigation.findNavController(view!!).navigateUp()
                R.id.btnRegisterCancel, R.id.btnLoginCancel -> Navigation.findNavController(view!!).navigateUp()
                R.id.btnRegister -> {
                    GeneralUtils.showToast(context!!, "Register")
                    viewModel.doRegisterValidateAndSubmit(
                        edtRegisterAcc.text.toString(),
                        edtEmail.text.toString(),
                        edtRegisterPw.text.toString(),
                        edtPwConfirm.text.toString()
                    )
                }
                R.id.btnForget -> GeneralUtils.showToast(context!!, "Forget")
                R.id.btnLogin -> {
                    GeneralUtils.showToast(context!!, "btnLogin")
                    viewModel.doLoginValidateAndSubmit(
                        edtLoginAcc.text.toString(),
                        edtLoginPw.text.toString()
                    )
                }
            }
        }.also {
            btnClose.setOnClickListener(it)
            btnRegisterCancel.setOnClickListener(it)
            btnLoginCancel.setOnClickListener(it)
            btnRegister.setOnClickListener(it)
            btnForget.setOnClickListener(it)
            btnLogin.setOnClickListener(it)
        }

        cbKeepAcc.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("${LoginFragment::class.java.simpleName}_isChecked = $isChecked")
            GeneralUtils.showToast(context!!, "Remember")
        }
    }

    override fun initSettings() {
        Timber.d("${LoginFragment::class.java.simpleName}_initSettings")
    }
}