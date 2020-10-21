package org.wildaid.ofish.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import org.wildaid.ofish.R
import org.wildaid.ofish.ui.home.HomeActivity

private const val NOTIFICATION_ID = 119

class OnDutyAlarmReminder : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(HomeActivity::class.java)
            .setGraph(R.navigation.home_navigation)
            .setDestination(R.id.profileFragment)
            .setArguments(Bundle())
            .createPendingIntent()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Are you still on duty?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
