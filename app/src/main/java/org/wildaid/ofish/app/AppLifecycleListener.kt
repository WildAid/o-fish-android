package org.wildaid.ofish.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.wildaid.ofish.data.ON_DUTY

const val FOUR_HOURS_TIMER_REQUEST_ID = 13
private const val FOUR_HOURS = 4 * 60 * 60 * 1000

class AppLifecycleListener(
    private val context: Context
) : LifecycleObserver {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    private val alarmIntent: PendingIntent by lazy { getAlarmPendingIntent() }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        alarmManager?.cancel(alarmIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        val repository = ServiceLocator.provideRepository(context)
        if (repository.getRecentOnDutyChange()?.status == ON_DUTY) {
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