package org.wildaid.ofish.ui.reportdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.bson.types.ObjectId
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.util.LATITUDE
import org.wildaid.ofish.util.LONGITUDE
import org.wildaid.ofish.util.convert

class ReportDetailViewModel(val repository: Repository) : ViewModel() {

    private var _reportLiveData = MutableLiveData<Report>()
    val reportLiveData: LiveData<Report>
        get() = _reportLiveData

    private var _boardVesselLiveData = MutableLiveData<Event<Report>>()
    val boardVesselLiveData: LiveData<Event<Report>>
        get() = _boardVesselLiveData

    lateinit var activityViewModel: HomeActivityViewModel

    private var loadedReport: Report? = null

    fun loadReport(reportId: ObjectId) {
        loadedReport = repository.findReport(reportId)

        loadedReport?.let {
            _reportLiveData.value = it
        }
    }

    fun getPhotosForIds(photoIds: List<String>?): List<PhotoItem> {
        return repository.getPhotosWithIds(photoIds.orEmpty()).map { PhotoItem(it) }
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            loadedReport?.let {
                _boardVesselLiveData.value = Event(it)
            }
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.HomeActivityUserEvent.AskDutyConfirmationEvent)
        }
    }

    fun getFormattedLatitude() = convert(reportLiveData.value?.location?.get(1), LATITUDE)

    fun getFormattedLongitude() = convert(reportLiveData.value?.location?.get(0), LONGITUDE)
}