package com.dabenxiang.mimi.view.updateprofile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.ProfileItem
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.FilterDialogFragment
import com.dabenxiang.mimi.view.listener.OnDialogListener
import kotlinx.android.synthetic.main.fragment_update_profile.*
import kotlinx.android.synthetic.main.item_setting_bar.*

class UpdateProfileFragment : BaseFragment() {

    private val viewModel: UpdateProfileViewModel by viewModels()

    companion object {
        private const val KEY_TYPE = "TYPE"
        private const val KEY_PROFILE = "PROFILE"
        const val TYPE_NAME = 0
        const val TYPE_EMAIL = 1
        const val TYPE_GEN = 2
        const val TYPE_BIRTHDAY = 3

        fun createBundle(type: Int, profileItem: ProfileItem) = Bundle().also {
            it.putInt(KEY_TYPE, type)
            it.putSerializable(KEY_PROFILE, profileItem)
        }
    }

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initSettings()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_update_profile
    }

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
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    progressHUD?.dismiss()
                    navigateTo(NavigateItem.Up)
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.btn_confirm -> {
                    if (viewModel.type == TYPE_BIRTHDAY) {
                        //FIXME user TIMEFORMAT !!!!
                        viewModel.content.value = edit_birthday.text.toString()
                        viewModel.birthday.value = edit_birthday.text.toString()
                    }else if (viewModel.type == TYPE_GEN) {

                    }else{
                        viewModel.content.value = edit_content.text.toString()
                    }
                    viewModel.doRegisterValidateAndSubmit()
                }
                R.id.edit_content -> {

                }
                R.id.content_gender -> {
                    showFilterDialog(
                        R.string.setting_choose,
                        R.array.filter_gender,
                        R.array.filter_gender_value,
                        viewModel.profileItem.gender ?: 0,
                        onDialogListener
                    )
                }
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_confirm.setOnClickListener(it)
            if (viewModel.type == TYPE_GEN) {
                //FIXME EditText force change !!
                content_gender.setOnClickListener(it)
            }
        }
    }

    override fun initSettings() {
        arguments?.also { it ->
            viewModel.profileItem = it.getSerializable(KEY_PROFILE) as ProfileItem
            viewModel.type = it.getInt(KEY_TYPE, TYPE_NAME)
            edit_content.visibility = View.VISIBLE
            content_gender.visibility = View.INVISIBLE
            edit_birthday.visibility = View.INVISIBLE
            if (viewModel.profileItem.username.isNullOrEmpty()) {
                when (viewModel.type) {
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
                    TYPE_GEN -> {

                    }
                    TYPE_BIRTHDAY -> {
                        tv_title.text = getString(R.string.setting_birthday_title)
                        tv_text.text = getString(R.string.setting_birthday)
                        edit_content.visibility = View.INVISIBLE
                        edit_birthday.visibility = View.VISIBLE
                        edit_birthday.listen()
                    }
                }
                viewModel.content.bindingEditText = edit_content
                viewModel.birthday.bindingEditText = edit_birthday
            } else {
                when (viewModel.type) {
                    TYPE_NAME -> {
                        edit_content.setText(viewModel.profileItem.friendlyName)
                    }
                    TYPE_EMAIL -> {
                        edit_content.setText(viewModel.profileItem.email)
                    }
                    TYPE_GEN -> {
                        content_gender.visibility = View.VISIBLE
                        edit_content.visibility = View.INVISIBLE
                        content_gender.setText(getString(viewModel.profileItem.getGenderRes()))
                    }
                    TYPE_BIRTHDAY -> {
                        edit_content.visibility = View.INVISIBLE
                        edit_birthday.visibility = View.VISIBLE
                        //FIXME
                        edit_birthday.setText(viewModel.profileItem.birthday!!.split("T")[0])
                        edit_birthday.listen()
                    }
                }
            }
        }
    }

    private fun showFilterDialog(
        titleId: Int,
        textArrayId: Int,
        valueArrayId: Int,
        selectedValue: Int,
        dialogListener: OnDialogListener
    ) {
        val dialog = FilterDialogFragment.newInstance(
            FilterDialogFragment.Content(
                titleId,
                textArrayId,
                valueArrayId,
                dialogListener,
                selectedValue
            )
        )
        dialog.show(
            requireActivity().supportFragmentManager,
            FilterDialogFragment::class.java.simpleName
        )
    }

    private val onDialogListener = object : OnDialogListener {
        override fun onItemSelected(value: Int, text: String) {
            viewModel.profileItem.gender = value
            content_gender.setText(getString(viewModel.profileItem.getGenderRes()))
        }
    }
}