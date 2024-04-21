package com.example.forearm_curl

import android.util.Log
import java.io.IOException
import java.net.InetAddress

object PingUtil {
    fun ping(targetAddress: String): Boolean {
        Log.d("t", targetAddress)
        return try {
            val inetAddress = InetAddress.getByName(targetAddress)
            inetAddress.isReachable(500) // Ping timeout in milliseconds
        } catch (e: IOException) {
            e.printStackTrace()
            e.message?.let { Log.d("t", it) }
            false
        }
    }
}
