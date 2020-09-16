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

    private var _discardReportLiveData = MutableLiveData<Event<Boolean>>()
    val discardReportLiveData: LiveData<Event<Boolean>>
        get() = _discardReportLiveData

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

            // Prefilled empty items for create report flow
            inspection?.summary?.seizures = Seizures()
            inspection?.summary?.violations?.add(Violation())
            notes.add(AnnotatedNote())
        }
    }

    fun saveReport(listener: OnSaveListener) {
        val photosToSave = reportPhotos.map { Pair(it.photo, it.localUri) }
        repository.saveReport(report, photosToSave, listener)
    }

    fun onBackPressed(): Boolean {
        if (isOnSearch || isAddingCrewMember) {
            return false
        }
        _discardReportLiveData.value = Event(true)
        return true
    }
}