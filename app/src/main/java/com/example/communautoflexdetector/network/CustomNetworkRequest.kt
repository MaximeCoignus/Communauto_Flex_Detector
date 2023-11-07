package com.example.communautoflexdetector.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.communautoflexdetector.MainActivity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class CustomNetworkRequest(
    button: Button,
    textView: TextView
): ConnectivityManager.NetworkCallback() {
    private val button = button
    private val textView = textView

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        MainScope().launch {
            try {
                MainActivity().runOnUiThread {
                    button.isEnabled = true
                    textView.text = null
                }
            } catch (e: Exception) {
                Log.d("CustomNetworkRequest", e.toString())
            }
        }
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        MainScope().launch {
            try {
                MainActivity().runOnUiThread {
                    button.isEnabled = false
                    textView.text = "Please, check your internet connection!"
                }
            } catch (e: Exception) {
                Log.d("CustomNetworkRequest", e.toString())
            }
        }
    }

    fun createNetworkRequest(context: Context) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, this)
    }
}