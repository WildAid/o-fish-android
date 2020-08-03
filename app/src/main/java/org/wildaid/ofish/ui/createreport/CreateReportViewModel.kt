package org.wildaid.ofish.ui.createreport

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.OnSaveListener
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.PhotoItem
import java.util.*

class CreateReportViewModel(val repository: Repository) : ViewModel() {
    val discardReportLiveData = MutableLiveData<Event<Boolean>>()
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
        discardReportLiveData.value = Event(true)
        return true
    }
}