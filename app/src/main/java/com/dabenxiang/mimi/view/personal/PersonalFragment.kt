package com.dabenxiang.mimi.view.personal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.dabenxiang.mimi.BuildConfig
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.MeItem
import com.dabenxiang.mimi.model.enums.LoadImageType
import com.dabenxiang.mimi.view.base.BaseFragment
import com.dabenxiang.mimi.view.base.NavigateItem
import com.dabenxiang.mimi.view.dialog.CheckUpdateMessageAlertDialog
import com.dabenxiang.mimi.view.listener.OnSimpleDialogListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_LOGIN
import com.dabenxiang.mimi.view.login.LoginFragment.Companion.TYPE_REGISTER
import com.dabenxiang.mimi.view.topup.TopUpFragment
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.fragment_personal.*
import kotlinx.android.synthetic.main.item_personal_is_login.*
import timber.log.Timber
import tw.gov.president.manager.submanager.update.callback.DownloadProgressCallback
import tw.gov.president.manager.submanager.update.data.VersionStatus
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

    @SuppressLint("SetTextI18n")
    override fun initSettings() {
        super.initSettings()
        tv_version.text = getString(R.string.personal_version_v_char) + BuildConfig.VERSION_NAME
        initUi()
        viewModel.getMemberInfo()

        layout_refresh.setOnRefreshListener {
            viewModel.getMemberInfo()
        }
    }

    override fun setupFirstTime() {
        super.setupFirstTime()
        viewModel.versionStatus.observe(this, Observer {
            Timber.i("versionStatus=$it isVersionChecked=${mainViewModel?.isVersionChecked}")
            tv_version_check_text.text = getString(R.string.personal_check_update)
            tv_version_check_text.setTextColor(requireContext().getColor(R.color.color_blue_2))
            tv_version_check_text.isClickable = true
            progress_check_update.visibility = View.GONE
            when (it) {
                VersionStatus.UPDATE, VersionStatus.FORCE_UPDATE -> updateDialog(requireContext())
                else -> {
                    Toast.makeText(requireContext(), getString(R.string.no_need_update_title), Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun initUi() {
        Glide.with(this).load(R.drawable.default_profile_picture).into(avatar)
        layout_vip_unlimit.visibility = View.INVISIBLE
        layout_vip_unlimit_unlogin.visibility = View.VISIBLE
        vip_buy.visibility = View.VISIBLE
        tv_expiry_date.visibility = View.GONE
        img_arrow.visibility = View.INVISIBLE
        if (viewModel.isLogin()) {
            item_is_Login.visibility = View.VISIBLE
            tv_logout.visibility = View.VISIBLE
        } else {
            item_is_Login.visibility = View.GONE
            tv_logout.visibility = View.GONE
        }
    }

    private fun updateUi(profile: MeItem) {
        if (viewModel.isLogin()) {
            id_personal.text = profile.friendlyName
            like_count.text = profile.likes.toString()
            fans_count.text = profile.fans.toString()
            follow_count.text = profile.follows.toString()
            viewModel.loadImage(profile.avatarAttachmentId, avatar, LoadImageType.AVATAR)
        } else {
            id_personal.text = getString(R.string.identity)
            like_count.text = "0"
            fans_count.text = "0"
            follow_count.text = "0"
            Glide.with(this).load(R.drawable.default_profile_picture).into(avatar)
        }

        if (viewModel.isLogin() && profile.isSubscribed) {
            layout_vip_unlimit.visibility = View.VISIBLE
            layout_vip_unlimit_unlogin.visibility = View.INVISIBLE
            video_long_count.text = getString(R.string.every_day_video_count_unlimit)
            video_short_count.text = getString(R.string.every_day_video_count_unlimit)
            tv_expiry_date.text = getString(
                R.string.deadline_vip,
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(profile.expiryDate ?: Date())
            )
            vip_buy.visibility = View.GONE
            tv_expiry_date.visibility = View.VISIBLE
            img_arrow.visibility = View.VISIBLE
        } else {
            layout_vip_unlimit.visibility = View.INVISIBLE
            layout_vip_unlimit_unlogin.visibility = View.VISIBLE
            profile.videoOnDemandCount.let { count ->
                profile.videoOnDemandCountLimit.let { countLimit ->

                    val longCount = StringBuilder()
                        .append(GeneralUtils.getMaxCount(count ?: 0))
                        .append("/")
                        .append(GeneralUtils.getMaxCount(countLimit ?: 0))
                        .toString()

                    video_long_count.text = longCount
                }
            }
            profile.videoCount.let { count ->
                profile.videoCountLimit.let { countLimit ->
                    val shortCount = StringBuilder()
                        .append(GeneralUtils.getMaxCount(count ?: 0))
                        .append("/")
                        .append(GeneralUtils.getMaxCount(countLimit ?: 0))
                        .toString()

                    video_short_count.text = shortCount
                }
            }
            vip_buy.visibility = View.VISIBLE
            tv_expiry_date.visibility = View.GONE
            img_arrow.visibility = View.INVISIBLE
        }
    }

    private fun likeClick() =
        checkStatus { navigateTo(NavigateItem.Destination(R.id.action_to_likelistFragment)) }

    private fun followClick() =
        checkStatus { navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myFollowFragment)) }

    private fun fansClick() =
        checkStatus { navigateTo(NavigateItem.Destination(R.id.action_to_fanslistFragment)) }

    @SuppressLint("SetTextI18n")
    override fun setupObservers() {
        viewModel.showProgress.observe(this, {
            layout_refresh.isRefreshing = it
        })

        viewModel.meItem.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> layout_refresh.isRefreshing = true
                is Loaded -> layout_refresh.isRefreshing = false
                is Success -> updateUi(it.result)
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.apiSignOut.observe(viewLifecycleOwner, {
            when (it) {
                is Loading -> layout_refresh.isRefreshing = true
                is Loaded -> layout_refresh.isRefreshing = false
                is Empty -> {
                    initUi()
                    scroll_view.smoothScrollTo(0, 0)
                    viewModel.getMemberInfo()
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.unreadResult.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> {
                    tv_new.visibility = if (it.result == 0) View.INVISIBLE else View.VISIBLE
                }
                is Error -> onApiError(it.throwable)
            }
        })

        viewModel.totalUnreadResult.observe(viewLifecycleOwner, {
            when (it) {
                is Success -> {
                    iv_new.visibility = if (it.result == 0) View.INVISIBLE else View.VISIBLE
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

                R.id.tv_favorite -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myCollectionFragment))

                R.id.tv_topup_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_orderFragment))
//                R.id.tv_chat_history -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_chatHistoryFragment))
                R.id.tv_my_post -> navigateTo(NavigateItem.Destination(R.id.action_personalFragment_to_myPostFragment))
//                R.id.tv_exchange -> navigateTo(
//                    NavigateItem.Destination(R.id.action_to_settingFragment)
//                )
                R.id.tv_old_driver -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(viewModel.getOldDriverUrl())
                    startActivity(intent)
                }
                R.id.tv_version_check_text -> {
                    tv_version_check_text.text = getString(R.string.personal_checking_update)
                    tv_version_check_text.setTextColor(requireContext().getColor(R.color.color_black_1))
                    tv_version_check_text.isClickable = false
                    progress_check_update.visibility = View.VISIBLE
                    viewModel.checkVersion()
                }
                R.id.tv_logout -> {
                    Glide.with(this).clear(avatar)
                    Glide.with(this).load(R.drawable.default_profile_picture).into(avatar)
                    viewModel.signOut()
                }
                R.id.vippromote_now -> {
                    checkStatus {
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_to_inviteVipFragment,
                                null
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

                R.id.like_count -> likeClick()
                R.id.like -> likeClick()

                R.id.fans_count -> fansClick()
                R.id.fans -> fansClick()

                R.id.follow_count -> followClick()
                R.id.follow -> followClick()

                R.id.setting -> {
                    if (viewModel.isLogin()) {
                        navigateTo(NavigateItem.Destination(R.id.action_to_settingFragment))
                    } else {
                        navigateTo(
                            NavigateItem.Destination(
                                R.id.action_personalFragment_to_loginFragment,
                                LoginFragment.createBundle(TYPE_LOGIN)
                            )
                        )
                    }
                }
            }
        }.also {
            layout_vip_unlimit_unlogin.setOnClickListener(it)
            layout_vip_unlimit.setOnClickListener(it)

            tv_my_post.setOnClickListener(it)
            setting.setOnClickListener(it)
            tv_old_driver.setOnClickListener(it)
            tv_version_check_text.setOnClickListener(it)
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

    private fun updateDialog(context: Context) {
        Timber.d("updateDialog")

        CheckUpdateMessageAlertDialog(
            context,
            {callback: DownloadProgressCallback -> viewModel.updateApp(callback)},
            object : OnSimpleDialogListener {
                override fun onConfirm() {
                    Timber.d("update")
                }

                override fun onCancel() {
                    Timber.d("close")
                }

            }
        ).show()
    }
}