package com.dabenxiang.mimi.view.changepassword

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.android.synthetic.main.fragment_resend_mail.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import timber.log.Timber

class ChangePasswordFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        Timber.d("${ChangePasswordFragment::class.java.simpleName}_setupObservers")
        return R.layout.fragment_change_password
    }

    override fun setupObservers() {
        Timber.d("${ChangePasswordFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${ChangePasswordFragment::class.java.simpleName}_setupListeners")
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> Navigation.findNavController(view!!).navigateUp()
                R.id.btn_confirm -> GeneralUtils.showToast(context!!, "btn_confirm")
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_confirm.setOnClickListener(it)
        }

        cb_show_current_pw.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("${ChangePasswordFragment::class.java.simpleName}_isChecked = $isChecked")
            GeneralUtils.showToast(context!!, "Remember")
        }

        cb_show_new_pw.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("${ChangePasswordFragment::class.java.simpleName}_isChecked = $isChecked")
            GeneralUtils.showToast(context!!, "Remember")
        }

        cb_show_confirm_pw.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("${ChangePasswordFragment::class.java.simpleName}_isChecked = $isChecked")
            GeneralUtils.showToast(context!!, "Remember")
        }
    }

    private fun initSettings() {
        tv_title.text = getString(R.string.setting_change_password)
    }
}