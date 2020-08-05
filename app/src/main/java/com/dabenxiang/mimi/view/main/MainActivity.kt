package com.dabenxiang.mimi.view.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.App
import com.dabenxiang.mimi.PACKAGE_INSTALLED_ACTION
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setupWithNavController
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.view.home.HomeFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.view.search.video.SearchVideoFragment
import com.dabenxiang.mimi.widget.utility.FileUtil.deleteExternalFile
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.*

class MainActivity : BaseActivity(), InteractionListener {

    private val viewModel: MainViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("MainActivity onCreate")
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        bottom_navigation.itemIconTintList = null

        val navGraphIds = listOf(
            R.navigation.navigation_home,
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
            intent = intent
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
    }

    private fun setUiMode(isAdult: Boolean) {
        if (isAdult) {
            window?.statusBarColor = getColor(R.color.adult_color_status_bar)
            bottom_navigation.setBackgroundColor(getColor(R.color.adult_color_status_bar))
            bottom_navigation.itemTextColor =
                resources.getColorStateList(R.color.color_white_1_30, null)
        } else {
            window?.statusBarColor = getColor(R.color.normal_color_status_bar)
            bottom_navigation.setBackgroundColor(getColor(R.color.normal_color_status_bar))
            bottom_navigation.itemTextColor =
                resources.getColorStateList(R.color.normal_color_bottom_bar_item, null)
        }
    }

    override fun changeNavigationPosition(index: Int) {
        bottom_navigation.selectedItemId = index
    }

    override fun setAdult(isAdult: Boolean) {
        viewModel.setAdultMode(isAdult)
        setUiMode(isAdult)
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        // 判斷當前的頁面是停留在 homeFragment，顯示退出 app 訊息
        if (supportFragmentManager.fragments[0].findNavController().currentDestination?.displayName?.substringAfter("/").toString().toLowerCase(Locale.getDefault()) == HomeFragment::class.java.simpleName.toLowerCase(Locale.getDefault())) {
            if (!viewModel.needCloseApp) {
                viewModel.startBackExitAppTimer()
                GeneralUtils.showToast(this, getString(R.string.press_again_exit))
            } else {
                finish()
            }
        } else if(supportFragmentManager.fragments[0].findNavController().currentDestination?.displayName?.substringAfter("/").toString().toLowerCase(Locale.getDefault()) == SearchVideoFragment::class.java.simpleName.toLowerCase(Locale.getDefault())) {
            if(viewModel.isFromPlayer)
                deepLinkTo(MainActivity::class.java, R.navigation.navigation_home, R.id.homeFragment, null)
            else
                super.onBackPressed()
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
        }
    }

    private fun deepLinkTo(activity: Class<out Activity>, navGraphId: Int, destId: Int, bundle: Bundle?){
        val pendingIntent = NavDeepLinkBuilder(this)
                .setComponentName(activity)
                .setGraph(navGraphId)
                .setDestination(destId)
                .setArguments(bundle)
                .createPendingIntent()

        pendingIntent.send()
    }

}