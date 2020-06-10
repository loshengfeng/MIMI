package com.dabenxiang.mimi.view.setting

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.FilterDialogFragment
import com.dabenxiang.mimi.view.listener.OnDialogListener
import com.dabenxiang.mimi.view.updateprofile.UpdateProfileFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.item_setting_bar.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@ExperimentalCoroutinesApi
class SettingFragment : BaseFragment<SettingViewModel>() {
    private val viewModel by viewModel<SettingViewModel>()

    override val bottomNavigationVisibility: Int
        get() = View.GONE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_setting }

    override fun fetchViewModel(): SettingViewModel? { return viewModel }

    override fun setupObservers() {
        viewModel.profileItem.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Loaded -> progressHUD?.dismiss()
                is ApiResult.Success -> {
                    tv_name.text = viewModel.profileData?.friendlyName
                    tv_email.text = viewModel.profileData?.email
                    tv_account.text = viewModel.profileData?.username
                    var img: Drawable? = null
                    when(viewModel.profileData?.emailConfirmed ?: false) {
                        true -> {
                            img = context!!.resources.getDrawable(R.drawable.ico_checked)
                            btn_resend.visibility = View.GONE
                        }
                        else -> btn_resend.visibility = View.VISIBLE
                    }

                    tv_email.setCompoundDrawablesWithIntrinsicBounds(null, null, img,null)

//                    GeneralDialog.newInstance(
//                        GeneralDialogData(
//                            titleRes = R.string.desc_success,
//                            message = it.result.friendlyName.toString(),
//                            messageIcon = R.drawable.ico_default_photo,
//                            secondBtn = getString(R.string.btn_confirm),
//                            secondBlock = { navigateTo(NavigateItem.Up) }
//                        )
//                    ).show(requireActivity().supportFragmentManager)
                }
                is ApiResult.Error -> onApiError(it.throwable)
                /*{
                    when (val errorHandler = it.throwable.handleException { ex -> mainViewModel?.processException(ex) }) {
                        is ExceptionResult.HttpError -> showErrorMessageDialog(errorHandler.httpExceptionItem.errorItem.message.toString())
                        else -> onApiError(it.throwable)
                    }
                }*/
            }
        })

        viewModel.resendResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Empty -> { }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.updateResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Empty -> { }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_back -> navigateTo(NavigateItem.Up)
                R.id.btn_photo -> GeneralUtils.showToast(context!!, "btnPhoto")
                R.id.btn_name -> navigateTo(NavigateItem.Destination(R.id.updateProfileFragment,
                    viewModel.profileData?.let {
                        UpdateProfileFragment.createBundle(UpdateProfileFragment.TYPE_NAME,
                            it
                        )
                    }))
                R.id.btn_email -> navigateTo(NavigateItem.Destination(R.id.updateProfileFragment,
                    viewModel.profileData?.let {
                        UpdateProfileFragment.createBundle(UpdateProfileFragment.TYPE_EMAIL,
                            it
                        )
                    }))
                R.id.btn_resend -> viewModel.resendEmail()
                R.id.btn_chang_pw -> navigateTo(NavigateItem.Destination(R.id.action_settingFragment_to_changePasswordFragment))
                R.id.btn_gender -> {
                    Timber.d("btn_gender: ${viewModel.profileData.toString()}")
                    showFilterDialog(
                        R.string.setting_choose,
                        R.array.filter_gender,
                        R.array.filter_gender_value,
                        viewModel.profileData?.gender ?: 0,
                        onDialogListener
                    )
                }
                R.id.btn_birthday -> navigateTo(NavigateItem.Destination(R.id.updateProfileFragment,
                    viewModel.profileData?.let {
                        UpdateProfileFragment.createBundle(UpdateProfileFragment.TYPE_BIRTHDAY,
                            it
                        )
                    }))
            }
        }.also {
            tv_back.setOnClickListener(it)
            btn_photo.setOnClickListener(it)
            btn_name.setOnClickListener(it)
            btn_email.setOnClickListener(it)
            btn_resend.setOnClickListener(it)
            btn_chang_pw.setOnClickListener(it)
            btn_gender.setOnClickListener(it)
            btn_birthday.setOnClickListener(it)
        }
    }

    override fun initSettings() { viewModel.getProfile() }

    private fun showFilterDialog(titleId: Int,
                                 textArrayId: Int,
                                 valueArrayId: Int,
                                 selectedValue: Int,
                                 dialogListener: OnDialogListener
    ) {
        val dialog = FilterDialogFragment.newInstance(FilterDialogFragment.Content(titleId, textArrayId, valueArrayId, dialogListener, selectedValue))
        dialog.show(requireActivity().supportFragmentManager, FilterDialogFragment::class.java.simpleName)
    }

    private val onDialogListener = object : OnDialogListener {
        override fun onItemSelected(value: Int, text: String) {
            viewModel.profileData?.gender = value
            viewModel.updateProfile()
        }
    }
}