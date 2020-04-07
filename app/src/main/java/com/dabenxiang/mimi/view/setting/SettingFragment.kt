package com.dabenxiang.mimi.view.setting

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_setting.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class SettingFragment : BaseFragment() {
    private val viewModel by viewModel<SettingViewModel>()

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
                R.id.btnBack -> Navigation.findNavController(view!!).navigateUp()
                R.id.btnTopup -> GeneralUtils.showToast(context!!, "btnTopup")
                R.id.btnPhoto -> GeneralUtils.showToast(context!!, "btnPhoto")
                R.id.btnRaw1 -> GeneralUtils.showToast(context!!, "btnRaw1")
                R.id.btnEmail -> GeneralUtils.showToast(context!!, "btnEmail")
                R.id.btnRaw2 -> {
                    GeneralUtils.showToast(context!!, "btnRaw2")
//                    Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_xxxFragment)
                }
                R.id.btnChangPw -> GeneralUtils.showToast(context!!, "btnChangPw")
            }
        }.also {
            btnBack.setOnClickListener(it)
            btnPhoto.setOnClickListener(it)
            btnRaw1.setOnClickListener(it)
            btnEmail.setOnClickListener(it)
            btnRaw2.setOnClickListener(it)
            btnChangPw.setOnClickListener(it)
        }
    }

    private fun initSettings() {
        tvRaw1.text = "內容內容內容內容"
        tvEmail.text = "ABCDEFG@abcde.com"
        tvRaw2.text = "內容內容內容內容"
    }
}