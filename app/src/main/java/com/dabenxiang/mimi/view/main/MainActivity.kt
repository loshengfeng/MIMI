package com.dabenxiang.mimi.view.main

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import com.dabenxiang.mimi.R
import com.dabenxiang.mimi.view.base.BaseActivity


class MainActivity : BaseActivity() {

//    private val viewModel by viewModel<MainViewModel>()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        windowManager?.let {
            val metrics = DisplayMetrics()
            it.defaultDisplay.getMetrics(metrics)
            metrics.density = metrics.ydpi / 160
            metrics.densityDpi = metrics.ydpi.toInt()
            metrics.scaledDensity = metrics.density
            resources.configuration.densityDpi = metrics.densityDpi
            resources.configuration.fontScale = 1f
            baseContext.resources.updateConfiguration(resources.configuration, metrics)
        }
    }

    override fun getResources(): Resources {
        val overrideConfiguration = baseContext.resources.configuration
        if (overrideConfiguration.fontScale != 1f) {
            overrideConfiguration.fontScale = 1f
            val context = createConfigurationContext(overrideConfiguration)
            return context.resources
        }
        return super.getResources()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.fontScale != 1f) {
            resources
        }
        super.onConfigurationChanged(newConfig)
    }

}