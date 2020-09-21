package org.wildaid.ofish.ui.violation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.OffenceData
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.data.report.Violation
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class ViolationViewModel(
    val repository: Repository,
    application: Application
) :
    AndroidViewModel(application) {

    private var _violationLiveData = MutableLiveData<List<ViolationItem>>()
    val violationLiveData: LiveData<List<ViolationItem>>
        get() = _violationLiveData

    private var _seizureLiveData = MutableLiveData<SeizureItem>()
    val seizureLiveData: LiveData<SeizureItem>
        get() = _seizureLiveData

    private var _violationUserEventLiveData = MutableLiveData<Event<ViolationUserEvent>>()
    val violationUserEventLiveData: LiveData<Event<ViolationUserEvent>>
        get() = _violationUserEventLiveData
    lateinit var currentReport: Report

    private val violationTitle = getString(R.string.violation)
    private val currentViolationItems = mutableListOf<ViolationItem>()
    private lateinit var currentSeizureItem : SeizureItem
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    fun initViolations(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos

        addViolation()

        val seizure = report.inspection?.summary?.seizures!!
        currentSeizureItem = SeizureItem(seizure, AttachmentItem(seizure.attachments!!))
    }

    fun refreshIssuedTo() {
        _violationLiveData.value = currentViolationItems
    }

    fun addViolation() {
        val newViolation = Violation()
        currentReport.inspection?.summary?.violations?.add(newViolation)

        val newItem = ViolationItem(
            violation = newViolation,
            title = "$violationTitle ${currentViolationItems.size + 1}",
            attachments = AttachmentItem(newViolation.attachments!!)
        )

        currentViolationItems.add(newItem)
        notifyWithEditItem(currentViolationItems, newItem)
    }

    fun removeViolation(position: Int) {
        currentReport.inspection?.summary?.violations?.removeAt(position)
        currentViolationItems.removeAt(position)
        notifyWithEditItem(currentViolationItems)
    }

    fun addNoteForViolation(item: ViolationItem) {
        currentViolationItems.find { it.violation == item.violation }?.attachments?.addNote()
        _violationLiveData.value = currentViolationItems
    }

    fun removeNoteFromViolation(item: ViolationItem) {
        currentViolationItems.find { it.violation == item.violation }?.attachments?.removeNote()
        _violationLiveData.value = currentViolationItems
    }

    fun addPhotoForViolation(imageUri: Uri, item: ViolationItem) {
        val newPhotoItem = PhotoItem(createPhoto(), imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentViolationItems.find { it.violation == item.violation }?.attachments?.addPhoto(
            newPhotoItem
        )
        _violationLiveData.value = currentViolationItems
    }

    fun addNoteForSeizure() {
        currentSeizureItem.attachments.addNote()
        _seizureLiveData.value = currentSeizureItem
    }

    fun addPhotoForSeizure(imageUri: Uri) {
        val newPhotoItem = PhotoItem(createPhoto(), imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentSeizureItem.attachments.addPhoto(newPhotoItem)
        _seizureLiveData.value = currentSeizureItem
    }

    fun removePhotoFromSeizure(photoItem: PhotoItem) {
        currentSeizureItem.attachments.removePhoto(photoItem)
        _seizureLiveData.value = currentSeizureItem
    }

    fun removeNoteFromSeizure() {
        currentSeizureItem.attachments.removeNote()
        _seizureLiveData.value = currentSeizureItem
    }

    fun removePhotoFromViolation(photoItem: PhotoItem, item: ViolationItem) {
        currentReportPhotos.remove(photoItem)
        currentViolationItems.find { it.violation == item.violation }?.attachments?.removePhoto(
            photoItem
        )
        _violationLiveData.value = currentViolationItems
    }

    fun editViolation(editItem: ViolationItem) {
        notifyWithEditItem(currentViolationItems, editItem)
    }

    private fun notifyWithEditItem(
        list: MutableList<ViolationItem>,
        editingItem: ViolationItem? = null
    ) {
        _violationLiveData.value = list.also {
            it.forEachIndexed() { index, item ->
                if (editingItem != null) {
                    item.inEditMode = item == editingItem
                }
                item.title = "$violationTitle ${index + 1}"
            }
        }
    }

    fun updateViolationExplanation(id: String?, violation: OffenceData) {
        val item = currentViolationItems.findLast { v ->
            v.title == id
        }
        item?.violation?.offence?.let {
            it.code = violation.code
            it.explanation = violation.explanation
        }
    }

    fun updateIssuedTo(id: String?, crewMember: CrewMember) {
        currentViolationItems.findLast { v ->
            v.title == id
        }?.violation?.crewMember =
            CrewMember().apply {
                name = crewMember.name
                license = crewMember.license
                attachments?.notes?.addAll(crewMember.attachments?.notes.orEmpty())
                attachments?.photoIDs?.addAll(crewMember.attachments?.photoIDs.orEmpty())
            }
    }

    fun onNextClicked() {
        _violationUserEventLiveData.value = Event(ViolationUserEvent.NextEvent)
    }

    private fun createPhoto(): Photo {
        return Photo().apply {
            referencingReportID = currentReport._id.toString()
        }
    }

    sealed class ViolationUserEvent {
        object NextEvent : ViolationUserEvent()
    }
}