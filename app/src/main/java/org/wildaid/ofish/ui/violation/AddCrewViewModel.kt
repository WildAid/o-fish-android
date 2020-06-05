package org.wildaid.ofish.ui.violation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.CrewSearchModel

class AddCrewViewModel : ViewModel() {
    val crewMember = MutableLiveData<Event<CrewSearchModel>>()
    val validated = MutableLiveData<Event<Boolean>>()
    val isCaptain = MutableLiveData(false)
    var newCrewMember: CrewMember = CrewMember()

    private lateinit var report: Report

    fun initReport(report: Report) {
        this.report = report
    }

    fun onAddClicked() {
        if (validated()) {
            if (isCaptain.value!!) {
                report.captain = newCrewMember
            } else {
                report.crew.add(newCrewMember)
            }
            crewMember.value = Event(CrewSearchModel(newCrewMember, isCaptain.value!!))
        } else {
            validated.value = Event(false)
        }
    }

    private fun validated() =
        newCrewMember.name.isNotBlank() && newCrewMember.license.isNotBlank()
}
