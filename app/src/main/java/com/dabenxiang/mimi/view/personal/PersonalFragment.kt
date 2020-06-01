package com.dabenxiang.mimi.view.personal

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class PersonalFragment : BaseFragment<PersonalViewModel>() {
    private val viewModel by viewModel<PersonalViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun fetchViewModel(): PersonalViewModel? {
        return viewModel
    }

    override fun setupObservers() {}

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_topup -> GeneralUtils.showToast(context!!, "btnTopup")
                R.id.tv_favorite -> GeneralUtils.showToast(context!!, "btnFavorite")
                R.id.tv_topup_history -> Navigation.findNavController(view!!)
                    .navigate(R.id.action_personalFragment_to_topupHistoryFragment)
                R.id.tv_chat_history -> Navigation.findNavController(view!!)
                    .navigate(R.id.action_personalFragment_to_chatHistoryFragment)
                R.id.tv_setting -> Navigation.findNavController(view!!)
                    .navigate(R.id.action_personalFragment_to_settingFragment)
                R.id.tv_logout -> {
                    initSettings()
                }
                R.id.tv_login -> {
                    findNavController().navigate(R.id.action_personalFragment_to_loginFragment, LoginFragment.createBundle(TYPE_LOGIN))
                }
                R.id.tv_register -> {
                    findNavController().navigate(R.id.action_personalFragment_to_loginFragment, LoginFragment.createBundle(TYPE_REGISTER))
                }
            }
        }.also {
            tv_topup.setOnClickListener(it)
            tv_favorite.setOnClickListener(it)
            tv_topup_history.setOnClickListener(it)
            tv_chat_history.setOnClickListener(it)
            tv_setting.setOnClickListener(it)
            tv_logout.setOnClickListener(it)
            tv_login.setOnClickListener(it)
            tv_register.setOnClickListener(it)
        }
    }

    private fun initSettings() {
        when (viewModel.accountManager.isLogin.value) {
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
        tv_name.text = "好大一棵洋梨"
        tv_coco.text = "200"
        tv_new.text = "N"
        tv_version_is_login.text = "v1.0.0"
        tv_version_is_not_login.text = "v1.0.0"
        tv_content.text = "文字內容文字內容"
        tv_sub_content.text = "文字內容文字內容文字內容文字內容文字內容文字內容文字內容文字內容"
    }
}