package com.example.extsecure.broadcast

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

object NetworkUtil {

    fun observeNetwork(context: Context, onChange: (Boolean) -> Unit) {

        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                onChange(true)
            }

            override fun onLost(network: Network) {
                onChange(false)
            }
        }

        cm.registerDefaultNetworkCallback(callback)
    }
}