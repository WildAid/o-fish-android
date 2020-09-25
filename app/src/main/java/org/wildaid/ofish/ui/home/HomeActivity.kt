package org.wildaid.ofish.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.SearchManager
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
import org.wildaid.ofish.ui.search.base.BaseSearchFragment
import org.wildaid.ofish.util.getViewModelFactory

const val ASK_CHANGE_DUTY_DIALOG_ID = 10
const val ASK_TO_LOGOUT_DIALOG_ID = 11
const val TEN_HOURS_TIMER_REQUEST_ID = 12

private const val TEN_HOURS = 10 * 60 * 60 * 1000

class HomeActivity : AppCompatActivity(R.layout.activity_home) {
    private val activityViewModel: HomeActivityViewModel by viewModels { getViewModelFactory() }
    private val navigation: NavController by lazy { findNavController(R.id.home_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityViewModel.userEventLiveData.observe(this, EventObserver {
            when (it) {
                HomeActivityViewModel.HomeActivityUserEvent.AskDutyConfirmationEvent -> askToChangeDuty()
                HomeActivityViewModel.HomeActivityUserEvent.AskUserLogoutEvent -> askToLogout()
                HomeActivityViewModel.HomeActivityUserEvent.HomeUserLogoutEvent -> onUserLoggedOut()
                HomeActivityViewModel.HomeActivityUserEvent.BecomeNotAtSea -> navigateToPatrolSummary()
            }
        })
        activityViewModel.timerLiveData.observe(this, EventObserver(::setOrCancelTimer))
    }

    private fun navigateToPatrolSummary() {
        navigation.navigate(R.id.action_profileFragment_to_patrolSummaryFragment2)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (Intent.ACTION_SEARCH == intent?.action) {
            val requiredQuery = intent.getStringExtra(SearchManager.QUERY)
            supportFragmentManager.findFragmentById(R.id.home_host_fragment)?.let {
                val topFragment = it.childFragmentManager.fragments[0]
                if (topFragment is BaseSearchFragment<*>) {
                    topFragment.applySearchQuery(requiredQuery)
                }
            }
        } else {
            val navOptions = NavOptions.Builder().apply {
                setPopUpTo(R.id.home_fragment, true)
            }.build()
            navigation.navigate(R.id.home_fragment, intent?.extras, navOptions)
        }
    }

    private fun askToChangeDuty() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_CHANGE_DUTY_DIALOG_ID,
            getString(R.string.you_are_not_at_sea),
            getString(R.string.change_status_to_at_sea),
            getString(R.string.yes),
            getString(android.R.string.cancel)
        ).bundle()

        navigation.navigate(R.id.ask_change_duty_dialog, dialogBundle)
    }

    private fun onUserLoggedOut() {
        navigation.navigate(R.id.action_profileFragment_to_login_activity)
        finish()
    }

    private fun askToLogout() {
        val dialogBundle = ConfirmationDialogFragment.Bundler(
            ASK_TO_LOGOUT_DIALOG_ID,
            getString(R.string.logout_dialog_title),
            getString(R.string.logout_dialog_message),
            getString(R.string.logout_dialog_yes),
            getString(android.R.string.cancel)
        ).bundle()

        navigation.navigate(R.id.action_profileFragment_to_ask_logout_dialog, dialogBundle)
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