package com.dabenxiang.mimi.view.updateprofile

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import kotlinx.android.synthetic.main.fragment_update_profile.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
class UpdateProfileFragment :BaseFragment<UpdateProfileViewModel>() {
    private val viewModel by viewModel<UpdateProfileViewModel>()

    companion object {
        private const val KEY_TYPE = "TYPE"
        private const val KEY_PROFILE = "PROFILE"
        const val TYPE_NAME = 0
        const val TYPE_EMAIL = 1
        const val TYPE_BIRTHDAY = 2

        fun createBundle(type: Int, profileItem: ProfileItem) = Bundle().also {
            it.putInt(KEY_TYPE, type)
            it.putSerializable(KEY_PROFILE, profileItem)
        }

    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_update_profile }

    override fun fetchViewModel(): UpdateProfileViewModel? { return viewModel }

    override fun setupObservers() {
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it == "") {
                edit_content.setBackgroundResource(R.drawable.edit_text_rectangle)
                tv_content_error.visibility = View.INVISIBLE
            } else {
                edit_content.setBackgroundResource(R.drawable.edit_text_error_rectangle)
                tv_content_error.text = it
                tv_content_error.visibility = View.VISIBLE
            }
        })

        viewModel.updateResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Empty -> {
                    progressHUD?.dismiss()
                    navigateTo(NavigateItem.Up)
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.btn_confirm -> viewModel.doRegisterValidateAndSubmit()
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_confirm.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        arguments?.also { it ->
            viewModel.profileItem = it.getSerializable(KEY_PROFILE) as ProfileItem
            viewModel.type = it.getInt(KEY_TYPE, TYPE_NAME)
            when(viewModel.type) {
                TYPE_NAME -> {
                    tv_title.text = getString(R.string.setting_change_name)
                    tv_text.text = getString(R.string.setting_name)
                    edit_content.hint = getString(R.string.login_name)
                }
                TYPE_EMAIL -> {
                    tv_title.text = getString(R.string.setting_mail_title)
                    tv_text.text = getString(R.string.setting_email)
                    edit_content.hint = getString(R.string.login_email)
                }
                TYPE_BIRTHDAY -> {
                    tv_title.text = getString(R.string.setting_birthday_title)
                    tv_text.text = getString(R.string.setting_birthday)
                    edit_content.visibility = View.INVISIBLE
                    edit_birthday.visibility = View.VISIBLE
                    edit_birthday.listen()
                }
            }
        }
        viewModel.content.bindingEditText = edit_content
        viewModel.birthday.bindingEditText = edit_birthday
    }
}