package com.example.forearm_curl

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.HeaderViewListAdapter
import androidx.drawerlayout.widget.DrawerLayout
import android.util.Log

class httpHub : AppCompatActivity() {
    private lateinit var txtCommandName: Any
    private lateinit var sidebar: ListView
    private lateinit var toggleButton: Button
    
    private lateinit var addButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private val commandList = mutableListOf("Command 1", "Command 2", "Command 3") // Sample command list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_hub)

        sidebar = findViewById(R.id.sidebar)
        toggleButton = findViewById(R.id.btnToggleSidebar)
        drawerLayout = findViewById(R.id.drawer_layout)
        txtCommandName = findViewById(R.id.txtCommandName)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, commandList)
        sidebar.adapter = adapter

        toggleButton.setOnClickListener {
            drawerLayout.openDrawer(sidebar)
        }

        sidebar.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drawerLayout.closeDrawer(sidebar)
            openCommandFragment(commandList[position])
        }

        // Add button to add command to the sidebar
        val inflater = layoutInflater
        val footerView = inflater.inflate(R.layout.sidebar_footer, sidebar, false)
        sidebar.addFooterView(footerView)

        val addButton = footerView.findViewById<Button>(R.id.btnAddCommand)
        addButton.setOnClickListener {
            addCommand()
            drawerLayout.closeDrawer(sidebar) // Close sidebar after adding a command
        }
    }

    private fun openCommandFragment(commandName: String) {
        val fragment = CommandFragment()
        val args = Bundle()

        args.putString("commandName", commandName)
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun addCommand() {
        Log.d("Getting ips", "start")
        val ips = pingRange("192.168.0.1", "192.168.0.254")
        Log.d("Getting ips", "end")
        for (ip in ips){
            Log.d("Ping", "Reachable address: $ip")
            commandList.add(ip)
        }
        commandList.add("New Command ${commandList.size + 1}")
        val adapter = (sidebar.adapter as HeaderViewListAdapter).wrappedAdapter as ArrayAdapter<*>
        adapter.notifyDataSetChanged()
    }

    fun sendCustomHttpRequest(url: String, method: String, headers: Map<String, String>?, requestBody: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method

        // Set request headers if provided
        headers?.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        // Set request body if provided
        requestBody?.let {
            connection.doOutput = true
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(it)
            outputStream.flush()
            outputStream.close()
        }

        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage

        val response = StringBuilder()
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
        } else {
            response.append("Error: $responseCode - $responseMessage")
        }

        connection.disconnect()
        return response.toString()
    }

//    external fun pingRange(startAddr: String, endAddr: String): ArrayList<String>
//
//    companion object {
//        // Used to load the 'forearm_curl' library on application startup.
//        init {
//            System.loadLibrary("forearm_curl")
//        }
//    }
}