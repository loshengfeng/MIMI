package com.dabenxiang.mimi.view.personal

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.AppUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import retrofit2.HttpException

class PersonalFragment : BaseFragment<PersonalViewModel>() {
    private val viewModel by viewModel<PersonalViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()
    }

    override fun getLayoutId(): Int { return R.layout.fragment_personal }

    override fun fetchViewModel(): PersonalViewModel? { return viewModel }

    @ExperimentalCoroutinesApi
    override fun setupObservers() {
        viewModel.meItem.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable)
                is ApiResult.Success -> {
                    val meItem = it.result
                    progressHUD?.dismiss()
                    tv_name.text = meItem.friendlyName.toString()
                    tv_Point.text = meItem.availablePoint.toString()
                    // todo: confirm by Jeff...
                    tv_new.visibility = when(meItem.hasNewMessage) {
                        true -> View.VISIBLE
                        else -> View.GONE
                    }
                }
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })

        viewModel.accountManager.isLogin.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> {
                    viewModel.getMe()
                    item_is_Login.visibility = View.VISIBLE
                    item_is_not_Login.visibility = View.GONE
                }
                false -> {
                    item_is_Login.visibility = View.GONE
                    item_is_not_Login.visibility = View.VISIBLE
                }
            }

            // todo: confirm by Jeff...
            tv_new.text = "N"
            tv_content.text = "文字內容文字內容"
            tv_sub_content.text = "文字內容文字內容文字內容文字內容文字內容文字內容文字內容文字內容"
        })

        viewModel.apiSignOut.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> {
                    when (it.throwable) {
                        is HttpException -> {
                            val data = AppUtils.getHttpExceptionData(it.throwable)
                            data.errorItem.message?.also { message ->
                                GeneralDialog.newInstance(
                                    GeneralDialogData(
                                        titleRes = 0,
                                        message = message,
                                        messageIcon = R.drawable.ico_default_photo,
                                        secondBtn = getString(R.string.btn_confirm)
                                    )
                                ).show(parentFragmentManager)
                            }
                        }
                    }
                }
                is ApiResult.Empty -> {}
                is ApiResult.Loaded -> progressHUD?.dismiss()
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_topup -> GeneralUtils.showToast(context!!, "btnTopup")
                R.id.tv_follow -> GeneralUtils.showToast(context!!, "btnFollow")
                R.id.tv_topup_history -> Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_topupHistoryFragment)
                R.id.tv_chat_history -> Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_chatHistoryFragment)
                R.id.tv_setting -> Navigation.findNavController(view!!).navigate(R.id.action_personalFragment_to_settingFragment)
                R.id.tv_logout -> viewModel.signOut()
                R.id.tv_login -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_loginFragment, LoginFragment.createBundle(TYPE_LOGIN)))
                R.id.tv_register -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_loginFragment, LoginFragment.createBundle(TYPE_REGISTER)))
            }
        }.also {
            tv_topup.setOnClickListener(it)
            tv_follow.setOnClickListener(it)
            tv_topup_history.setOnClickListener(it)
            tv_chat_history.setOnClickListener(it)
            tv_setting.setOnClickListener(it)
            tv_logout.setOnClickListener(it)
            tv_login.setOnClickListener(it)
            tv_register.setOnClickListener(it)
        }
    }

    override fun initSettings() {
        super.initSettings()
        tv_version_is_login.text = BuildConfig.VERSION_NAME
        tv_version_is_not_login.text = BuildConfig.VERSION_NAME
    }
}