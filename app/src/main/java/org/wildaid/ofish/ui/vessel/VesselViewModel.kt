package org.wildaid.ofish.ui.vessel

import android.net.Uri
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.*
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem

class VesselViewModel(val repository: Repository) : ViewModel() {
    val vesselItemLiveData = MutableLiveData<VesselItem>()
    val deliveryItemItemLiveData = MutableLiveData<DeliveryItem>()
    val emsLiveData = MutableLiveData<List<EMSItem>>()
    val itemClicksLiveData = MutableLiveData<Event<Int>>()
    var isNewBusiness = false

    private lateinit var currentVesselItem: VesselItem
    private lateinit var currentDeliveryItem: DeliveryItem
    private lateinit var currentEMSItems: MutableList<EMSItem>
    private lateinit var currentReport: Report
    private lateinit var currentReportPhotos: MutableList<PhotoItem>

    private var lastFocusInInfo = false
    private var lastFocusDelivery = false

    private val infoFocusIds = setOf(
        R.id.vessel_name,
        R.id.vessel_permit_number,
        R.id.vessel_home_port,
        R.id.vessel_flag_state,
        R.id.vessel_note
    )

    private val deliveryFocusIds = setOf(
        R.id.delivery_edit_date,
        R.id.delivery_business,
        R.id.delivery_location,
        R.id.delivery_note
    )

    fun initVessel(
        report: Report,
        currentReportPhotos: MutableList<PhotoItem>
    ) {
        this.currentReport = report
        this.currentReportPhotos = currentReportPhotos

        report.vessel?.also {
            currentVesselItem = VesselItem(it, true, AttachmentItem(it.attachments!!))
            vesselItemLiveData.value = currentVesselItem
        }

        report.vessel?.lastDelivery?.also {
            currentDeliveryItem = DeliveryItem(it, true, AttachmentItem(it.attachments!!))
            deliveryItemItemLiveData.value = currentDeliveryItem
        }

        currentEMSItems = mutableListOf()
        emsLiveData.value = currentEMSItems
    }

    fun fillVesselInfo(vesselToPrefill: Boat) {
        currentVesselItem.vessel.apply {
            name = vesselToPrefill.name
            homePort = vesselToPrefill.homePort
            nationality = vesselToPrefill.nationality
            permitNumber = vesselToPrefill.permitNumber
        }

        vesselItemLiveData.value = currentVesselItem
    }

    fun updateVesselFlagState(flagState: String) {
        currentVesselItem.vessel.nationality = flagState
        vesselItemLiveData.value = currentVesselItem
    }

    fun updateDeliveryBusiness(business: Pair<String, String>) {
        currentDeliveryItem.lastDelivery.business = business.first
        currentDeliveryItem.lastDelivery.location = business.second
    }

    fun addNoteForVessel() {
        currentVesselItem.attachments.addNote()
        vesselItemLiveData.value = currentVesselItem
    }

    fun removeNoteFromVessel() {
        currentVesselItem.attachments.removeNote()
        vesselItemLiveData.value = currentVesselItem
    }

