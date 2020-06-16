package org.wildaid.ofish.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.realm.mongodb.User
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.util.getString

class HomeActivityViewModel(val repository: Repository, app: Application) : AndroidViewModel(app) {
    val onDutyStatusLiveData = MutableLiveData<Boolean>()
    val onDutyImageStatusLiveData = MutableLiveData<Int>()
    val onDutyTextStatusLiveData = MutableLiveData<String>()
    val currentUserLiveData = MutableLiveData<User>()
    val timerLiveData = MutableLiveData<Event<Boolean>>()

    val userEventLiveData = MutableLiveData<Event<UserEvent>>()

    private lateinit var currentUser: User

    init {
        onDutyStatusLiveData.value = false
        onDutyImageStatusLiveData.value = R.drawable.shape_red_circle
        onDutyTextStatusLiveData.value = getString(R.string.off_duty)

        val realmUser = repository.getCurrentUser()
        if (realmUser == null) {
            logOutUser()
        } else {
            currentUser = realmUser
            currentUserLiveData.value = currentUser
        }
    }

    fun onDutyChanged(onDuty: Boolean) {
        if (onDutyStatusLiveData.value == onDuty) {
            return
        }

        onDutyStatusLiveData.value = onDuty
        if (onDuty) {
            onDutyImageStatusLiveData.value = R.drawable.shape_green_circle
            onDutyTextStatusLiveData.value = getString(R.string.on_duty)
        } else {
            onDutyImageStatusLiveData.value = R.drawable.shape_red_circle
            onDutyTextStatusLiveData.value = getString(R.string.off_duty)
        }
        timerLiveData.value = Event(onDuty)
        repository.saveOnDutyChange(currentUser, onDuty)
    }

    fun logOutUser() {
        repository.logOut(
            logoutSuccess = {
                userEventLiveData.value = Event(UserEvent.UserLogoutEvent)
            },
            logoutError = {
                Log.d("Logout", "Cannot logout, error -> $it")
            }
        )
    }

    sealed class UserEvent {
        object AskDutyConfirmationEvent : UserEvent()
        object UserLogoutEvent : UserEvent()
    }
}