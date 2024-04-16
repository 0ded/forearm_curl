package com.example.forearm_curl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*

class PingResultsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var refreshButton: Button
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_ping_results, container, false)

        listView = view.findViewById(R.id.listPingResults)
        refreshButton = view.findViewById(R.id.btnRefresh)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        listView.adapter = adapter

        refreshButton.setOnClickListener {
            refreshPingResults()
        }

        return view
    }

    private fun refreshPingResults() {
        // Clear previous results
        adapter.clear()

        // Perform ICMP ping for example IP addresses
        val addressesToPing = listOf(
            "192.168.0.1",
            "192.168.0.43",
            "192.168.0.10",
            "192.168.0.254"
        ) // Example IP addresses

        CoroutineScope(Dispatchers.IO).launch {
            val reachables = withContext(Dispatchers.IO) {
                pingRange("192.168.0.9", "192.168.0.11")
            }
            for (address in reachables) {
                val reachable = withContext(Dispatchers.IO) {
                    PingUtil.ping(address)
                }

                // Update UI on Main dispatcher
                withContext(Dispatchers.Main) {
                    val result = "$address is ${if (reachable) "reachable" else "unreachable"}"
                    adapter.add(result)
                }
            }
        }
    }


    external fun pingRange(startAddr: String, endAddr: String): ArrayList<String>

    companion object {
        // Used to load the 'forearm_curl' library on application startup.
        init {
            System.loadLibrary("forearm_curl")
        }
    }
}
