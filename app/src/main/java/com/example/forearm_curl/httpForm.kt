package com.example.forearm_curl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment

class CommandFragment : Fragment() {

    private lateinit var txtCommandName: TextView
    private lateinit var editTextCommand: EditText
    private lateinit var httpMethodSelector: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_http_form, container, false)

        editTextCommand = view.findViewById(R.id.editTextCommand)
        httpMethodSelector = view.findViewById(R.id.httpMethodSelector)

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
}
