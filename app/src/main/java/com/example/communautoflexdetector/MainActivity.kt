package com.example.communautoflexdetector

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.UnknownHostException

const val BASE_URL = "https://www.reservauto.net/WCF/LSI/LSIBookingServiceV3.svc/"
class MainActivity : ComponentActivity() {
    private val CHANNEL_ID = "my_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            lifecycleScope.launch {
                var numberOfCars = 0
                val textView1 = findViewById<TextView>(R.id.textView1)
                val button = findViewById<TextView>(R.id.button)

                try
                {
                    val message = StringBuilder()
                    textView1.text = null
                    button.text = "Searching..."
                    button.isEnabled = false
                    button.isClickable = false
                    while (numberOfCars == 0) {
                        numberOfCars = execution()
                        delay(1000L)
                    }
                    message.append("Number of Flex vehicle(s) available: $numberOfCars")
                    textView1.text = message

                    Log.d("MainActivity", "car found: $numberOfCars")
                    sendNotification("$numberOfCars Car(s) available!", "Go get your car quickly!")
                }
                catch (e: UnknownHostException)
                {
                    val errorMessage = StringBuilder()
                    errorMessage.append("Please, check your internet connection!")
                    textView1.text = errorMessage

                    Log.d("MainActivity", "Exception: " + e.message)
                    sendNotification("Research couldn't go through!", "Please, check your internet connection!")
                }
                button.text = "Find a car"
                button.isEnabled = true
                button.isClickable = true
                Log.d("MainActivity", "notification sent!")
            }
        }
    }

    private suspend fun execution() : Int {
        return withContext(Dispatchers.IO) {
            val retrofitBuilder = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(ApiInterface::class.java)

            val result = retrofitBuilder.getData(2, 2, 105)

            val body = result.execute().body()!!
            val vehicles = body.d.Vehicles
            return@withContext vehicles.size
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
}