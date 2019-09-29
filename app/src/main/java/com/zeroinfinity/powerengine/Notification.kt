package com.zeroinfinity.powerengine

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zeroinfinity.powerengine.PowerEngine.lastPackageName
import com.zeroinfinity.powerengine.PowerEngine.previousProfile
import com.zeroinfinity.powerengine.activities.MainActivity

object Notification {
    fun createNotification(context: Context) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val body = "Detected app: $lastPackageName\nProfile: $previousProfile"

        builder.apply {
            setSmallIcon(R.drawable.outline_memory_24)
            setContentTitle(context.getString(R.string.running))
            setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )
            setContentIntent(pendingIntent)
            setOngoing(true)
            setShowWhen(false)
            priority = NotificationCompat.PRIORITY_MIN
        }

        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }

    fun cancelNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) { cancel(0) }
    }
}