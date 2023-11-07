package com.example.communautoflexdetector.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.communautoflexdetector.data.CarFound
import com.example.communautoflexdetector.R

class CustomNotification {
    private val CHANNEL_ID = "my_channel"
    private val NOTIFICATION_ID = 1

    private fun createIntent(context: Context) : NotificationCompat.Builder {
        val intent = Intent(context, CarFound::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Car found"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(context: Context, notificationTitle: CharSequence, notificationDescription: CharSequence) {
        if (ContextCompat.checkSelfPermission( context,android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, createIntent(context).apply {
                    setContentTitle(notificationTitle).
                    setContentText(notificationDescription)
                }.build())
            }
        }
    }
}