package org.wildaid.ofish.ui.createreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.bson.types.ObjectId
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.OTHER
import org.wildaid.ofish.data.OnSaveListener
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.description.ReportDescriptionFields
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.PhotoItem
import java.util.*

class CreateReportViewModel(val repository: Repository) : ViewModel() {
    var fieldsDescriptions = ReportDescriptionFields()

    private val _createReportUserEvent = MutableLiveData<Event<CreateReportUserEvent>>()
    val createReportUserEvent: LiveData<Event<CreateReportUserEvent>>
        get() = _createReportUserEvent

    var isOnSearch: Boolean = false
    var isAddingCrewMember: Boolean = false

    lateinit var report: Report
    val reportPhotos: MutableList<PhotoItem> = mutableListOf()

    fun initReport(reportDraftId: ObjectId?) {
        report = if (reportDraftId == null) {
            initiateNewReport()
        } else {
            getDraft(reportDraftId)
        }

        _createReportUserEvent.value = Event(CreateReportUserEvent.StartReportCreation)
    }

    fun saveReport(isDraft: Boolean? = null, listener: OnSaveListener) {
        checkIfFieldsChooseOther()
        report.draft = isDraft
        val photosToSave = reportPhotos.map { Pair(it.photo, it.localUri) }
        repository.saveReport(report, isDraft, photosToSave, listener)
    }

    private fun checkIfFieldsChooseOther() {
        if (report.inspection?.activity?.name == OTHER) {
            report.inspection?.activity?.name = fieldsDescriptions.activityDescription
        }
        if (report.inspection?.fishery?.name == OTHER) {
            report.inspection?.fishery?.name = fieldsDescriptions.fisheryDescription
        }
        if (report.inspection?.gearType?.name == OTHER) {
            report.inspection?.gearType?.name = fieldsDescriptions.gearDescription
        }

        report.inspection?.actualCatch?.forEachIndexed { index, catch ->
            if (catch.fish == OTHER) {
                if (fieldsDescriptions.catches[index].description.isNotEmpty()) {
                    catch.fish = fieldsDescriptions.catches[index].description
                } else {
                    catch.fish = ""
                }
            }
        }
    }

    fun deleteReport() {
        repository.deleteDraft(report)
        _createReportUserEvent.value = Event(CreateReportUserEvent.NavigateToDraftList)
    }

    fun onBackPressed(): Boolean {
        if (isOnSearch || isAddingCrewMember) {
            return false
        }
        if (report.draft!!) {
            _createReportUserEvent.value = Event(CreateReportUserEvent.AskDeleteDraft)
        } else {
            _createReportUserEvent.value = Event(CreateReportUserEvent.AskDiscardBoarding)
        }
        return true
    }

    private fun initiateNewReport(): Report {
        val officer = repository.getCurrentOfficer()
        return Report().apply {
            reportingOfficer?.apply {
                email = officer.email
                name?.apply {
                    first = officer.firstName
                    last = officer.lastName
                }
            }
            vessel?.lastDelivery?.date = Date(0)

            // Pre-filled empty items for create report flow
            inspection?.summary?.seizures = Seizures()
            inspection?.summary?.violations?.add(Violation())
            notes.add(AnnotatedNote())
        }
    }

    private fun getDraft(draftId: ObjectId): Report {
        val draft = repository.findDraft(draftId) ?: return initiateNewReport()

        if (draft.vessel?.lastDelivery == null) {
            draft.vessel?.lastDelivery = Delivery()
        }
        return draft
    }

    sealed class CreateReportUserEvent {
        object AskDiscardBoarding : CreateReportUserEvent()
        object StartReportCreation : CreateReportUserEvent()
        object AskDeleteDraft : CreateReportUserEvent()
        object NavigateToDraftList : CreateReportUserEvent()
    }
}