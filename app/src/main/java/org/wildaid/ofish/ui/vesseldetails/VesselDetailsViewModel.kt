package org.wildaid.ofish.ui.vesseldetails

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.ViolationRisk
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.util.getString
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

class VesselDetailsViewModel(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private var _vesselItemLiveData = MutableLiveData<VesselItem>()
    val vesselItemLiveData: LiveData<VesselItem>
        get() = _vesselItemLiveData

    private var _vesselPhotosLiveData = MutableLiveData<List<Photo>>()
    val vesselPhotosLiveData: LiveData<List<Photo>>
        get() = _vesselPhotosLiveData

    private var _userEventLiveData = MutableLiveData<Event<VesselDetailsUserEvent>>()
    val userEventLiveData: LiveData<Event<VesselDetailsUserEvent>>
        get() = _userEventLiveData

    private lateinit var vesselReports: List<Report>

    lateinit var activityViewModel: HomeActivityViewModel

    fun loadVessel(vesselPermitNumber: String, vesselName: String) {
        repository.findReportsForBoat(vesselPermitNumber, vesselName)
            .flatMapMerge { reports ->
                repository.findBoat(vesselPermitNumber, vesselName)
                    .map { boat ->
                        val kajshd = 0
                        Pair(boat, reports)
                    }
            }.onEach { (boat, reports) ->
                initView(boat, reports)
            }.launchIn(viewModelScope)
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            vesselItemLiveData.value?.let {
                _userEventLiveData.value =
                    Event(VesselDetailsUserEvent.AskOnDutyToNavigate(vesselReports.first()))
            }
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.HomeActivityUserEvent.AskDutyConfirmationEvent)
        }
    }

    fun getPermitNumberDescription(): LiveData<String> {
        return vesselItemLiveData.map {
            getString(R.string.records_permit_number, it.vessel.permitNumber)
        }
    }

    private fun initView(boat: Boat?, reports: List<Report>) {
        if (boat == null) {
            throw IllegalArgumentException("Boat cannot be null.")
        }

        vesselReports = reports

        val warnings = reports.flatMap { it.inspection?.summary?.violations!! }
            .count { it.disposition == ViolationRisk.Warning.name }

        val citations = reports.flatMap { it.inspection?.summary?.violations!! }
            .count { it.disposition == ViolationRisk.Citation.name }

        val vesselReportItems = reports.map { report ->
            ReportItem(
                report,
                report.inspection?.summary?.violations!!
                    .count { it.disposition == ViolationRisk.Warning.name },
                report.inspection?.summary?.violations!!
                    .count { it.disposition == ViolationRisk.Citation.name }
            )
        }

        _vesselItemLiveData.value =
            VesselItem(boat, vesselReportItems, reports.size, warnings, citations)

        reports.map {
            repository.getPhotosWithIds(it.vessel?.attachments?.photoIDs.orEmpty())
        }.flatten().ifEmpty {
            listOf(Photo()) // invalid photo, just to display holder
        }.also {
            _vesselPhotosLiveData.value = it
        }
    }
}

sealed class VesselDetailsUserEvent {
    class AskOnDutyToNavigate(val report: Report) : VesselDetailsUserEvent()
}

data class VesselItem(
    val vessel: Boat,
    val reports: List<ReportItem>,
    val reportCount: Int,
    val warningsCount: Int,
    val citationCount: Int
)

data class ReportItem(
    val report: Report,
    val warningsCount: Int,
    val citationCount: Int
)