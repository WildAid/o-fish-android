package org.wildaid.ofish.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository

class HomeFragmentViewModel(val repository: Repository) : ViewModel() {
    val locationLiveData = MutableLiveData<Pair<Double, Double>>()
    val userEventLiveData = MutableLiveData<Event<UserEvent>>()

    lateinit var activityViewModel: HomeActivityViewModel

    fun onLocationAvailable(latitude: Double, longitude: Double) {
        locationLiveData.value = Pair(latitude, longitude)
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            userEventLiveData.value = Event(UserEvent.BoardVessel)
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.UserEvent.AskDutyConfirmationEvent)
        }
    }

    fun findRecords() {
        userEventLiveData.value = Event(UserEvent.FindRecords)
    }

    fun showUserStatus() {
        userEventLiveData.value = Event(UserEvent.ShowUserStatus)
    }

    sealed class UserEvent {
        object FindRecords : UserEvent()
        object ShowUserStatus : UserEvent()
        object BoardVessel : UserEvent()
    }
}