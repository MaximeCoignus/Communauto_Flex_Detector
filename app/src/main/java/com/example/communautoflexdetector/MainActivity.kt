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

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            lifecycleScope.launch {
                var numberOfCarsStr = 0
                try {
                    while (numberOfCarsStr == 0) {
                        execution()
                        numberOfCarsStr = findViewById<TextView>(R.id.textView2).text.toString().toInt()
                        delay(1000L)
                    }
                    Log.d("MainActivity", "car found: $numberOfCarsStr")
                    if (ContextCompat.checkSelfPermission( applicationContext,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                    {
                        with(NotificationManagerCompat.from(applicationContext)) {
                            notify(NOTIFICATION_ID, builder.apply {
                                setContentTitle(findViewById<TextView>(R.id.textView2).text.toString() + " Car(s) available!").
                                setContentText("Go get your car quickly!")
                            }.build())
                        }
                    }
                    Log.d("MainActivity", "notification sent!")
                }
                catch (e: UnknownHostException)
                {
                    val errorMessage = StringBuilder()
                    errorMessage.append("Please, check your internet connection!")
                    val textView1 = findViewById<TextView>(R.id.textView1)
                    textView1.text = errorMessage
                    val textView2 = findViewById<TextView>(R.id.textView2)
                    textView2.text = null

                    Log.d("MainActivity", "Exception: " + e.message)
                    if (ContextCompat.checkSelfPermission( applicationContext,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
                    {
                        with(NotificationManagerCompat.from(applicationContext)) {
                            notify(NOTIFICATION_ID, builder.apply {
                                setContentTitle("Research couldn't go through!").
                                setContentText("Please, check your internet connection!")
                            }.build())
                        }
                    }
                    Log.d("MainActivity", "notification sent!")
                }
            }
        }
    }

    private suspend fun execution() {
        withContext(Dispatchers.IO) {
            val retrofitBuilder = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
                .create(ApiInterface::class.java)

            val result = retrofitBuilder.getData(2, 2, 105)

            val body = result.execute().body()!!

            val myStringBuilder1 = StringBuilder()
            val myStringBuilder2 = StringBuilder()

            val vehicles = body.d.Vehicles
            val size = vehicles.size

            myStringBuilder1.append("Number of Flex vehicle(s) available: ")
            myStringBuilder2.append(size)

            Log.d("MainActivity", "Number of Flex vehicle(s) available: $size")

            runOnUiThread(Runnable() {
                 run {
                    val textView1 = findViewById<TextView>(R.id.textView1)
                    val textView2 = findViewById<TextView>(R.id.textView2)
                    textView1.text = myStringBuilder1
                    textView2.text = myStringBuilder2
                }
            })

        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "My Channel"
            val channelDescription = "My channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}