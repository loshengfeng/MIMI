package com.dabenxiang.mimi.view.personal

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.navigation.Navigation
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class PersonalFragment : BaseFragment() {
    private val viewModel by viewModel<PersonalViewModel>()
    private val isLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
        /*Handler().postDelayed({
            Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_loginFragment)
        },1000)*/
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun setupObservers() {
        Timber.d("${PersonalFragment::class.java.simpleName}_setupObservers")
    }

    override fun setupListeners() {
        Timber.d("${PersonalFragment::class.java.simpleName}_setupListeners")

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.btnTopup -> GeneralUtils.showToast(context!!, "btnTopup")
                R.id.btnPhoto -> GeneralUtils.showToast(context!!, "btnFavorite")
                R.id.btnRaw1 -> GeneralUtils.showToast(context!!, "btnTopupHistory")
                R.id.btnEmail -> GeneralUtils.showToast(context!!, "btnChatHistory")
                R.id.btnRaw2 -> Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_settingFragment)
                R.id.btnLogout -> GeneralUtils.showToast(context!!, "btnLogout")
            }
        }.also {
            btnTopup.setOnClickListener(it)
            btnPhoto.setOnClickListener(it)
            btnRaw1.setOnClickListener(it)
            btnEmail.setOnClickListener(it)
            btnRaw2.setOnClickListener(it)
            btnLogout.setOnClickListener(it)
        }
    }

    private fun initSettings() {
        when(isLogin) {
            true -> {
                item_is_Login.visibility = View.VISIBLE
                item_is_not_Login.visibility = View.GONE
            }
            false -> {
                item_is_Login.visibility = View.GONE
                item_is_not_Login.visibility = View.VISIBLE
            }
        }

        // todo: for testing
        tvName.text = "好大一棵洋梨"
        tvCoco.text = "200"
        tvNew.text = "N"
        tvVersion.text = "v1.0.0"
        tvContent1.text = "文字內容文字內容"
        tvContent2.text = "文字內容文字內容文字內容文字內容文字內容文字內容文字內容文字內容"
    }
}