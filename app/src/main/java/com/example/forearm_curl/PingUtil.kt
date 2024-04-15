package com.example.forearm_curl

import java.io.IOException
import java.net.InetAddress

object PingUtil {
    fun ping(targetAddress: String): Boolean {
        return try {
            val inetAddress = InetAddress.getByName(targetAddress)
            inetAddress.isReachable(5000) // Ping timeout in milliseconds
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}
