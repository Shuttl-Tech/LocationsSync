package com.shuttl.location_pings

import android.content.Context
import android.net.ConnectivityManager

fun Context.isInternetConnected(): Boolean {
    val context = this
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNetworkInfo = connectivityManager?.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}