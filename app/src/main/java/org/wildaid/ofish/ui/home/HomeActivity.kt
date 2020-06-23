package org.wildaid.ofish.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import org.wildaid.ofish.EventObserver
import org.wildaid.ofish.R
import org.wildaid.ofish.app.OnDutyAlarmReminder
import org.wildaid.ofish.ui.base.ConfirmationDialogFragment
import org.wildaid.ofish.util.getViewModelFactory

const val ASK_CHANGE_DUTY_DIALOG_ID = 10
const val TEN_HOURS_TIMER_REQUEST_ID = 11

private const val TEN_HOURS = 10 * 60 * 60 * 1000

class HomeActivity : AppCompatActivity(R.layout.activity_home) {
    private val activityViewModel: HomeActivityViewModel by viewModels { getViewModelFactory() }
    private val navigation: NavController by lazy { findNavController(R.id.home_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityViewModel.userEventLiveData.observe(this, EventObserver() {
            when (it) {
                HomeActivityViewModel.UserEvent.AskDutyConfirmationEvent -> askToChangeDuty()
                HomeActivityViewModel.UserEvent.UserLogoutEvent -> onUserLoggedOut()
            }
        })
        activityViewModel.timerLiveData.observe(this, EventObserver(::setOrCancelTimer))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val navOptions = NavOptions.Builder().apply {
            setPopUpTo(R.id.home_fragment, true)
        }.build()
        navigation.navigate(R.id.home_fragment, intent?.extras, navOptions)
    }

    private fun onUserLoggedOut() {
        navigation.navigate(R.id.action_home_fragment_to_login_activity)
        finish()
    }

    private fun askToChangeDuty() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_CHANGE_DUTY_DIALOG_ID,
            getString(R.string.you_are_of_duty),
            getString(R.string.change_status_to_on_duty),
            getString(R.string.yes),
            getString(android.R.string.cancel)
        ).bundle()

        navigation.navigate(R.id.ask_change_duty_dialog, dialogBundle)
    }

    private fun setOrCancelTimer(onDuty: Boolean) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val alarmIntent = Intent(this, OnDutyAlarmReminder::class.java).let { intent ->
            PendingIntent.getBroadcast(this, TEN_HOURS_TIMER_REQUEST_ID, intent, 0)
        }
        if (onDuty) {
            alarmManager?.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + TEN_HOURS,
                alarmIntent
            )
        } else {
            alarmManager?.cancel(alarmIntent)
        }
    }
}