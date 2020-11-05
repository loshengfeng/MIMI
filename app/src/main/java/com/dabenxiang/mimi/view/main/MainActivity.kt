package com.dabenxiang.mimi.view.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.*
import com.dabenxiang.mimi.callback.NetworkCallback
import com.dabenxiang.mimi.callback.NetworkCallbackListener
import com.dabenxiang.mimi.extension.setupWithNavController
import com.dabenxiang.mimi.extension.switchTab
import com.dabenxiang.mimi.model.api.ApiResult.*
import com.dabenxiang.mimi.model.api.vo.BaseMemberPostItem
import com.dabenxiang.mimi.model.api.vo.MemberPostItem
import com.dabenxiang.mimi.model.api.vo.MembersPostCommentItem
import com.dabenxiang.mimi.model.enums.BottomNavType
import com.dabenxiang.mimi.model.vo.StatusItem
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.view.base.BaseViewModel.Companion.POP_HINT_ANIM_TIME
import com.dabenxiang.mimi.view.dialog.GeneralDialog
import com.dabenxiang.mimi.view.dialog.GeneralDialogData
import com.dabenxiang.mimi.view.dialog.ReportDialogFragment
import com.dabenxiang.mimi.view.dialog.dailycheckin.DailyCheckInDialogFragment
import com.dabenxiang.mimi.view.dialog.show
import com.dabenxiang.mimi.view.home.AdultHomeFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.login.LoginFragment
import com.dabenxiang.mimi.view.player.ui.PlayerFragment.Companion.KEY_DEST_ID
import com.dabenxiang.mimi.widget.utility.FileUtil.deleteExternalFile
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*

class MainActivity : BaseActivity(), InteractionListener {

    private val viewModel: MainViewModel by viewModels()

    private val badgeViewMap = mutableMapOf<BottomNavType, View>()

    private val networkCallbackListener = object : NetworkCallbackListener {
        override fun onLost() {
            Timber.d("network disconnect")
            showCheckNetworkDialog()
        }
    }

    private val networkCallback = NetworkCallback(networkCallbackListener)

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBottomNavigationBar()

