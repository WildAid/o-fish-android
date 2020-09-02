package org.wildaid.ofish.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.ON_DUTY
import org.wildaid.ofish.data.OfficerData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.util.getString
import java.util.*

class HomeActivityViewModel(val repository: Repository, app: Application) : AndroidViewModel(app) {

    private var _onDutyStatusLiveData = MutableLiveData<Boolean>()
    val onDutyStatusLiveData: LiveData<Boolean>
        get() = _onDutyStatusLiveData

    private var _onDutyImageStatusLiveData = MutableLiveData<Int>()
    val onDutyImageStatusLiveData: LiveData<Int>
        get() = _onDutyImageStatusLiveData

    private var _onDutyImageStatusSmallLiveData = MutableLiveData<Int>()
    val onDutyImageStatusSmallLiveData: LiveData<Int>
        get() = _onDutyImageStatusSmallLiveData

    private var _onDutyTextStatusLiveData = MutableLiveData<String>()
    val onDutyTextStatusLiveData: LiveData<String>
        get() = _onDutyTextStatusLiveData

    private var _currentOfficerLiveData = MutableLiveData<OfficerData>()
    val currentOfficerLiveData: LiveData<OfficerData>
        get() = _currentOfficerLiveData

    private var _timerLiveData = MutableLiveData<Event<Boolean>>()
    val timerLiveData: LiveData<Event<Boolean>>
        get() = _timerLiveData

    var userEventLiveData = MutableLiveData<Event<UserEvent>>()

    init {
        val officer = repository.getCurrentOfficer()
        if (!repository.isLoggedIn()) {
            logOutUser()
        } else {
            _currentOfficerLiveData.value = officer
        }

        val lastOnDutyStatus = repository.getRecentOnDutyChange()?.status == ON_DUTY
        _onDutyStatusLiveData.value = lastOnDutyStatus
        applyDutyStatusDrawables(lastOnDutyStatus)
    }

    fun onDutyChanged(onDuty: Boolean, date: Date = Date()) {
        if (onDutyStatusLiveData.value == onDuty) {
            return
        }

        _onDutyStatusLiveData.value = onDuty
        _timerLiveData.value = Event(onDuty)
        repository.saveOnDutyChange(onDuty, date)

        applyDutyStatusDrawables(onDuty)
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
                // TODO add message here
                Log.d("Logout", "Cannot logout, error -> $it")
            }
        )
    }

    private fun applyDutyStatusDrawables(onDuty: Boolean) {
        if (onDuty) {
            _onDutyImageStatusLiveData.value = R.drawable.shape_green_circle
            _onDutyImageStatusSmallLiveData.value = R.drawable.shape_green_circle_small
            _onDutyTextStatusLiveData.value = getString(R.string.on_duty)
        } else {
            _onDutyImageStatusLiveData.value = R.drawable.shape_red_circle
            _onDutyImageStatusSmallLiveData.value = R.drawable.shape_red_circle_small
            _onDutyTextStatusLiveData.value = getString(R.string.off_duty)
        }
    }

    sealed class UserEvent {
        object AskDutyConfirmationEvent : UserEvent()
        object AskUserLogoutEvent : UserEvent()
        object UserLogoutEvent : UserEvent()
    }
}