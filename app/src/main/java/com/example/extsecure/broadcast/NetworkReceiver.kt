package com.example.extsecure.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * A proper BroadcastReceiver that listens for connectivity changes.
 * Registered dynamically in MainActivity with IntentFilter(CONNECTIVITY_ACTION).
 */
class NetworkReceiver(
    private val onNetworkChanged: (Boolean) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = checkConnectivity(context)
        Log.d("NetworkReceiver", "Broadcast received — network connected: $isConnected")
        onNetworkChanged(isConnected)
    }

    companion object {
        fun checkConnectivity(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}

