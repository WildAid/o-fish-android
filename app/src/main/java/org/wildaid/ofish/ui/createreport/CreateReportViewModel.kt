package org.wildaid.ofish.ui.createreport

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.OnSaveListener
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.PhotoItem

class CreateReportViewModel(val repository: Repository) : ViewModel() {
    val discardReportLiveData = MutableLiveData<Event<Boolean>>()
    var isOnSearch: Boolean = false

    lateinit var report: Report
    lateinit var reportPhotos: MutableList<PhotoItem>

    fun initReport() {
        val currentUser = repository.getCurrentUser()

        reportPhotos = mutableListOf()
        report = Report().apply {
            reportingOfficer?.apply {
                email = currentUser?.email.orEmpty()
                name?.apply {
                    last = currentUser?.firstName.orEmpty()
                    last = currentUser?.lastName.orEmpty()
                }
            }
        }
    }

    fun saveReport(listener: OnSaveListener) {
        val photosToSave = reportPhotos.map { Pair(it.photo, it.localUri) }
        repository.saveReport(report, photosToSave, listener)
    }

    fun onBackPressed(): Boolean {
        if (isOnSearch) {
            return false
        }
        discardReportLiveData.value = Event(true)
        return true
    }
}