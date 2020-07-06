package org.wildaid.ofish.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

const val FOUR_HOURS_TIMER_REQUEST_ID = 13
private const val FOUR_HOURS = 4 * 60 * 60 * 1000

class LifecycleListener(private val context: Context) : LifecycleObserver {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val alarmIntent: PendingIntent by lazy { getAlarmPendingIntent() }
    private val repository = (context.applicationContext as OFishApplication).repository

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        alarmManager?.cancel(alarmIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        if (repository.getOnDutyStatus().dutyStatus) {
            alarmManager?.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FOUR_HOURS,
                alarmIntent
            )
        }
    }

    private fun getAlarmPendingIntent() =
        Intent(context, OnDutyAlarmReminder::class.java).let { intent ->
            PendingIntent.getBroadcast(context, FOUR_HOURS_TIMER_REQUEST_ID, intent, 0)
        }
}