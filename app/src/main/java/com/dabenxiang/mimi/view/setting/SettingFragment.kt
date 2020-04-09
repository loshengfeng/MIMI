package com.dabenxiang.mimi.view.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SettingFragment : BaseFragment() {
    private val viewModel by viewModel<SettingViewModel>()
    private var isValidated = true

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_setting
    }

    override fun setupObservers() {
        Timber.d("${SettingFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${SettingFragment::class.java.simpleName}_setupListeners")

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> Navigation.findNavController(view!!).navigateUp()
                R.id.btnPhoto -> GeneralUtils.showToast(context!!, "btnPhoto")
                R.id.tv_raw1 -> GeneralUtils.showToast(context!!, "btnRaw1")
                R.id.btnEmail -> Navigation.findNavController(view!!).navigate(R.id.action_settingFragment_to_resendMailFragment)
                R.id.btnRaw2 -> GeneralUtils.showToast(context!!, "btnRaw2")
                R.id.btnChangPw -> Navigation.findNavController(view!!).navigate(R.id.action_settingFragment_to_changePasswordFragment)
            }
        }.also {
            tv_back.setOnClickListener(it)
            btnPhoto.setOnClickListener(it)
            tv_raw1.setOnClickListener(it)
            btnEmail.setOnClickListener(it)
            btnRaw2.setOnClickListener(it)
            btnChangPw.setOnClickListener(it)
        }
    }

    private fun initSettings() {
        tvRaw1.text = "內容內容內容內容"
        tvEmail.text = "ABCDEFG@abcde.com"
        var img = when(isValidated) {
            true -> context!!.resources.getDrawable(R.drawable.ico_checked)
            else -> context!!.resources.getDrawable(R.drawable.ico_checked_error)
        }
        tvEmail.setCompoundDrawablesWithIntrinsicBounds(null, null, img,null)
        tvRaw2.text = "內容內容內容內容"
    }
}