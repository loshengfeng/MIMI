package com.dabenxiang.mimi.view.base

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {

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

        setContentView(getLayoutId())
    }

    open fun getLayoutId(): Int {
        return -1
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context): Context {
        val resources = context.resources
        val configuration = resources.configuration
        if(configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f
        }

        return context.createConfigurationContext(configuration)
    }

    override fun attachBaseContext(newBase: Context?) {
        val context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 8.0需要使用createConfigurationContext處理
            updateResources(newBase!!)
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (newConfig.fontScale != 1f) {
            resources
        }
        super.onConfigurationChanged(newConfig)
    }

    fun registerNetworkCallback(context: Context, networkCallback: ConnectivityManager.NetworkCallback) {
        Timber.d("register network callback")
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        val request = builder.build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun unregisterNetworkCallback(context: Context, networkCallback: ConnectivityManager.NetworkCallback){
        Timber.d("unregister network callback")
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
