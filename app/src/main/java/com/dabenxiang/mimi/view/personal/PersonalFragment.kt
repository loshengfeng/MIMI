package com.dabenxiang.mimi.view.personal

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import kotlinx.android.synthetic.main.item_personal_is_not_login.*
import retrofit2.HttpException
import timber.log.Timber

class PersonalFragment : BaseFragment() {

    private val viewModel: PersonalViewModel by viewModels()
    private var interactionListener: InteractionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            interactionListener = context as InteractionListener
        } catch (e: ClassCastException) {
            Timber.e("PersonalFragment interaction listener can't cast")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            interactionListener?.changeNavigationPosition(R.id.navigation_home)
        }
        initSettings()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun onResume() {
        super.onResume()
        viewModel.getTotalUnread()
    }

    override fun setupObservers() {
        viewModel.meItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Success -> {
                    progressHUD?.dismiss()

                    val meItem = it.result
                    tv_name.text = meItem.friendlyName.toString()
                    tv_Point.text = meItem.availablePoint.toString()

                    //TODO: 目前先不判斷是否有驗證過
//                    takeUnless { meItem.isEmailConfirmed == true }?.run {
//                        (requireActivity() as MainActivity).showEmailConfirmDialog()
//                    }
                    viewModel.loadImage(meItem.avatarAttachmentId, iv_photo, LoadImageType.AVATAR)
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.apiSignOut.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Loading -> progressHUD?.show()
                is Loaded -> progressHUD?.dismiss()
                is Empty -> {
                    item_is_Login.visibility = View.GONE
                    item_is_not_Login.visibility = View.VISIBLE
                }
                is Error -> {
                    when (it.throwable) {
                        is HttpException -> {
                            val data = GeneralUtils.getHttpExceptionData(it.throwable)
                            data.errorItem.message?.also { message ->
                                GeneralDialog.newInstance(
                                    GeneralDialogData(
                                        message = message,
                                        messageIcon = R.drawable.ico_default_photo,
                                        secondBtn = getString(R.string.btn_confirm)
                                    )
                                ).show(parentFragmentManager)
                            }
                        }
                    }
                }
            }
        })

        viewModel.unreadResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    tv_new.visibility = if (it.result == 0) View.INVISIBLE else View.VISIBLE
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.totalUnreadResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    iv_new.visibility = if (it.result == 0) View.INVISIBLE else View.VISIBLE
                    interactionListener?.refreshBottomNavigationBadge(it.result)
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {
        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_topup -> interactionListener?.changeNavigationPosition(R.id.navigation_topup)
                R.id.tv_follow -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myFollowFragment))
                R.id.tv_topup_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_orderFragment))
                R.id.tv_chat_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_chatHistoryFragment))
                R.id.tv_my_post -> findNavController().navigate(R.id.action_personalFragment_to_myPostFragment)
                R.id.tv_setting -> navigateTo(
                    NavigateItem.Destination(R.id.action_to_settingFragment)
                )
                R.id.tv_logout -> {
                    Glide.with(this).clear(iv_photo)
                    Glide.with(this).load(R.drawable.default_profile_picture).into(iv_photo)
                    viewModel.signOut()
                }
                R.id.tv_login -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_personalFragment_to_loginFragment,
                        LoginFragment.createBundle(TYPE_LOGIN)
                    )
                )
                R.id.tv_register -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_personalFragment_to_loginFragment,
                        LoginFragment.createBundle(TYPE_REGISTER)
                    )
                )
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
        interactionListener?.setAdult(false)

        tv_version_is_login.text = BuildConfig.VERSION_NAME
        tv_version_is_not_login.text = BuildConfig.VERSION_NAME

        if (viewModel.isLogin()) {
            item_is_Login.visibility = View.VISIBLE
            item_is_not_Login.visibility = View.GONE
            viewModel.getMe()
        } else {
            item_is_Login.visibility = View.GONE
            item_is_not_Login.visibility = View.VISIBLE
        }
    }
}