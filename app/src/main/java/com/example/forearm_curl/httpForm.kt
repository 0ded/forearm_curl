package com.example.forearm_curl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.*

class CommandFragment : Fragment() {

    private lateinit var txtCommandName: EditText
    private lateinit var httpMethodSelector: Spinner
    private lateinit var editTextCommand: EditText
    private lateinit var dataAttached: EditText
    private lateinit var reqResult: EditText
    private lateinit var btnSendReq: Button
    private lateinit var btnCopyResult: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_http_form, container, false)

        txtCommandName = view.findViewById(R.id.commandName)
        httpMethodSelector = view.findViewById(R.id.httpMethodSelector)
        editTextCommand = view.findViewById(R.id.editTextCommand)
        dataAttached = view.findViewById(R.id.dataAttached)
        reqResult = view.findViewById(R.id.reqResult)
        btnSendReq = view.findViewById(R.id.btnSendReq)
        btnCopyResult = view.findViewById(R.id.btnCopyResult)

        val httpMethods = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            mutableListOf("GET", "POST"))

        httpMethods.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        httpMethodSelector.adapter = httpMethods

        arguments?.let { args ->
            val commandName = args.getString("commandName")
            commandName?.let {
                txtCommandName.setText(it)
            }
        }

        btnSendReq.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val url = editTextCommand.text.toString()
                val method = httpMethodSelector.selectedItem.toString()
                var data = dataAttached.text.toString()
                var response = ""
                if (method != "GET"){
                    response = withContext(Dispatchers.IO) {
                        sendCustomHttpRequest(url, method, null, data)
                    }
                }
                else {
                    response = withContext(Dispatchers.IO) {
                        sendCustomHttpRequest(url, method, null, null)
                    }
                }
                reqResult.setText(response)
            }
        }

        return view
    }

    fun sendCustomHttpRequest(url: String, method: String, headers: Map<String, String>?, requestBody: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method

       
        headers?.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }

        val outputStream = connection.outputStream ?: throw IOException("Output stream is null")
        val dataOutputStream = DataOutputStream(outputStream)

       
        requestBody?.let {
            connection.doOutput = true
            val outputStream = dataOutputStream
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
