package com.example.communautoflexdetector

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.communautoflexdetector.data.AvailableCarsDataSource
import com.example.communautoflexdetector.network.CustomNetworkRequest
import com.example.communautoflexdetector.notifications.CustomNotification
import com.example.communautoflexdetector.ui.theme.CustomSpinners
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity :
    ComponentActivity(),
    View.OnClickListener
{
    private lateinit var button : Button
    private lateinit var textView1 : TextView
    private lateinit var provinceSpinner: Spinner
    private lateinit var customNotification: CustomNotification
    private lateinit var provinceAdapter: ArrayAdapter<String>
    private lateinit var customSpinners : CustomSpinners

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customSpinners = CustomSpinners(this@MainActivity)
        provinceSpinner = findViewById(R.id.province_spinner)
        provinceAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.provinces_array)
        )
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        provinceSpinner.adapter = provinceAdapter
        provinceSpinner.onItemSelectedListener = customSpinners

        button = findViewById(R.id.button)
        button.text = "Find a car"
        button.setOnClickListener(this)

        textView1 = findViewById(R.id.textView1)

        CustomNetworkRequest(button, textView1).createNetworkRequest(this)

        customNotification = CustomNotification()
        customNotification.createNotificationChannel(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(p0: View?) {
        lifecycleScope.launch {
            button.isEnabled = false
            button.text = "Searching..."
            textView1.text = null
            try
            {
                val listOfCars = AvailableCarsDataSource().getAvailableCars(customSpinners.getProvinceId(), customSpinners.getCityId())
                if (listOfCars.isEmpty()) {
                    customNotification.sendNotification(this@MainActivity, "Research timed out", "No car found, please try again!")
                } else {
                    val firstCar = listOfCars[0]
                    val carId = firstCar.CarId

                    // automatically create a reservation when a car is being released
                    AvailableCarsDataSource().createBooking(carId)

                    val plate = firstCar.CarPlate
                    textView1.text = "Car plate is $plate"
                    customNotification.sendNotification(this@MainActivity, "1 or more car available", "Click here to open Communauto!")
                }
            }
            catch (e: IOException)
            {
                textView1.text = "Please, check your internet connection!"
                customNotification.sendNotification(this@MainActivity, "Research had to stop", "Please, check your internet connection and try again!")
            }
            button.text = "Find a car"
            button.isEnabled = true
        }
    }
}