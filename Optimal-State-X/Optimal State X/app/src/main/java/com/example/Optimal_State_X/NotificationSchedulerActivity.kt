package com.example.Optimal_State_X

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
// Add these imports at the top
import android.Manifest
//import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.appcompat.widget.Toolbar
import com.example.Optimal_State_X.databinding.ActivityNotificationSchedulerBinding
//import com.example.loginapplication.R
//import com.example.loginapplication.databinding.ActivityNotificationSchedulerBinding

//import com.example.Optimal_State_X.databinding.ActivityNotificationSchedulerBinding

class NotificationSchedulerActivity : AppCompatActivity() {
    private lateinit var timePicker: TimePicker
    private lateinit var btnSetNotification: Button
    private lateinit var toolbar: Toolbar
    private lateinit var binding: ActivityNotificationSchedulerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_scheduler)

        setupToolbar()
        initializeViews()
        checkPermissions()
        createNotificationChannel()

    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Set Assessment Time"

        // Add this for back arrow functionality
        toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun initializeViews() {
        timePicker = findViewById(R.id.timePicker)
        btnSetNotification = findViewById(R.id.btnSetNotification)

        btnSetNotification.setOnClickListener {
            scheduleNotification()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun scheduleNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timePicker.hour)
            set(Calendar.MINUTE, timePicker.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
            showSuccessMessage(calendar)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Failed to schedule: Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSuccessMessage(calendar: Calendar) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val scheduledTime = timeFormat.format(calendar.time)
        Toast.makeText(
            this,
            "Assessment notification scheduled for $scheduledTime",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Assessment Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled assessments"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val CHANNEL_ID = "assessment_notifications"
        const val NOTIFICATION_ID = 101
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, settng::class.java)
        startActivity(intent)
        finish()
    }

}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val contentIntent = Intent(context, DisplayImageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time for Assessment")
            .setContentText("Tap to start your assessment now")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "assessment_notifications"
        const val NOTIFICATION_ID = 101
    }
}
