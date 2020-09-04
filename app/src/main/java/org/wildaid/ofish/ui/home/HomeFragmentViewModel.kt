package org.wildaid.ofish.ui.home

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository

class HomeFragmentViewModel(val repository: Repository) : ViewModel() {

    private var _locationLiveData = MutableLiveData<Pair<Double, Double>>()
    val locationLiveData: LiveData<Pair<Double, Double>>
        get() = _locationLiveData

    private var _userEventLiveData = MutableLiveData<Event<UserEvent>>()
    val userEventLiveData: LiveData<Event<UserEvent>>
        get() = _userEventLiveData

    lateinit var activityViewModel: HomeActivityViewModel

    fun onLocationAvailable(latitude: Double, longitude: Double) {
        _locationLiveData.value = Pair(latitude, longitude)
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            _userEventLiveData.value = Event(UserEvent.BoardVessel)
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.UserEvent.AskDutyConfirmationEvent)
        }
    }

    fun findRecords() {
        _userEventLiveData.value = Event(UserEvent.FindRecords)
    }

    fun showUserStatus() {
        _userEventLiveData.value = Event(UserEvent.ShowUserStatus)
    }

    fun saveProfileImage(uri: Uri) {
        repository.updateCurrentOfficerPhoto(uri)
    }

    sealed class UserEvent {
        object FindRecords : UserEvent()
        object ShowUserStatus : UserEvent()
        object BoardVessel : UserEvent()
    }
}