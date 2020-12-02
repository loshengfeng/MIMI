package com.dabenxiang.mimi.view.personal

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.view.topup.TopUpFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
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
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun initSettings() {
        super.initSettings()
        tv_version.text = BuildConfig.VERSION_NAME
        Glide.with(this).clear(avatar)
        layout_vip_unlimit.visibility = View.INVISIBLE
        layout_vip_unlimit_unlogin.visibility = View.INVISIBLE
        viewModel.getPostDetail()

        if (viewModel.isLogin()) {
            item_is_Login.visibility = View.VISIBLE
            layout_vip_unlimit.visibility = View.VISIBLE
            tv_logout.visibility = View.VISIBLE
            vip_buy.visibility = View.INVISIBLE
            layout_vip_unlimit_unlogin.visibility = View.INVISIBLE
            tv_expiry_date.visibility = View.VISIBLE
            img_arrow.visibility = View.VISIBLE
        } else {
            item_is_Login.visibility = View.GONE
            tv_logout.visibility = View.GONE
            id_personal.text = getString(R.string.identity)
            like_count.text = "0"
            fans_count.text = "0"
            follow_count.text = "0"
            vip_buy.visibility = View.VISIBLE
            layout_vip_unlimit_unlogin.visibility = View.VISIBLE
            tv_expiry_date.visibility = View.GONE
            img_arrow.visibility = View.INVISIBLE
            Glide.with(this).load(R.drawable.default_profile_picture).into(avatar)
        }
        layout_refresh.setOnRefreshListener {
            viewModel.getPostDetail()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setupObservers() {
        viewModel.showProgress.observe(this, Observer {
            layout_refresh.isRefreshing = it
        })

        viewModel.meItem.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Success -> {
                    val meItem = it.result
                    if (meItem.isSubscribed) {
                        layout_vip_unlimit.visibility = View.VISIBLE
                        layout_vip_unlimit_unlogin.visibility = View.INVISIBLE

                        video_long_count.text = getString(R.string.every_day_video_count_unlimit)
                        video_short_count.text = getString(R.string.every_day_video_count_unlimit)
                    } else {
                        layout_vip_unlimit.visibility = View.INVISIBLE
                        layout_vip_unlimit_unlogin.visibility = View.VISIBLE

                        meItem.videoCount?.let { count ->
                            meItem.videoCountLimit?.let { countLimit ->
                                video_long_count.text = "$count/$countLimit"
                            }
                        }
                        meItem.videoOnDemandCount?.let { count ->
                            meItem.videoOnDemandCountLimit?.let { countLimit ->
                                video_short_count.text = "$count/$countLimit"
                            }
                        }
                    }

                    meItem.friendlyName?.let {
                        id_personal.text = it
                    }
                    meItem.likes?.let { it ->
                        like_count.text = it.toString()
                    }
                    meItem.fans?.let { it ->
                        fans_count.text = it.toString()
                    }
                    meItem.follows?.let { it ->
                        follow_count.text = it.toString()
                    }

                    if (meItem.expiryDate == null) {
                        layout_vip_unlimit_unlogin.visibility = View.VISIBLE
                    } else {
                        meItem.expiryDate?.let { date ->
                            tv_expiry_date.text = getString(
                                R.string.deadline_vip,
                                SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                                ).format(date)
                            )
                        }
                    }
                    viewModel.loadImage(meItem.avatarAttachmentId, avatar, LoadImageType.AVATAR)
                }
                is Error -> onApiError(it.throwable)
            }
        })
//
        viewModel.apiSignOut.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Empty -> {
                    Glide.with(this).clear(avatar)
                    layout_vip_unlimit_unlogin.visibility = View.VISIBLE
                    item_is_Login.visibility = View.GONE
                    tv_logout.visibility = View.GONE
                    id_personal.text = getString(R.string.identity)
                    like_count.text = "0"
                    fans_count.text = "0"
                    follow_count.text = "0"
                    Glide.with(this).load(R.drawable.default_profile_picture).into(avatar)
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

        viewModel.visibleSetting.observe(viewLifecycleOwner, Observer {
            if (it) {
                setting.visibility = View.VISIBLE
            } else {
                setting.visibility = View.GONE
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

                R.id.tv_favorite -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myCollectionFragment))

                R.id.follow_count -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myFollowFragment))
                R.id.follow -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myFollowFragment))


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
                }
                R.id.vippromote_now -> {
                    if (viewModel.isLogin()) {
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_inviteVipFragment,
                                null
                            )
                        )
                    } else {
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_personalFragment_to_loginFragment,
                                LoginFragment.createBundle(TYPE_LOGIN)
                            )
                        )
                    }
                }
                R.id.tv_register -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_personalFragment_to_loginFragment,
                        LoginFragment.createBundle(TYPE_REGISTER)
                    )
                )

                R.id.layout_vip_unlimit -> navigateTo(
                    NavigateItem.Destination(
                        R.id.action_personalFragment_to_topupFragment,
                        TopUpFragment.createBundle(this::class.java.simpleName)
                    )
                )

                R.id.layout_vip_unlimit_unlogin -> {
                    if (viewModel.isLogin()) {
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_personalFragment_to_topupFragment,
                                TopUpFragment.createBundle(this::class.java.simpleName)
                            )
                        )
                    } else {
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_personalFragment_to_loginFragment,
                                LoginFragment.createBundle(TYPE_LOGIN)
                            )
                        )
                    }
                }

                R.id.fans_count -> navigateTo(NavigateItem.Destination(R.id.action_to_fanslistFragment))
                R.id.fans -> navigateTo(NavigateItem.Destination(R.id.action_to_fanslistFragment))

                R.id.like_count -> navigateTo(NavigateItem.Destination(R.id.action_to_likelistFragment))
                R.id.like -> navigateTo(NavigateItem.Destination(R.id.action_to_likelistFragment))
                R.id.setting -> navigateTo(NavigateItem.Destination(R.id.action_to_settingFragment))
            }
        }.also {
            layout_vip_unlimit_unlogin.setOnClickListener(it)
            layout_vip_unlimit.setOnClickListener(it)

            tv_my_post.setOnClickListener(it)
            setting.setOnClickListener(it)
            tv_old_driver.setOnClickListener(it)
            tv_logout.setOnClickListener(it)
            vippromote_now.setOnClickListener(it)

            like_count.setOnClickListener(it)
            like.setOnClickListener(it)

            fans_count.setOnClickListener(it)
            fans.setOnClickListener(it)


            follow_count.setOnClickListener(it)
            follow.setOnClickListener(it)

            tv_favorite.setOnClickListener(it)

            tv_topup_history.setOnClickListener(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getTotalUnread()
    }
}