package au.edu.cqu.smartacademia.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import au.edu.cqu.smartacademia.R

/**
 * Helper class responsible for creating
 * notification channels and displaying task reminders.
 *
 * Supports Assignment 3 interactive features by
 * notifying users about overdue, due and upcoming tasks.
 */
object NotificationHelper {

    /**
     * Notification channel identifier.
     */
    private const val CHANNEL_ID = "task_reminders_channel"

    /**
     * Creates the notification channel required for
     * Android 8.0 (API 26) and above.
     *
     * The channel is used to display task reminder notifications.
     *
     * @param context Application context.
     */
    fun createNotificationChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description =
                    context.getString(R.string.notification_channel_description)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Displays a task reminder notification.
     *
     * Checks notification permissions before sending
     * notifications on Android 13+ devices.
     *
     * @param context Application context.
     * @param title Notification title.
     * @param message Notification message.
     * @param notificationId Unique notification identifier.
     */
    fun showTaskNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat
            .from(context)
            .notify(notificationId, notification)
    }
}