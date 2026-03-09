package com.example.extsecure.broadcast

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

object NetworkUtil {

    /**
     * Registers a default network callback and returns a cleanup lambda
     * that unregisters it. Call the returned lambda in onDestroy().
     */
    fun observeNetwork(context: Context, onChange: (Boolean) -> Unit): () -> Unit {

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

        return { cm.unregisterNetworkCallback(callback) }
    }
}