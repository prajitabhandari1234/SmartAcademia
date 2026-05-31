package au.edu.cqu.smartacademia.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val preferences = context.getSharedPreferences("smartacademia_reminders", Context.MODE_PRIVATE)

        val endTime = preferences.getLong("end_time", 0L)
        val message = preferences.getString(
            "reminder_message",
            "Check your due and upcoming assignments."
        ) ?: "Check your due and upcoming assignments."

        if (System.currentTimeMillis() <= endTime) {
            NotificationHelper.showTaskNotification(
                context,
                "SmartAcademia Assignment Reminder",
                message,
                2001
            )
        } else {
            ReminderScheduler.cancelReminder(context)
        }
    }
}