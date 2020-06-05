package org.wildaid.ofish.ui.vesseldetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.ViolationRisk
import org.wildaid.ofish.data.report.Boat
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.home.HomeActivityViewModel
import org.wildaid.ofish.util.getString

class VesselDetailsViewModel(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {
    val vesselItemLiveData = MutableLiveData<VesselItem>()
    val boardVesselLiveData = MutableLiveData<Event<String>>()

    lateinit var activityViewModel: HomeActivityViewModel

    fun loadVessel(vesselPermitNumber: String) {
        val vessel = repository.findBoat(vesselPermitNumber) ?: return
        val vesselReports = repository.findReportsForBoat(vesselPermitNumber)
        val warnings = vesselReports
            .flatMap { it.inspection?.summary?.violations!! }
            .count { it.disposition == ViolationRisk.Warning.name }

        val citations = vesselReports
            .flatMap { it.inspection?.summary?.violations!! }
            .count { it.disposition == ViolationRisk.Citation.name }

        val vesselReportItems = vesselReports.map {
            ReportItem(
                it,
                it.inspection?.summary?.violations!!
                    .count { it.disposition == ViolationRisk.Warning.name },
                it.inspection?.summary?.violations!!
                    .count { it.disposition == ViolationRisk.Citation.name }
            )
        }

        vesselItemLiveData.value =
            VesselItem(vessel, vesselReportItems, vesselReports.size, warnings, citations)
    }

    fun getPermitNumberDescription(): String {
        return getString(
            R.string.records_permit_number,
            vesselItemLiveData.value?.vessel?.permitNumber
        )
    }

    fun boardVessel() {
        if (activityViewModel.onDutyStatusLiveData.value == true) {
            vesselItemLiveData.value?.let {
                boardVesselLiveData.value = Event(it.vessel.permitNumber)
            }
        } else {
            activityViewModel.userEventLiveData.value =
                Event(HomeActivityViewModel.UserEvent.AskDutyConfirmationEvent)
        }
    }
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