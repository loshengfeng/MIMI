package com.dabenxiang.mimi.view.personal

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
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
import com.dabenxiang.mimi.view.setting.SettingFragment
import com.dabenxiang.mimi.widget.utility.AppUtils
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import retrofit2.HttpException

@ExperimentalCoroutinesApi
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
                    progressHUD?.dismiss()

                    val meItem = it.result

                    val profile = viewModel.accountManager.getProfile()
                    profile.userId = meItem.id ?: 0
                    profile.avatarAttachmentId = meItem.avatarAttachmentId ?: 0
                    profile.friendlyName = meItem.friendlyName ?: ""
                    viewModel.accountManager.setupProfile(profile)

                    viewModel.getAttachment()
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

        viewModel.imageBitmap.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ApiResult.Loading -> progressHUD?.show()
                is ApiResult.Error -> onApiError(it.throwable, onHttpErrorBlock = { httpError ->
                    when (httpError.httpExceptionItem.errorItem.code) {
                        // todo: confirm by jeff...
//                        ErrorCode.NOT_FOUND -> { viewModel.toastData.value = "no photo"}
                    }
                })
                is ApiResult.Success -> {
                    val options: RequestOptions = RequestOptions()
                        .transform(MultiTransformation(CenterCrop(), CircleCrop()))
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .priority(Priority.NORMAL)

                    Glide.with(this).load(it.result)
                        .apply(options)
                        .into(iv_photo)
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
                R.id.tv_follow -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myFollowFragment))
                R.id.tv_topup_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_orderFragment))
                R.id.tv_chat_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_chatHistoryFragment))
                R.id.tv_my_post -> GeneralUtils.showToast(context!!, "My post")
                R.id.tv_setting -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_settingFragment, viewModel.byteArray?.let { SettingFragment.createBundle(it) }))
                R.id.tv_logout -> viewModel.signOut()
                R.id.tv_login -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_loginFragment, LoginFragment.createBundle(TYPE_LOGIN)))
                R.id.tv_register -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_loginFragment, LoginFragment.createBundle(TYPE_REGISTER)))
            }
        }.also {
            tv_topup.setOnClickListener(it)
            tv_follow.setOnClickListener(it)
            tv_topup_history.setOnClickListener(it)
            tv_chat_history.setOnClickListener(it)
            tv_my_post.setOnClickListener(it)
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