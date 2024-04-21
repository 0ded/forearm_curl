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

class httpHub : AppCompatActivity() {
    private lateinit var txtCommandName: Any
    private lateinit var commandSidebar: ListView
    private lateinit var btnOpenCommandSidebar: Button
    private lateinit var btnOpenScanbar: Button
    
    private lateinit var addButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private val commandList = mutableListOf("Command 1", "Command 2", "Command 3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_hub)

        commandSidebar = findViewById(R.id.sidebar)
        btnOpenCommandSidebar = findViewById(R.id.btnToggleSidebar)
        drawerLayout = findViewById(R.id.drawer_layout)
        txtCommandName = findViewById(R.id.txtCommandName)
        btnOpenScanbar = findViewById(R.id.btnToggleScanbar)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, commandList)
        commandSidebar.adapter = adapter

        btnOpenCommandSidebar.setOnClickListener {
            drawerLayout.openDrawer(commandSidebar)
        }

        commandSidebar.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            drawerLayout.closeDrawer(commandSidebar)
            openCommandFragment(commandList[position])
        }

        btnOpenScanbar.setOnClickListener {
            openPingResultsFragment()
        }

       
        val inflater = layoutInflater
        val footerView = inflater.inflate(R.layout.sidebar_footer, commandSidebar, false)
        commandSidebar.addFooterView(footerView)

        val addButton = footerView.findViewById<Button>(R.id.btnAddCommand)
        addButton.setOnClickListener {
            addCommand()
            drawerLayout.closeDrawer(commandSidebar)
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

    private fun openPingResultsFragment() {
        val fragment = PingResultsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun addCommand() {
        commandList.add("New Command ${commandList.size + 1}")
        val adapter = (commandSidebar.adapter as HeaderViewListAdapter).wrappedAdapter as ArrayAdapter<*>
        adapter.notifyDataSetChanged()
    }

    fun sendCustomHttpRequest(url: String, method: String, headers: Map<String, String>?, requestBody: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method

       
        headers?.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

       
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
}