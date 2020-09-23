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

    private var _userEventLiveData = MutableLiveData<Event<HomeFragmentUserEvent>>()
    val userEventLiveData: LiveData<Event<HomeFragmentUserEvent>>
        get() = _userEventLiveData

    private var _amountOfDrafts = MutableLiveData<Int>()
    val amountOfDrafts: LiveData<Int>
        get() = _amountOfDrafts

    lateinit var activityViewModel: HomeActivityViewModel

    fun updateDraftCount(){
        _amountOfDrafts.value = repository.getAmountOfDrafts()
    }

    fun onLocationAvailable(latitude: Double, longitude: Double) {
        _locationLiveData.value = Pair(latitude, longitude)
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            _userEventLiveData.value = Event(HomeFragmentUserEvent.BoardVessel)
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.HomeActivityUserEvent.AskDutyConfirmationEvent)
        }
    }

    fun findRecords() {
        _userEventLiveData.value = Event(HomeFragmentUserEvent.FindRecords)
    }

    fun showUserStatus() {
        _userEventLiveData.value = Event(HomeFragmentUserEvent.ShowUserStatus)
    }

    fun saveProfileImage(uri: Uri) {
        repository.updateCurrentOfficerPhoto(uri)
    }

    sealed class HomeFragmentUserEvent {
        object FindRecords : HomeFragmentUserEvent()
        object ShowUserStatus : HomeFragmentUserEvent()
        object BoardVessel : HomeFragmentUserEvent()
    }
}