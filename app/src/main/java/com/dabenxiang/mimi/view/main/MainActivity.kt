package com.dabenxiang.mimi.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseActivity
import com.dabenxiang.mimi.widget.utility.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : BaseActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBottomNavigationBar()

        viewModel.loadHomeCategories()

        viewModel.enableNightMode.observe(this, Observer { isNight ->
            //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            val mode = if (isNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (delegate.localNightMode != mode) {
                delegate.localNightMode = mode
            }
        })
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val navGraphIds = listOf(R.navigation.navigation_home, R.navigation.navigation_topup, R.navigation.navigation_favorite, R.navigation.navigation_personal)

        // Setup the bottom navigation view with a list of navigation graphs
        bottom_navigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )
    }
}