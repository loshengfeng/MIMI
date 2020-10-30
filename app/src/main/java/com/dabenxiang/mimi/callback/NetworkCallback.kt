package com.dabenxiang.mimi.callback

import android.net.ConnectivityManager
import android.net.Network
import timber.log.Timber

class NetworkCallback(private val networkCallbackListener: NetworkCallbackListener) : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Timber.d("onAvailable")
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        Timber.d("onLosing")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Timber.d("onLost")
        networkCallbackListener.onLost()
    }
}