package org.wildaid.ofish.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.OfficerData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.util.getString

class HomeActivityViewModel(val repository: Repository, app: Application) : AndroidViewModel(app) {
    val onDutyStatusLiveData = MutableLiveData<Boolean>()
    val onDutyImageStatusLiveData = MutableLiveData<Int>()
    val onDutyImageStatusSmallLiveData = MutableLiveData<Int>()
    val onDutyTextStatusLiveData = MutableLiveData<String>()
    val currentOfficerLiveData = MutableLiveData<OfficerData>()
    val timerLiveData = MutableLiveData<Event<Boolean>>()
    val userEventLiveData = MutableLiveData<Event<UserEvent>>()

    init {
        val officer = repository.getCurrentOfficer()
        if (!repository.isLoggedIn() || officer == null) {
            logOutUser()
        } else {
            currentOfficerLiveData.value = officer
        }

        val lastOnDutyStatus = repository.getOnDutyStatus()
        onDutyStatusLiveData.value = lastOnDutyStatus.dutyStatus
        applyDutyStatusDrawables(lastOnDutyStatus.dutyStatus)
    }

    fun onDutyChanged(onDuty: Boolean) {
        if (onDutyStatusLiveData.value == onDuty) {
            return
        }

        onDutyStatusLiveData.value = onDuty
        timerLiveData.value = Event(onDuty)
        repository.saveOnDutyChange(onDuty)

        applyDutyStatusDrawables(onDuty)

        if (onDuty == false) {
            userEventLiveData.value = Event(UserEvent.DutyReportEvent)
        }
    }

    fun logOutUser() {
        userEventLiveData.value = Event(UserEvent.AskUserLogoutEvent)
    }

    fun logoutConfirmed() {
        repository.logOut(
            logoutSuccess = {
                userEventLiveData.value = Event(UserEvent.UserLogoutEvent)
            },
            logoutError = {
                Log.d("Logout", "Cannot logout, error -> $it")
            }
        )
    }

    private fun applyDutyStatusDrawables(onDuty: Boolean) {
        if (onDuty) {
            onDutyImageStatusLiveData.value = R.drawable.shape_green_circle
            onDutyImageStatusSmallLiveData.value = R.drawable.shape_green_circle_small
            onDutyTextStatusLiveData.value = getString(R.string.on_duty)
        } else {
            onDutyImageStatusLiveData.value = R.drawable.shape_red_circle
            onDutyImageStatusSmallLiveData.value = R.drawable.shape_red_circle_small
            onDutyTextStatusLiveData.value = getString(R.string.off_duty)
        }
    }

    sealed class UserEvent {
        object AskDutyConfirmationEvent : UserEvent()
        object AskUserLogoutEvent : UserEvent()
        object UserLogoutEvent : UserEvent()
        object DutyReportEvent: UserEvent()
    }
}