package com.example.communautoflexdetector

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

const val BASE_URL = "https://www.reservauto.net/WCF/LSI/LSIBookingServiceV3.svc/"
class MainActivity : ComponentActivity() {
    private val CHANNEL_ID = "my_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView1 = findViewById<TextView>(R.id.textView1)
        val button: Button = findViewById(R.id.button)

        val provinces = resources.getStringArray(R.array.provinces_array)
        val provinceSpinner : Spinner = findViewById(R.id.province_spinner)

        var cities : Array<String>? = null
        val citySpinner : Spinner = findViewById(R.id.city_spinner)

        var cityId : Int? = null
        var provinceId : Int? = null

        val citiesMap = HashMap<String, Int>()
        citiesMap["Toronto"] = 105
        citiesMap["SW Ontario (Hamilton)"] = 103
        citiesMap["Montreal"] = 59
        citiesMap["Quebec"] = 90
        citiesMap["Halifax"] = 92
        citiesMap["Calgary"] = 107
        citiesMap["Edmonton"] = 106

        val provincesMap = HashMap<String, Int>()
        provincesMap["Quebec"] = 1
        provincesMap["Ontario"] = 2
        provincesMap["Nova Scotia"] = 3
        provincesMap["Alberta"] = 10

        val provincesAndCitiesMap = HashMap<String, Int>()
        provincesAndCitiesMap["Ontario"] = R.array.ontario_cities_array
        provincesAndCitiesMap["Quebec"] = R.array.quebec_cities_array
        provincesAndCitiesMap["Nova Scotia"] = R.array.novascotia_cities_array
        provincesAndCitiesMap["Alberta"] = R.array.alberta_cities_array

        ArrayAdapter.createFromResource(
            this,
            R.array.provinces_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            provinceSpinner.adapter = adapter
        }

        provinceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val provinceName = provinces[p2]
                provinceId = provincesMap[provinceName]

                val provinceArray = provincesAndCitiesMap[provinceName]
                cities = resources.getStringArray(provinceArray!!)

                ArrayAdapter.createFromResource(
                    applicationContext,
                    provinceArray,
                    android.R.layout.simple_spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    citySpinner.adapter = adapter
                }

                enableButton(textView1)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                disableButton(textView1)
            }
        }

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val cityName = cities!![p2]
                cityId = citiesMap[cityName]
                enableButton(textView1)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                disableButton(textView1)
            }
        }

        createNotificationChannel()
        createNetworkRequest(textView1, button)
        button.setOnClickListener {
            lifecycleScope.launch {
                try
                {
                    var carsData: Array<Any> = arrayOf(0, "null")
                    var numberOfCars = carsData[0] as Int
                    var counter = 0
                    disableButton(button)
                    searchingButton(button)
                    deleteMessage(textView1)
                    while (numberOfCars == 0 && counter < 300) {
                        carsData = execution(cityId!!, provinceId!!)
                        numberOfCars = carsData[0] as Int
                        counter++
                    }
                    if (numberOfCars == 0) {
                        sendNotification("Research timed out", "No car found, please try again!")
                    } else {
                        val plate = carsData[1] as String
                        foundCarMessage(textView1, plate)
                        sendNotification("1 or more car available", "Click here to open Communauto!")
                    }
                }
                catch (e: IOException)
                {
                    disableButton(button)
                    noInternetMessage(textView1)
                    sendNotification("Research had to stop", "Please, check your internet connection and try again!")
                }
                findButton(button)
                enableButton(button)
            }
        }
    }

    private fun isNotEmpty(list: List<*>?): Boolean {
        return list != null && list.isNotEmpty()
    }
    private suspend fun execution(cityId: Int, provinceId: Int): Array<Any> {
        return withContext(Dispatchers.IO) {
            val retrofitBuilder = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(ApiInterface::class.java)

            val result = retrofitBuilder.getData(provinceId, 2, cityId)

            val body = result.execute().body()!!
            val vehicles = body.d.Vehicles.size
            val plate = if (isNotEmpty(body.d.Vehicles)) body.d.Vehicles[0].CarPlate else "null"
            return@withContext arrayOf(vehicles, plate)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Car found"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createIntent() : NotificationCompat.Builder {
        val intent = Intent(this, CarFound::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    private fun sendNotification(notificationTitle: CharSequence, notificationDescription: CharSequence) {
        if (ContextCompat.checkSelfPermission( applicationContext,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(NOTIFICATION_ID, createIntent().apply {
                    setContentTitle(notificationTitle).
                    setContentText(notificationDescription)
                }.build())
            }
        }
    }

    private fun createNetworkRequest(textView1: TextView, button: TextView) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread(Runnable {
                    enableButton(button)
                    deleteMessage(textView1)
                })
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                runOnUiThread(Runnable {
                    disableButton(button)
                    noInternetMessage(textView1)
                })


            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun disableButton(button: TextView) {
        button.isEnabled = false
        button.isClickable = false
    }

    private fun enableButton(button: TextView) {
        button.isEnabled = true
        button.isClickable = true
    }

    private fun searchingButton(button: TextView) {
        button.text = "Searching..."
    }

    private fun findButton(button: TextView) {
        button.text = "Find a car"
    }

    private fun noInternetMessage(textView1: TextView) {
        val message = StringBuilder()
        message.append("Please, check your internet connection!")
        textView1.text = message
    }

    private fun deleteMessage(textView1: TextView) {
        textView1.text = null
    }

    private fun foundCarMessage(textView1: TextView, plate: String) {
        val message = StringBuilder()
        message.append("Car plate is $plate")
        textView1.text = message
    }
}