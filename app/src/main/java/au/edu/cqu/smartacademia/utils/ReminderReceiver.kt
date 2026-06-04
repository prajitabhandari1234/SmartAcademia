package au.edu.cqu.smartacademia.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import au.edu.cqu.smartacademia.R

/**
 * BroadcastReceiver responsible for handling
 * scheduled assignment reminder notifications.
 *
 * Triggered by AlarmManager through ReminderScheduler.
 *
 * Supports Assignment 3 interactive features by
 * automatically notifying users about due and
 * upcoming assignments.
 */
class ReminderReceiver : BroadcastReceiver() {

    /**
     * Called when the scheduled reminder alarm is triggered.
     *
     * Displays a notification if the reminder period
     * is still active. Otherwise, the reminder schedule
     * is cancelled.
     *
     * @param context Application context.
     * @param intent Incoming broadcast intent.
     */
    override fun onReceive(context: Context, intent: Intent?) {

        val preferences = context.getSharedPreferences(
            "smartacademia_reminders",
            Context.MODE_PRIVATE
        )

        val endTime = preferences.getLong(
            "end_time",
            0L
        )

        val message = preferences.getString(
            "reminder_message",
            context.getString(R.string.default_reminder_message)
        ) ?: context.getString(R.string.default_reminder_message)

        if (System.currentTimeMillis() <= endTime) {

            NotificationHelper.showTaskNotification(
                context,
                context.getString(R.string.assignment_reminder_title),
                message,
                2001
            )

        } else {

            ReminderScheduler.cancelReminder(context)
        }
    }
}