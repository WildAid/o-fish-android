package org.wildaid.ofish.ui.createreport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.OnSaveListener
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.AnnotatedNote
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.data.report.Seizures
import org.wildaid.ofish.data.report.Violation
import org.wildaid.ofish.ui.base.PhotoItem
import java.util.*

class CreateReportViewModel(val repository: Repository) : ViewModel() {

    private var _createReportUserEvent = MutableLiveData<Event<CreateReportUserEvent>>()
    val createReportUserEvent: LiveData<Event<CreateReportUserEvent>>
        get() = _createReportUserEvent

    var isOnSearch: Boolean = false
    var isAddingCrewMember: Boolean = false

    lateinit var report: Report
    val reportPhotos: MutableList<PhotoItem> = mutableListOf()

    fun initReport() {
        val officer = repository.getCurrentOfficer()
        report = Report().apply {
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

    fun saveReport(isDraft: Boolean? = null, listener: OnSaveListener) {
        report.draft = isDraft
        val photosToSave = reportPhotos.map { Pair(it.photo, it.localUri) }
        repository.saveReport(report, photosToSave, listener)
    }

    fun onBackPressed(): Boolean {
        if (isOnSearch || isAddingCrewMember) {
            return false
        }
        _createReportUserEvent.value = Event(CreateReportUserEvent.AskDiscardBoarding)
        return true
    }


    sealed class CreateReportUserEvent {
        object AskDiscardBoarding: CreateReportUserEvent()
    }
}