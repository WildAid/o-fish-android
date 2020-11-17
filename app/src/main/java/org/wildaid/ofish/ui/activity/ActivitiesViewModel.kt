package org.wildaid.ofish.ui.activity

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.BaseReportViewModel
import org.wildaid.ofish.ui.base.PhotoItem

class ActivitiesViewModel(
    repository: Repository,
    app: Application
) : BaseReportViewModel(repository, app) {

    private val _activityItemLiveData = MutableLiveData<ActivityItem>()
    val activityItemLiveData: LiveData<ActivityItem>
        get() = _activityItemLiveData
    private val _fisheryItemLiveData = MutableLiveData<FisheryItem>()
    val fisheryItemLiveData: LiveData<FisheryItem>
        get() = _fisheryItemLiveData

    private val _gearItemLiveData = MutableLiveData<GearItem>()
    val gearItemLiveData: LiveData<GearItem>
        get() = _gearItemLiveData

    private val _activitiesUserEvents: MutableLiveData<Event<ActivitiesUserEvent>> =
        MutableLiveData()
    val activitiesUserEvents: LiveData<Event<ActivitiesUserEvent>>
        get() = _activitiesUserEvents

    private lateinit var currentActivityItem: ActivityItem
    private lateinit var currentFisheryItem: FisheryItem
    private lateinit var currentGearItem: GearItem

    override fun initViewModel(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        super.initViewModel(report, currentReportPhotos)

        report.inspection?.activity?.let {
            currentActivityItem = ActivityItem(it, AttachmentItem(
                it.attachments!!,
                getPhotoItemsForIds(it.attachments!!.photoIDs)
            ))
            _activityItemLiveData.postValue(currentActivityItem)
        }

        report.inspection?.fishery?.let {
            currentFisheryItem = FisheryItem(it, AttachmentItem(
                it.attachments!!,
                getPhotoItemsForIds(it.attachments!!.photoIDs)
            ))
            _fisheryItemLiveData.postValue(currentFisheryItem)
        }

        report.inspection?.gearType?.let {
            currentGearItem = GearItem(it, AttachmentItem(
                it.attachments!!,
                getPhotoItemsForIds(it.attachments!!.photoIDs)
            ))
            _gearItemLiveData.postValue(currentGearItem)
        }
    }

    fun updateActivity(activity: String) {
        currentActivityItem.activity.name = activity
        _activityItemLiveData.value = currentActivityItem
    }

    fun addNoteForActivity() {
        currentActivityItem.attachments.addNote()
        _activityItemLiveData.value = currentActivityItem
    }

    fun removeNoteFromActivity() {
        currentActivityItem.attachments.removeNote()
        _activityItemLiveData.value = currentActivityItem
    }

    fun addPhotoForActivity(imageUri: Uri) {
        val newPhotoItem = createPhotoItem(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentActivityItem.attachments.addPhoto(newPhotoItem)
        _activityItemLiveData.value = currentActivityItem
    }

    fun removePhotoFromActivity(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentActivityItem.attachments.removePhoto(photoItem)
        _activityItemLiveData.value = currentActivityItem
    }

    fun updateFishery(fishery: String) {
        currentFisheryItem.fishery.name = fishery
        _fisheryItemLiveData.value = currentFisheryItem
    }

    fun addNoteForFishery() {
        currentFisheryItem.attachments.addNote()
        _fisheryItemLiveData.value = currentFisheryItem
    }

    fun removeNoteFromFishery() {
        currentFisheryItem.attachments.removeNote()
        _fisheryItemLiveData.value = currentFisheryItem
    }

    fun addPhotoForFishery(imageUri: Uri) {
        val newPhotoItem = createPhotoItem(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentFisheryItem.attachments.addPhoto(newPhotoItem)
        _fisheryItemLiveData.value = currentFisheryItem
    }

    fun removePhotoFromFishery(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentFisheryItem.attachments.removePhoto(photoItem)
        _fisheryItemLiveData.value = currentFisheryItem
    }

    fun updateGear(gear: String) {
        currentGearItem.gear.name = gear
        _gearItemLiveData.value = currentGearItem
    }

    fun addNoteForGear() {
        currentGearItem.attachments.addNote()
        _gearItemLiveData.value = currentGearItem
    }

    fun removeNoteFromGear() {
        currentGearItem.attachments.removeNote()
        _gearItemLiveData.value = currentGearItem
    }

    fun addPhotoForGear(imageUri: Uri) {
        val newPhotoItem = createPhotoItem(imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentGearItem.attachments.addPhoto(newPhotoItem)
        _gearItemLiveData.value = currentGearItem
    }

    fun removePhotoFromGear(photoItem: PhotoItem) {
        currentReportPhotos.remove(photoItem)
        currentGearItem.attachments.removePhoto(photoItem)
        _gearItemLiveData.value = currentGearItem
    }

    fun chooseActivity() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.ChooseActivityEvent)
    }

    fun chooseFishery() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.ChooseFisheryEvent)
    }

    fun chooseGear() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.ChooseGearEvent)
    }

    fun next() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.NextEvent)
    }

    fun addActivityAttachment() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.AddActivityAttachmentEvent)
    }

    fun addFisheryAttachment() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.AddFisheryAttachmentEvent)
    }

    fun addGearAttachment() {
        _activitiesUserEvents.value = Event(ActivitiesUserEvent.AddGearAttachmentEvent)
    }

    sealed class ActivitiesUserEvent {
        object ChooseActivityEvent : ActivitiesUserEvent()
        object ChooseFisheryEvent : ActivitiesUserEvent()
        object ChooseGearEvent : ActivitiesUserEvent()
        object NextEvent : ActivitiesUserEvent()
        object AddActivityAttachmentEvent : ActivitiesUserEvent()
        object AddFisheryAttachmentEvent : ActivitiesUserEvent()
        object AddGearAttachmentEvent : ActivitiesUserEvent()
    }
}