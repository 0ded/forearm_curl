package com.example.forearm_curl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.forearm_curl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       
        binding.sampleText.text = stringFromJNI()
    }

    /**
     * A native method that is implemented by the 'forearm_curl' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
       
        init {
            System.loadLibrary("forearm_curl")
        }
    }
}