package org.wildaid.ofish.ui.activity

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem

class ActivitiesViewModel(val repository: Repository) : ViewModel() {
    val activityItemLiveData = MutableLiveData<ActivityItem>()
    val fisheryItemLiveData = MutableLiveData<FisheryItem>()
    val gearItemLiveData = MutableLiveData<GearItem>()

    val buttonId: MutableLiveData<Event<Int>> = MutableLiveData()

    private lateinit var currentActivityItem: ActivityItem
    private lateinit var currentFisheryItem: FisheryItem
    private lateinit var currentGearItem: GearItem
    private lateinit var currentReport: Report
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    fun initActivities(
        report: Report,
        currentReportPhotos: MutableList<PhotoItem>
    ) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos

        report.inspection?.activity?.let {
            currentActivityItem = ActivityItem(it, AttachmentItem(it.attachments!!))
            activityItemLiveData.value = currentActivityItem
        }

        report.inspection?.fishery?.let {
            currentFisheryItem = FisheryItem(it, AttachmentItem(it.attachments!!))
            fisheryItemLiveData.value = currentFisheryItem
        }

        report.inspection?.gearType?.let {
            currentGearItem = GearItem(it, AttachmentItem(it.attachments!!))
            gearItemLiveData.value = currentGearItem
        }
    }

    fun updateActivity(activity: String) {
        currentActivityItem.activity.name = activity
        activityItemLiveData.value = currentActivityItem
    }

    fun addNoteForActivity() {
        currentActivityItem.attachments.addNote()
        activityItemLiveData.value = currentActivityItem
    }

    fun removeNoteFromActivity() {
        currentActivityItem.attachments.removeNote()
        activityItemLiveData.value = currentActivityItem
    }

    fun addPhotoForActivity(imageUri: Uri) {
        val newPhotoItem = createPhoto(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentActivityItem.attachments.addPhoto(newPhotoItem)
        activityItemLiveData.value = currentActivityItem
    }

    fun removePhotoFromActivity(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentActivityItem.attachments.removePhoto(photoItem)
        activityItemLiveData.value = currentActivityItem
    }

    fun updateFishery(fishery: String) {
        currentFisheryItem.fishery.name = fishery
        fisheryItemLiveData.value = currentFisheryItem
    }

    fun addNoteForFishery() {
        currentFisheryItem.attachments.addNote()
        fisheryItemLiveData.value = currentFisheryItem
    }

    fun removeNoteFromFishery() {
        currentFisheryItem.attachments.removeNote()
        fisheryItemLiveData.value = currentFisheryItem
    }

    fun addPhotoForFishery(imageUri: Uri) {
        val newPhotoItem = createPhoto(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentFisheryItem.attachments.addPhoto(newPhotoItem)
        fisheryItemLiveData.value = currentFisheryItem
    }

    fun removePhotoFromFishery(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentFisheryItem.attachments.removePhoto(photoItem)
        fisheryItemLiveData.value = currentFisheryItem
    }

    fun updateGear(gear: String) {
        currentGearItem.gear.name = gear
        gearItemLiveData.value = currentGearItem
    }

    fun addNoteForGear() {
        currentGearItem.attachments.addNote()
        gearItemLiveData.value = currentGearItem
    }

    fun removeNoteFromGear() {
        currentGearItem.attachments.removeNote()
        gearItemLiveData.value = currentGearItem
    }

    fun addPhotoForGear(imageUri: Uri) {
        val newPhotoItem = createPhoto(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentGearItem.attachments.addPhoto(newPhotoItem)
        gearItemLiveData.value = currentGearItem
    }

    fun removePhotoFromGear(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentGearItem.attachments.removePhoto(photoItem)
        gearItemLiveData.value = currentGearItem
    }

    fun onButtonClicked(id: Int) {
        buttonId.value = Event(id)
    }

    private fun createPhoto(imageUri: Uri): PhotoItem {
        return PhotoItem(
            Photo().apply {
                agency = repository.getCurrentAgency()
                referencingReportID = currentReport._id.toString()
            },
            imageUri
        )
    }
}
