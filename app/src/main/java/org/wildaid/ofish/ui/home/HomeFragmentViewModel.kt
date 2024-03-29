package org.wildaid.ofish.ui.home

import android.net.Uri
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.MpaData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.ui.base.MpaViewModel

class HomeFragmentViewModel(val repository: Repository) : MpaViewModel() {

    val mpaObservable = ObservableBoolean(true)

    private val _mpaLiveData = MutableLiveData<Boolean>()
    val mpaLiveData: LiveData<Boolean>
        get() = _mpaLiveData

    private val _locationLiveData = MutableLiveData<Pair<Double, Double>>()
    val locationLiveData: LiveData<Pair<Double, Double>>
        get() = _locationLiveData

    private val _userEventLiveData = MutableLiveData<Event<HomeFragmentUserEvent>>()
    val userEventLiveData: LiveData<Event<HomeFragmentUserEvent>>
        get() = _userEventLiveData

    private val _amountOfDrafts = MutableLiveData<Int>()
    val amountOfDrafts: LiveData<Int>
        get() = _amountOfDrafts

    lateinit var activityViewModel: HomeActivityViewModel

    fun onLocationAvailable(latitude: Double, longitude: Double) {
        _locationLiveData.value = Pair(latitude, longitude)
    }

    fun fetchMpaData(): MutableList<MpaData> {
        return super.fetchMpaData(repository.getAllProtectionMarineAreas())
    }

    fun showDrafts(){
        _userEventLiveData.value = Event(HomeFragmentUserEvent.ShowDrafts)
    }

    fun updateDraftCount() {
        _amountOfDrafts.value = repository.getAmountOfDraftsByEmail()
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

    fun onMpaClick() {
        mpaObservable.set(!mpaObservable.get())
        _mpaLiveData.value = mpaObservable.get()
    }

    sealed class HomeFragmentUserEvent {
        object FindRecords : HomeFragmentUserEvent()
        object ShowUserStatus : HomeFragmentUserEvent()
        object BoardVessel : HomeFragmentUserEvent()
        object ShowDrafts : HomeFragmentUserEvent()
    }
}