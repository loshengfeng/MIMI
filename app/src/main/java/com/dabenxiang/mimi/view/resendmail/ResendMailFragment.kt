package com.dabenxiang.mimi.view.resendmail

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_resend_mail.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ResendMailFragment :BaseFragment<ResendMailViewModel>() {
    private val viewModel by viewModel<ResendMailViewModel>()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_resend_mail
    }

    override fun fetchViewModel(): ResendMailViewModel? {
        return viewModel
    }

    override fun setupObservers() {
        Timber.d("${ResendMailFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${ResendMailFragment::class.java.simpleName}_setupListeners")
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> Navigation.findNavController(view!!).navigateUp()
                R.id.btn_resend -> GeneralUtils.showToast(context!!, "btn_resend")
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_resend.setOnClickListener(it)
        }
    }

    private fun initSettings() {
        tv_title.text = getString(R.string.setting_resend_mail)
        tv_text.text = getString(R.string.setting_resend_mail)
    }
}