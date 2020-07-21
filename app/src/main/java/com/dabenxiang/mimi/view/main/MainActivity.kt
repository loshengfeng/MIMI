package com.dabenxiang.mimi.view.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.extension.setupWithNavController
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.view.home.HomeFragment
import com.dabenxiang.mimi.view.listener.InteractionListener
import com.dabenxiang.mimi.widget.utility.GeneralUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity(), InteractionListener {

    private val viewModel: MainViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        } else
            super.onBackPressed()
    }
}