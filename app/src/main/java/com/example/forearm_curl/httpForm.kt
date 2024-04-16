package com.example.forearm_curl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CommandFragment : Fragment() {

    private lateinit var txtCommandName: TextView
    private lateinit var editTextCommand: EditText

    private lateinit var btnSendReq: Button
    private lateinit var httpMethodSelector: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_http_form, container, false)

        editTextCommand = view.findViewById(R.id.editTextCommand)
        httpMethodSelector = view.findViewById(R.id.httpMethodSelector)
        btnSendReq = view.findViewById(R.id.btnSendReq)

        val httpMethods = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf("GET", "POST"))

        httpMethods.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        httpMethodSelector.adapter = httpMethods

        arguments?.let { args ->
            val commandName = args.getString("commandName")
            commandName?.let {
                txtCommandName.text = it
            }
        }

        return view
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

}
