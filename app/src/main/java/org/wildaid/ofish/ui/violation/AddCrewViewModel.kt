package org.wildaid.ofish.ui.violation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.CrewSearchModel

class AddCrewViewModel : ViewModel() {
    val crewMember = MutableLiveData<Event<CrewSearchModel>>()
    val validated = MutableLiveData<Event<AddCrewValidation>>()
    val isCaptain = MutableLiveData(false)
    var newCrewMember: CrewMember = CrewMember()

    private lateinit var report: Report

    fun initReport(report: Report) {
        this.report = report
    }

    fun onAddClicked() {
        if (validated()) {
            if (isCaptain.value!!) {
                validated.value = Event(AddCrewValidation.IS_CAPTAIN)
            } else {
                report.crew.add(newCrewMember)
                crewMember.value = Event(CrewSearchModel(newCrewMember, false))
            }
        } else {
            validated.value = Event(AddCrewValidation.NOT_VALID)
        }
    }

    fun updateCaptain() {
        report.captain = newCrewMember
        crewMember.value = Event(CrewSearchModel(newCrewMember, true))
    }

    private fun validated() =
        newCrewMember.name.isNotBlank() && newCrewMember.license.isNotBlank()
}

enum class AddCrewValidation {
    NOT_VALID, IS_CAPTAIN
}