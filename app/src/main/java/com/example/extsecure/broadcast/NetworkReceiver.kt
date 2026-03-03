package com.example.extsecure.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkReceiver : BroadcastReceiver() {

    var onNetworkChanged: ((isConnected: Boolean) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        onNetworkChanged?.invoke(isNetworkAvailable(context))
    }

    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}