    fun addNoteForDelivery() {
        currentDeliveryItem.attachments.addNote()
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    fun removeNoteFromDelivery() {
        currentDeliveryItem.attachments.removeNote()
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    fun addPhotoForVessel(imageUri: Uri) {
        val newPhoto = createPhoto(imageUri)
        currentReportPhotos.add(newPhoto)
        currentVesselItem.attachments.addPhoto(newPhoto)
        vesselItemLiveData.value = currentVesselItem
    }

    fun removePhotoFromVessel(photo: PhotoItem) {
        currentReportPhotos.remove(photo)
        currentVesselItem.attachments.removePhoto(photo)
        vesselItemLiveData.value = currentVesselItem
    }

    fun addPhotoForDelivery(imageUri: Uri) {
        val newPhoto = createPhoto(imageUri)
        currentReportPhotos.add(newPhoto)
        currentDeliveryItem.attachments.addPhoto(newPhoto)
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    fun removePhotoFromDelivery(photo: PhotoItem) {
        currentReportPhotos.remove(photo)
        currentDeliveryItem.attachments.removePhoto(photo)
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    fun removeEms(position: Int = -1) {
        currentEMSItems.removeAt(position)
        currentVesselItem.vessel.ems.removeAt(position)
        emsLiveData.value = currentEMSItems
    }

    fun addEms() {
        collapseDeliveryIfPossible()
        collapseInfoIfPossible()

        val newEMS = EMS()
        currentVesselItem.vessel.ems.add(newEMS)
        currentEMSItems.add(EMSItem(newEMS, true, AttachmentItem(newEMS.attachments!!)))

        emsLiveData.value = currentEMSItems.also {
            it.forEachIndexed { index, item -> item.inEditMode = index == it.lastIndex }
        }
    }

    fun updateEmsType(emsItem: EMSItem, emsType: String) {
        emsItem.ems.emsType = emsType
        emsLiveData.value = currentEMSItems
    }

    fun editEms(emsItem: EMSItem) {
        emsLiveData.value = currentEMSItems.also {
            it.forEach { item -> item.inEditMode = item == emsItem }
        }
    }

    fun addNoteForEms(emsItem: EMSItem) {
        emsItem.attachments.addNote()
        emsLiveData.value = currentEMSItems
    }

    fun removeNoteFromEms(emsItem: EMSItem) {
        emsItem.attachments.removeNote()
        emsLiveData.value = currentEMSItems
    }

    fun addPhotoForEms(imageUri: Uri, emsItem: EMSItem) {
        val newPhoto = createPhoto(imageUri)
        emsItem.attachments.addPhoto(newPhoto)
        currentReportPhotos.add(newPhoto)

        emsLiveData.value = currentEMSItems
    }

    fun removePhotoFromEms(photo: PhotoItem, emsItem: EMSItem) {
        emsItem.attachments.removePhoto(photo)
        currentReportPhotos.remove(photo)
        emsLiveData.value = currentEMSItems
    }

    fun onNextClicked() {
        itemClicksLiveData.value = Event(R.id.btn_next)
    }

    val fieldFocusListener = View.OnFocusChangeListener { view, hasFocus ->
        val fieldId = view.id
        if (hasFocus) {

            when (fieldId) {
                R.id.btn_vessel_info_edit -> expandInfo()
                R.id.btn_vessel_delivery_edit -> expandDelivery()
                else -> itemClicksLiveData.value = Event(fieldId)
            }

            if (fieldId in infoFocusIds && lastFocusDelivery) {
                collapseDeliveryIfPossible()
                lastFocusInInfo = true
                lastFocusInInfo = false
            } else if (fieldId in deliveryFocusIds && lastFocusInInfo) {
                collapseInfoIfPossible()
                lastFocusDelivery = true
                lastFocusInInfo = false
            } else {
                lastFocusDelivery = fieldId in deliveryFocusIds
                lastFocusInInfo = fieldId in infoFocusIds
            }
        }
    }

    private fun expandInfo() {
        currentVesselItem.inEditMode = true
        currentDeliveryItem.inEditMode = false
        vesselItemLiveData.value = currentVesselItem
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    private fun expandDelivery() {
        currentVesselItem.inEditMode = false
        currentDeliveryItem.inEditMode = true
        vesselItemLiveData.value = currentVesselItem
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    private fun collapseInfoIfPossible() {
        val vesselInEdit = when {
            currentVesselItem.vessel.name.isNotBlank() -> false
            currentVesselItem.vessel.nationality.isNotBlank() -> false
            currentVesselItem.vessel.permitNumber.isNotBlank() -> false
            currentVesselItem.vessel.homePort.isNotBlank() -> false
            currentVesselItem.attachments.getNote().orEmpty().isNotBlank() -> false
            else -> true
        }

        currentVesselItem.inEditMode = vesselInEdit
        vesselItemLiveData.value = currentVesselItem
    }

    private fun collapseDeliveryIfPossible() {
        val deliveryInEditMode = when {
            currentDeliveryItem.lastDelivery.business.isNotBlank() -> false
            currentDeliveryItem.lastDelivery.location.isNotBlank() -> false
            currentDeliveryItem.attachments.getNote().orEmpty().isNotBlank() -> false
            else -> true
        }
        currentDeliveryItem.inEditMode = deliveryInEditMode
        deliveryItemItemLiveData.value = currentDeliveryItem
    }

    private fun createPhoto(imageUri: Uri): PhotoItem {
        return PhotoItem(
            Photo().apply {
                agency = repository.getCurrentOfficer().agency
                referencingReportID = currentReport._id.toString()
            },
            imageUri
        )
    }

    fun createNewBusiness() {
        isNewBusiness = true
        currentDeliveryItem.lastDelivery.business = ""
        currentDeliveryItem.lastDelivery.location = ""
        deliveryItemItemLiveData.value = currentDeliveryItem
    }
}