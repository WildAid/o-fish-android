package org.wildaid.ofish.ui.violation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class ViolationViewModel(application: Application) :
    AndroidViewModel(application) {

    val violationLiveData = MutableLiveData<List<ViolationItem>>()
    val buttonId = MutableLiveData<Event<Int>>()
    lateinit var currentReport: Report

    private val violationTitle = getString(R.string.violation)
    private val currentViolationItems = mutableListOf<ViolationItem>()
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    fun initViolations(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos

        addViolation()
    }

    fun refreshIssuedTo() {
        violationLiveData.value = currentViolationItems
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
        violationLiveData.value = currentViolationItems
    }

    fun removeNoteFromViolation(item: ViolationItem) {
        currentViolationItems.find { it.violation == item.violation }?.attachments?.removeNote()
        violationLiveData.value = currentViolationItems
    }

    fun addPhotoForViolation(imageUri: Uri, item: ViolationItem) {
        val newPhotoItem = PhotoItem(createPhoto(), imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentViolationItems.find { it.violation == item.violation }?.attachments?.addPhoto(
            newPhotoItem
        )
        violationLiveData.value = currentViolationItems
    }

    fun removePhotoFromViolation(photoItem: PhotoItem, item: ViolationItem) {
        currentReportPhotos.remove(photoItem)
        currentViolationItems.find { it.violation == item.violation }?.attachments?.removePhoto(
            photoItem
        )
        violationLiveData.value = currentViolationItems
    }

    fun editViolation(editItem: ViolationItem) {
        notifyWithEditItem(currentViolationItems, editItem)
    }

    private fun notifyWithEditItem(
        list: MutableList<ViolationItem>,
        editingItem: ViolationItem? = null
    ) {
        violationLiveData.value = list.also {
            it.forEachIndexed() { index, item ->
                if (editingItem != null) {
                    item.inEditMode = item == editingItem
                }
                item.title = "$violationTitle ${index + 1}"
            }
        }
    }

    fun updateViolationExplanation(id: String?, violation: String) {
        currentViolationItems.findLast { v ->
            v.title == id
        }?.violation?.offence?.explanation = violation.replace(',', '\n')
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
        buttonId.value = Event(R.id.btn_next)
    }

    private fun createPhoto(): Photo {
        return Photo().apply {
            referencingReportID = currentReport._id.toString()
        }
    }
}
