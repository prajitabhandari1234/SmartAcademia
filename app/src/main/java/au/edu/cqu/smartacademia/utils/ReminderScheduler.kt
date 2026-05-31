package au.edu.cqu.smartacademia.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object ReminderScheduler {

    private const val REQUEST_CODE = 5001
    private const val FOUR_HOURS = 4 * 60 * 60 * 1000L
    private const val SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000L

    fun startReminder(context: Context, message: String) {
        val endTime = System.currentTimeMillis() + SEVEN_DAYS

        context.getSharedPreferences("smartacademia_reminders", Context.MODE_PRIVATE)
            .edit()
            .putLong("end_time", endTime)
            .putString("reminder_message", message)
            .apply()

        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + FOUR_HOURS,
            FOUR_HOURS,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context) {
        val intent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        context.getSharedPreferences("smartacademia_reminders", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}