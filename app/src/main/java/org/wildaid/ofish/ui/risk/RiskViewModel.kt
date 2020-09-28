package org.wildaid.ofish.ui.risk

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.data.report.SafetyLevel
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem

class RiskViewModel(application: Application) : AndroidViewModel(application) {
    val riskLiveData: LiveData<RiskItem>
        get() = _riskLiveData
    val userEventsLiveData: LiveData<Event<RiskUserEvent>>
        get() = _userEventsLiveData

    private lateinit var currentReport: Report
    private lateinit var currentRiskItem: RiskItem
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    private var _riskLiveData = MutableLiveData<RiskItem>()
    private var _userEventsLiveData = MutableLiveData<Event<RiskUserEvent>>()

    fun initReport(currentReport: Report, currentReportPhotos: MutableList<PhotoItem>) {
        this.currentReport = currentReport
        this.currentReportPhotos = currentReportPhotos

        val safetyLevel = this.currentReport.inspection?.summary?.safetyLevel ?: SafetyLevel().apply {
            level = SafetyColor.Green.name
        }
        this.currentRiskItem = RiskItem(safetyLevel, AttachmentItem(safetyLevel.attachments!!))
        _riskLiveData.postValue(this.currentRiskItem)
    }

    fun addPhotoAttachment(imageUri: Uri) {
        val newPhotoItem = createPhoto(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentRiskItem.attachments.addPhoto(newPhotoItem)
        _riskLiveData.value = currentRiskItem
    }

    fun removePhotoFromActivity(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentRiskItem.attachments.removePhoto(photoItem)
        _riskLiveData.value = currentRiskItem
    }

    fun onGreenChosen() {
        currentRiskItem.setLevel(SafetyColor.Green.name)
        _riskLiveData.value = currentRiskItem
    }

    fun onAmberChosen() {
        currentRiskItem.setLevel(SafetyColor.Amber.name)
        _riskLiveData.value = currentRiskItem
    }

    fun onRedChosen() {
        currentRiskItem.setLevel(SafetyColor.Red.name)
        _riskLiveData.value = currentRiskItem
    }

    fun onNextClicked() {
        _userEventsLiveData.value = Event(RiskUserEvent.NextEvent)
    }

    fun onChooseAttachment() {
        _userEventsLiveData.value = Event(RiskUserEvent.AddAttachment)
    }

    private fun createPhoto(imageUri: Uri): PhotoItem {
        return PhotoItem(
            Photo().apply {
                referencingReportID = currentReport._id.toString()
            },
            imageUri
        )
    }

    sealed class RiskUserEvent {
        object NextEvent : RiskUserEvent()
        object AddAttachment : RiskUserEvent()
    }
}