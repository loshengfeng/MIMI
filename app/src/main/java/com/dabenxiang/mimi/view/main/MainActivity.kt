package com.dabenxiang.mimi.view.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.extension.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBottomNavigationBar()

        /* 備案
        viewModel.enableNightMode.observe(this, Observer { isNight ->
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            val mode =
                if (isNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (delegate.localNightMode != mode) {
                delegate.localNightMode = mode
            }
        })
        */
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
            bottom_navigation.itemTextColor = resources.getColorStateList(R.color.adult_color_bottom_bar_item, null)
        } else {
            window?.statusBarColor = getColor(R.color.normal_color_status_bar)
            bottom_navigation.setBackgroundColor(getColor(R.color.normal_color_status_bar))
            bottom_navigation.itemTextColor = resources.getColorStateList(R.color.normal_color_bottom_bar_item, null)
        }
    }
}