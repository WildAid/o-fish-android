package org.wildaid.ofish.ui.reportdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.bson.types.ObjectId
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.ui.home.HomeActivityViewModel

class ReportDetailViewModel(val repository: Repository) : ViewModel() {
    val reportLiveData = MutableLiveData<Report>()
    val boardVesselLiveData = MutableLiveData<Event<String>>()
    lateinit var activityViewModel: HomeActivityViewModel

    private var loadedReport: Report? = null

    fun loadReport(reportId: ObjectId) {
        loadedReport = repository.findReport(reportId)

        loadedReport?.let {
            reportLiveData.value = it
        }
    }

    fun getPhotosForIds(photoIds: List<String>?): List<PhotoItem> {
        return repository.getPhotosWithIds(photoIds.orEmpty()).map { PhotoItem(it) }
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            loadedReport?.vessel?.permitNumber?.let {
                boardVesselLiveData.value = Event(it)
            }
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.UserEvent.AskDutyConfirmationEvent)
        }
    }
}