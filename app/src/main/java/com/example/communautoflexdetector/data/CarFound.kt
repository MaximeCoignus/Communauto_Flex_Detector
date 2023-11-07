package com.example.communautoflexdetector.data

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.communautoflexdetector.R

class CarFound : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_found_car)

        val button: Button = findViewById(R.id.open_button)
        button.setOnClickListener {
            try {
                val openCommunautoIntent = packageManager.getLaunchIntentForPackage("com.communauto.reservauto")
                startActivity(openCommunautoIntent)
            } catch (e: ActivityNotFoundException) {
                Log.d("CarFound", e.toString())
            }
        }
    }
}