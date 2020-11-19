package com.dabenxiang.mimi.widget.utility

import android.content.Context
import android.net.*
import com.dabenxiang.mimi.callback.ConnectionStateListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber


private const val TAG = "ConnectionStateMonitor"

class ConnectionStateMonitor(val context: Context, val listener: ConnectionStateListener? = null) : ConnectivityManager.NetworkCallback(), KoinComponent {

    private var networkRequest: NetworkRequest? = null

    init {
        networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
    }

    fun enable() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkRequest?.let { connectivityManager.registerNetworkCallback(it, this) }
    }

    // Likewise, you can have a disable method that simply calls ConnectivityManager.unregisterNetworkCallback(NetworkCallback) too.

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        super.onBlockedStatusChanged(network, blocked)
        Timber.d("$TAG onBlockedStatusChanged netWork = $network, blocked = $blocked ")
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        Timber.d("$TAG onCapabilitiesChanged netWork = $network, networkCapabilities = $networkCapabilities ")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Timber.d("$TAG onLost netWork = $network")
        listener?.disconnect()
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties)
        Timber.d("$TAG onLinkPropertiesChanged netWork = $network, linkProperties = ${linkProperties}")
    }

    override fun onUnavailable() {
        super.onUnavailable()
        Timber.d("$TAG onUnavailable")
        listener?.disconnect()
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        Timber.d("$TAG onLosing network = ${network}, maxMsToLive = ${maxMsToLive} ")
        listener?.disconnect()
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Timber.d("$TAG onAvailable network = ${network}")
        listener?.connect()
    }
}
