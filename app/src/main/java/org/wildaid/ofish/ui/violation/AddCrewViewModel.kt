package org.wildaid.ofish.ui.violation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.search.complex.CrewSearchModel

class AddCrewViewModel : ViewModel() {

    private var _crewMember = MutableLiveData<Event<CrewSearchModel>>()
    val crewMember: LiveData<Event<CrewSearchModel>>
        get() = _crewMember

    private var _validated = MutableLiveData<Event<AddCrewValidation>>()
    val validated: LiveData<Event<AddCrewValidation>>
        get() = _validated

    var isCaptain: Boolean = false

    var newCrewMember: CrewMember = CrewMember()

    private lateinit var report: Report

    fun initReport(report: Report) {
        this.report = report
    }

    fun onAddClicked() {
        if (validated()) {
            if (isCaptain) {
                _validated.value = Event(AddCrewValidation.IS_CAPTAIN)
            } else {
                report.crew.add(newCrewMember)
                _crewMember.value = Event(CrewSearchModel(newCrewMember, false))
            }
        } else {
            _validated.value = Event(AddCrewValidation.NOT_VALID)
        }
    }

    fun updateCaptain() {
        report.captain = newCrewMember
        _crewMember.value = Event(CrewSearchModel(newCrewMember, true))
    }

    private fun validated() =
        newCrewMember.name.isNotBlank()

}

enum class AddCrewValidation {
    NOT_VALID, IS_CAPTAIN
}