        viewModel.postReportResult.observe(this, Observer {
            when (it) {
                is Empty -> {
                    GeneralUtils.showToast(this, getString(R.string.report_success))
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.checkStatusResult.observe(this, Observer {
            when (it) {
                is Success -> {
                    when (it.result.status) {
                        StatusItem.NOT_LOGIN -> showNotLoginDialog()
                        StatusItem.LOGIN_BUT_EMAIL_NOT_CONFIRMED -> showEmailConfirmDialog()
                        StatusItem.LOGIN_AND_EMAIL_CONFIRMED -> it.result.onLoginAndEmailConfirmed()
                    }
                }
                is Error -> Timber.e(it.throwable)
            }
        })

        viewModel.totalUnreadResult.observe(this, Observer {
            when (it) {
                is Success -> {
                    refreshBottomNavigationBadge(it.result)
                }
            }
        })

        viewModel.dailyCheckInItem.observe(this, Observer {
            DailyCheckInDialogFragment.newInstance().show(
                supportFragmentManager,
                DailyCheckInDialogFragment::class.simpleName
            )
        })

        viewModel.switchTab.observe(this, {
            if (it >= 0) {

                bottom_navigation.switchTab(it)
            }
        })

        viewModel.showCopyHint.observe(this, {
            if (it.isNotBlank()) {
                tv_pop_hint.visibility = View.VISIBLE
                tv_pop_hint.text = it
                tv_pop_hint?.animate()?.alpha(1.0f)?.duration = POP_HINT_ANIM_TIME
            } else {
                tv_pop_hint?.animate()?.alpha(0.0f)?.duration = POP_HINT_ANIM_TIME
            }
        })

        viewModel.getTotalUnread()
    }

    override fun onResume() {
        super.onResume()
        registerNetworkCallback(App.applicationContext(), networkCallback)
    }

    override fun onPause() {
        super.onPause()
        unregisterNetworkCallback(App.applicationContext(), networkCallback)
    }

    private fun addBadgeView(bottomNavType: BottomNavType) {
        val menuView =
            bottom_navigation.getChildAt(0) as BottomNavigationMenuView
        val itemView = menuView.getChildAt(bottomNavType.value) as BottomNavigationItemView
        val notificationBadge = LayoutInflater.from(this)
            .inflate(R.layout.custom_menu_badge, menuView, false)
        badgeViewMap[bottomNavType] = notificationBadge
        itemView.addView(notificationBadge)
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        bottom_navigation.itemIconTintList = null

        val navGraphIds = listOf(
            R.navigation.navigation_adult,
            R.navigation.navigation_topup,
            R.navigation.navigation_favorite,
            R.navigation.navigation_personal
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent,
            domainManager = viewModel.domainManager,
            accountManager = viewModel.accountManager,
            onEmailUnconfirmed = { showEmailConfirmDialog() }
        )

        controller.observe(this, Observer {
            val isAdult =
                when (it.graph.id) {
                    R.id.navigation_adult -> true
                    else -> false
                }
            viewModel.setAdultMode(isAdult)
            setUiMode(isAdult)
        })

        for (type in BottomNavType.values()) {
            addBadgeView(type)
        }
    }

    private fun setUiMode(isAdult: Boolean) {
//        if (isAdult) {
//            window?.statusBarColor = getColor(R.color.adult_color_status_bar)
//            bottom_navigation.setBackgroundColor(getColor(R.color.adult_color_status_bar))
//            bottom_navigation.itemTextColor =
//                resources.getColorStateList(R.color.bottom_nav_adult_text_selector, null)
//        } else {
        window?.statusBarColor = getColor(R.color.normal_color_status_bar)
        bottom_navigation.background = getDrawable(R.drawable.bg_gray_2_top_line)
        bottom_navigation.itemTextColor =
            resources.getColorStateList(R.color.bottom_nav_normal_text_selector, null)
//        }
    }

    override fun changeNavigationPosition(index: Int) {
        Timber.i("changeNavigationPosition")
        bottom_navigation.selectedItemId = index
    }

    override fun setAdult(isAdult: Boolean) {
        viewModel.setAdultMode(isAdult)
        setUiMode(isAdult)
    }

    override fun refreshBottomNavigationBadge(unreadCount: Int) {
        val visibility = takeIf { unreadCount > 0 }?.let { View.VISIBLE } ?: let { View.GONE }
        badgeViewMap[BottomNavType.TOPUP]?.visibility = visibility
        badgeViewMap[BottomNavType.PERSONAL]?.visibility = visibility
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        val fragmentName =
            supportFragmentManager.fragments[0].findNavController().currentDestination?.displayName?.substringAfter(
                "/"
            ).toString()
        // 判斷當前的頁面是停留在 homeFragment，顯示退出 app 訊息
        if (fragmentName.toLowerCase(Locale.getDefault()) ==
            AdultHomeFragment::class.java.simpleName.toLowerCase(Locale.getDefault())
        ) {
            if (!viewModel.needCloseApp) {
                viewModel.startBackExitAppTimer()
                GeneralUtils.showToast(this, getString(R.string.press_again_exit))
            } else {
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("onNewIntent intent$intent")
        // this is for Package Installer activity callback method
        val extras = intent?.extras
        if (PACKAGE_INSTALLED_ACTION.equals(intent?.action)) {
            var message = extras?.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)
            when (extras?.getInt(PackageInstaller.EXTRA_STATUS)) {
                // to call installer dialog
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    Timber.d("STATUS_PENDING_USER_ACTION")
                    val confirmIntent = extras.get(Intent.EXTRA_INTENT) as Intent
                    startActivity(confirmIntent)
                }
                // self-update success
                PackageInstaller.STATUS_SUCCESS -> {
                    Timber.d("STATUS_SUCCESS")
                    // install success
                    @Suppress(
                        "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
                        "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
                    )
                    deleteExternalFile(App.applicationContext())

                }
                // the user confirm cancel button or some error happen.
                PackageInstaller.STATUS_FAILURE,
                PackageInstaller.STATUS_FAILURE_ABORTED,
                PackageInstaller.STATUS_FAILURE_BLOCKED,
                PackageInstaller.STATUS_FAILURE_CONFLICT,
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                PackageInstaller.STATUS_FAILURE_INVALID,
                PackageInstaller.STATUS_FAILURE_STORAGE -> {
                    Timber.d("STATUS_FAILURE ${extras.getInt(PackageInstaller.EXTRA_STATUS)}")
                    Timber.d("STATUS_FAILURE MESSAGE ${extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)}")
                    Toast.makeText(
                        this,
                        getString(R.string.install_apk_failed_alert),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    Timber.d("unrecognized status received from installer")
                }
            }
        } else if (NAVIGATE_TO_ACTION == intent?.action) {
            val dest = intent.getIntExtra(KEY_DEST_ID, 0)
            if (dest != 0)
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(dest, extras)
        } else if (NAVIGATE_TO_TOPUP_ACTION == intent?.action) {
            bottom_navigation.switchTab(1)
        }
    }

    private fun deepLinkTo(
        activity: Class<out Activity>,
        navGraphId: Int,
        destId: Int,
        bundle: Bundle?
    ) {
        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(activity)
            .setGraph(navGraphId)
            .setDestination(destId)
            .setArguments(bundle)
            .createPendingIntent()

        pendingIntent.send()
    }

    private var reportDialog: ReportDialogFragment? = null
    fun showReportDialog(
        item: BaseMemberPostItem,
        postItem: MemberPostItem? = null,
        isComment: Boolean? = false
    ) {
        reportDialog =
            ReportDialogFragment.newInstance(item, onReportDialogListener, postItem, isComment)
                .also {
                    it.show(supportFragmentManager, ReportDialogFragment::class.java.simpleName)
                }
    }

    private val onReportDialogListener = object : ReportDialogFragment.OnReportDialogListener {
        override fun onSend(item: BaseMemberPostItem, content: String, postItem: MemberPostItem?) {
            if (TextUtils.isEmpty(content)) {
                GeneralUtils.showToast(App.applicationContext(), getString(R.string.report_error))
            } else {
                reportDialog?.dismiss()
                when (item) {
                    is MemberPostItem -> viewModel.sendPostReport(item, content)
                    else -> {
                        postItem?.also {
                            viewModel.sendCommentPostReport(
                                it,
                                (item as MembersPostCommentItem),
                                content
                            )
                        }
                    }
                }
            }
        }

        override fun onCancel() {
            reportDialog?.dismiss()
        }
    }

    fun showEmailConfirmDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_email_not_confirmed_title,
                message = getString(R.string.error_email_not_confirmed_msg),
                messageIcon = R.drawable.ico_email,
                firstBtn = getString(R.string.verify_later),
                secondBtn = getString(R.string.verify_immediately),
                secondBlock = {
                    Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(R.id.action_to_settingFragment)
                }
            )
        ).show(supportFragmentManager)
    }

    fun showNotLoginDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.login_yet,
                message = getString(R.string.login_message),
                messageIcon = R.drawable.ico_default_photo,
                firstBtn = getString(R.string.btn_register),
                secondBtn = getString(R.string.btn_login),
                firstBlock = {
                    val bundle = Bundle()
                    bundle.putInt(LoginFragment.KEY_TYPE, LoginFragment.TYPE_REGISTER)
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                        R.id.action_to_loginFragment,
                        bundle
                    )
                },
                secondBlock = {
                    val bundle = Bundle()
                    bundle.putInt(LoginFragment.KEY_TYPE, LoginFragment.TYPE_LOGIN)
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                        R.id.action_to_loginFragment,
                        bundle
                    )
                },
                closeBlock = {
                    Timber.d("close!")
                }
            )
        ).show(supportFragmentManager)
    }

    fun showCheckNetworkDialog() {
        GeneralDialog.newInstance(
            GeneralDialogData(
                titleRes = R.string.error_device_binding_title,
                message = getString(R.string.server_error),
                messageIcon = R.drawable.ico_default_photo,
                secondBtn = getString(R.string.btn_close)
            )
        ).show(supportFragmentManager)
    }

}