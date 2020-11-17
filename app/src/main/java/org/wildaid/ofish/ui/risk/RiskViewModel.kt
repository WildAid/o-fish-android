package org.wildaid.ofish.ui.risk

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.SafetyColor
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.data.report.SafetyLevel
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.BaseReportViewModel
import org.wildaid.ofish.ui.base.PhotoItem

class RiskViewModel(
    repository: Repository,
    app: Application
) : BaseReportViewModel(repository, app) {
    val riskLiveData: LiveData<RiskItem>
        get() = _riskLiveData
    val userEventsLiveData: LiveData<Event<RiskUserEvent>>
        get() = _userEventsLiveData

    private lateinit var currentRiskItem: RiskItem

    private val _riskLiveData = MutableLiveData<RiskItem>()
    private val _userEventsLiveData = MutableLiveData<Event<RiskUserEvent>>()

    override fun initViewModel(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        super.initViewModel(report, currentReportPhotos)

        val safetyLevel = (this.currentReport.inspection?.summary?.safetyLevel ?: SafetyLevel()).also {
            it.level = it.level.ifBlank { SafetyColor.Green.name }
        }
        this.currentRiskItem = RiskItem(safetyLevel, AttachmentItem(
            safetyLevel.attachments!!,
            getPhotoItemsForIds(safetyLevel.attachments!!.photoIDs)
        ))
        _riskLiveData.postValue(this.currentRiskItem)
    }

    fun addPhotoAttachment(imageUri: Uri) {
        val newPhotoItem = createPhotoItem(imageUri)
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

    sealed class RiskUserEvent {
        object NextEvent : RiskUserEvent()
        object AddAttachment : RiskUserEvent()
    }
}