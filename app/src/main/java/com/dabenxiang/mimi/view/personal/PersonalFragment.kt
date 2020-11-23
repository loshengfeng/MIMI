package com.dabenxiang.mimi.view.personal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
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
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.Behavior.DragCallback
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*


class PersonalFragment : BaseFragment() {

    private val viewModel: PersonalViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSettings()

        appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            var newalpha = 255f + verticalOffset
            newalpha = if (newalpha < 0) 0f else newalpha
            layout_every_day.setAlpha(newalpha)
            layout_vip_unlimit.setAlpha(newalpha)
        })


    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun initSettings() {
        super.initSettings()
        tv_version.text = BuildConfig.VERSION_NAME
      //FIXME
        //            ViewCompat.setNestedScrollingEnabled(nestedScroll, true)
        val behavior = appbar_layout.behavior as AppBarLayout.Behavior?
        if (viewModel.isLogin()) {
            item_is_Login.visibility = View.VISIBLE
            tv_logout.visibility = View.VISIBLE
            viewModel.getMe()
            behavior!!.setDragCallback(object : DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return true
                }
            })
        } else {
            item_is_Login.visibility = View.GONE
            tv_logout.visibility = View.GONE
            behavior!!.setDragCallback(object : DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            })
        }
    }

    override fun setupObservers() {
        viewModel.meItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val meItem = it.result
                    id_personal.text = meItem.friendlyName.toString()

                    meItem.expiryDate?.let { date ->
                        tv_expiry_date.visibility = View.VISIBLE
                        tv_expiry_date.text = getString(
                            R.string.vip_expiry_date,
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).format(date)
                        )
                    }

//                    //TODO: 目前先不判斷是否有驗證過
////                    takeUnless { meItem.isEmailConfirmed == true }?.run {
////                        (requireActivity() as MainActivity).showEmailConfirmDialog()
////                    }
                    viewModel.loadImage(meItem.avatarAttachmentId, avatar, LoadImageType.AVATAR)
                }
                is Error -> onApiError(it.throwable)
            }
        })
//
        viewModel.apiSignOut.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {

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

                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.totalUnreadResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {

                    mainViewModel?.refreshBottomNavigationBadge?.value = it.result
                }
                is Error -> onApiError(it.throwable)
            }
        })
    }

    override fun setupListeners() {

        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            onBackPressed = {
                mainViewModel?.changeNavigationPosition?.value = R.id.navigation_mimi
            }
        )

        View.OnClickListener { buttonView ->
            when (buttonView.id) {
                R.id.tv_topup -> mainViewModel?.changeNavigationPosition?.value =
                    R.id.navigation_topup
                R.id.tv_follow -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myFollowFragment))
                R.id.tv_topup_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_orderFragment))
//                R.id.tv_chat_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_chatHistoryFragment))
                R.id.tv_my_post -> findNavController().navigate(R.id.action_personalFragment_to_myPostFragment)
//                R.id.tv_exchange -> navigateTo(
//                    NavigateItem.Destination(R.id.action_to_settingFragment)
//                )
                R.id.tv_old_driver -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(viewModel.getOldDriverUrl())
                    startActivity(intent)
                }
                R.id.tv_logout -> {
                    Glide.with(this).clear(avatar)
                    Glide.with(this).load(R.drawable.default_profile_picture).into(avatar)
                    viewModel.signOut()
//                    NavigateItem.Destination(
//                        R.id.action_personalFragment_to_loginFragment,
//                        LoginFragment.createBundle(TYPE_LOGIN)
//                    )
                }
//                R.id.tv_login -> navigateTo(
//                    NavigateItem.Destination(
//                        R.id.action_personalFragment_to_loginFragment,
//                        LoginFragment.createBundle(TYPE_LOGIN)
//                    )
//                )
                R.id.tv_register -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_personalFragment_to_loginFragment,
                        LoginFragment.createBundle(TYPE_REGISTER)
                    )
                )
            }
        }.also {
//            tv_topup.setOnClickListener(it)
            tv_follow.setOnClickListener(it)
//            tv_topup_history.setOnClickListener(it)
//            tv_chat_history.setOnClickListener(it)
            tv_my_post.setOnClickListener(it)
            setting.setOnClickListener(it)
            tv_old_driver.setOnClickListener(it)
            tv_logout.setOnClickListener(it)
//            tv_login.setOnClickListener(it)
//            tv_register.setOnClickListener(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getTotalUnread()
    }
}