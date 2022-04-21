package org.wildaid.ofish.ui.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
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

    private val _onDutyStatusLiveData = MutableLiveData<Boolean>()
    val onDutyStatusLiveData: LiveData<Boolean>
        get() = _onDutyStatusLiveData

    private val _onDutyTextStatusLiveData = MutableLiveData<String>()
    val onDutyTextStatusLiveData: LiveData<String>
        get() = _onDutyTextStatusLiveData

    private val _darkModeLiveData = MutableLiveData<Boolean>()
    val darkModeLiveData: LiveData<Boolean>
        get() = _darkModeLiveData

    private val _currentOfficerLiveData = MutableLiveData<OfficerData>()
    val currentOfficerLiveData: LiveData<OfficerData>
        get() = _currentOfficerLiveData

    private val _timerLiveData = MutableLiveData<Event<Boolean>>()
    val timerLiveData: LiveData<Event<Boolean>>
        get() = _timerLiveData

    private val _onDraftBoardsDeletedSuccessListener = MutableLiveData<Boolean>()
    val onDraftBoardsDeletedSuccessListener: LiveData<Boolean>
        get() = _onDraftBoardsDeletedSuccessListener

    var userEventLiveData = MutableLiveData<Event<HomeActivityUserEvent>>()

    init {
        val officer = repository.getCurrentOfficer()
        if (!repository.isLoggedIn()) {
            logOutUser()
        } else {
            _currentOfficerLiveData.value = officer
        }

        val lastOnDutyStatus = repository.getRecentOnDutyChange()?.status == ON_DUTY
        _onDutyStatusLiveData.value = lastOnDutyStatus
        updateStringStatus(lastOnDutyStatus)
        updateDarkModeStatus()
    }

    fun changeStatus() {
        if (_onDutyStatusLiveData.value == true) {
            userEventLiveData.value = Event(HomeActivityUserEvent.BecomeNotAtSea)
        } else {
            userEventLiveData.value = Event(HomeActivityUserEvent.AskDutyConfirmationEvent)
        }
    }

    fun onDutyChanged(onDuty: Boolean, date: Date = Date()) {
        if (onDutyStatusLiveData.value == onDuty) {
            return
        }

        _onDutyStatusLiveData.value = onDuty
        _timerLiveData.value = Event(onDuty)
        repository.saveOnDutyChange(onDuty, date)

        updateStringStatus(onDuty)
    }

    fun onDarkModeStateChange() {
        if(_darkModeLiveData.value!!) {
            _darkModeLiveData.value = false
            saveDarkModeState(false)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            _darkModeLiveData.value = true
            saveDarkModeState(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun logOutUser() {
        userEventLiveData.value = Event(HomeActivityUserEvent.AskUserLogoutEvent)
    }


    fun removeDraftedBoardsBeforeLogout() {
        repository.deleteAllDraftedBoardings(_onDraftBoardsDeletedSuccessListener)

    }

    fun logoutConfirmed() {
        repository.logOut(
            logoutSuccess = {
                userEventLiveData.value = Event(HomeActivityUserEvent.HomeUserLogoutEvent)
            },
            logoutError = {
                // TODO add message here
                Log.d("Logout", "Cannot logout, error -> $it")
            }
        )
    }

    private fun updateStringStatus(onDuty: Boolean) {
        if (onDuty)
            _onDutyTextStatusLiveData.value = getString(R.string.at_sea)
        else
            _onDutyTextStatusLiveData.value = getString(R.string.not_at_sea)
    }

    private fun updateDarkModeStatus() {
        _darkModeLiveData.value = getDarkModeStatus()
    }

    private fun getDarkModeStatus(): Boolean {
        val defaultNightMode: Int = AppCompatDelegate.getDefaultNightMode()
        return defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun saveDarkModeState(updatedState: Boolean) {
        val sharedPref: SharedPreferences = getApplication<Application>()
            .getSharedPreferences(getString(R.string.DARK_MODE_STATE), Context.MODE_PRIVATE)
            ?: return
        with(sharedPref.edit()) {
            putBoolean(getString(R.string.DARK_MODE_ENABLED), updatedState)
            apply()
        }
    }

    sealed class HomeActivityUserEvent {
        object AskDutyConfirmationEvent : HomeActivityUserEvent()
        object AskUserLogoutEvent : HomeActivityUserEvent()
        object HomeUserLogoutEvent : HomeActivityUserEvent()
        object BecomeNotAtSea : HomeActivityUserEvent()
    }
}
