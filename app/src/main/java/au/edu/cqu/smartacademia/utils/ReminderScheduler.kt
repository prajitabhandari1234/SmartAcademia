package au.edu.cqu.smartacademia.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Utility object responsible for scheduling
 * and cancelling automatic assignment reminders.
 *
 * Supports the SmartAcademia reminder feature by:
 * - Scheduling notifications every 4 hours.
 * - Running reminders for up to 7 days.
 * - Automatically stopping reminders after expiry.
 */
object ReminderScheduler {

    /**
     * Unique request code used for PendingIntent.
     */
    private const val REQUEST_CODE = 5001

    /**
     * Reminder interval (4 hours).
     */
    private const val FOUR_HOURS = 4 * 60 * 60 * 1000L

    /**
     * Reminder duration (7 days).
     */
    private const val SEVEN_DAYS = 7 * 24 * 60 * 60 * 1000L

    /**
     * Starts automatic assignment reminders.
     *
     * Notifications will be displayed every 4 hours
     * for a maximum period of 7 days.
     *
     * @param context Application context.
     * @param message Reminder message displayed
     * in the notification.
     */
    fun startReminder(
        context: Context,
        message: String
    ) {

        val endTime = System.currentTimeMillis() + SEVEN_DAYS

        context.getSharedPreferences(
            "smartacademia_reminders",
            Context.MODE_PRIVATE
        )
            .edit()
            .putLong("end_time", endTime)
            .putString("reminder_message", message)
            .apply()

        val intent = Intent(
            context,
            ReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + FOUR_HOURS,
            FOUR_HOURS,
            pendingIntent
        )
    }

    /**
     * Cancels all scheduled reminders.
     *
     * Removes the repeating alarm and clears
     * stored reminder preferences.
     *
     * @param context Application context.
     */
    fun cancelReminder(context: Context) {

        val intent = Intent(
            context,
            ReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        alarmManager.cancel(pendingIntent)

        context.getSharedPreferences(
            "smartacademia_reminders",
            Context.MODE_PRIVATE
        )
            .edit()
            .clear()
            .apply()
    }
}