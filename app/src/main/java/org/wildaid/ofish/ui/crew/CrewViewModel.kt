package org.wildaid.ofish.ui.crew

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

const val N_A = "N/A"

class CrewViewModel(
    val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private var _crewMembersData = MutableLiveData<List<CrewMemberItem>>()
    val crewMembersData: LiveData<List<CrewMemberItem>>
        get() = _crewMembersData

    private var _canAddNewMemberData = MutableLiveData(true)
    val canAddNewMemberData: LiveData<Boolean>
        get() = _canAddNewMemberData

    private var _buttonIdData = MutableLiveData<Event<Int>>()
    val buttonIdData: LiveData<Event<Int>>
        get() = _buttonIdData

    private lateinit var currentReport: Report
    private lateinit var currentReportPhotos: MutableList<PhotoItem>
    private var currentCrewItems = mutableListOf<CrewMemberItem>()

    private val captainTitle = getString(R.string.captain)
    private val memberTitle = getString(R.string.crew_member)

    fun initCrewMembers(
        currentReport: Report,
        currentReportPhotos: MutableList<PhotoItem>
    ) {
        this.currentReport = currentReport
        this.currentReportPhotos = currentReportPhotos
        this._crewMembersData.value = initiateCrewMembers()
        addCrewMember()
    }

    fun fillCrew(captain: CrewMember, crews: List<CrewMember>) {
        currentCrewItems.clear()

        addCrewMember(captain, isCaptain = true)
        crews.forEach {
            addCrewMember(it)
        }
    }

    fun updateCrewMembersIfNeeded() {
        if (isCrewChanged()) {
            val newCrewMembers = mutableListOf<CrewMemberItem>()
            newCrewMembers.add(
                CrewMemberItem(
                    crewMember = currentReport.captain!!,
                    title = captainTitle,
                    attachments = AttachmentItem(currentReport.captain!!.attachments!!),
                    isRemovable = false,
                    inEditMode = false,
                    isCaptain = true
                )
            )
            newCrewMembers.addAll(currentReport.crew.mapIndexed { index, member ->
                CrewMemberItem(
                    crewMember = member,
                    title = "$memberTitle ${index + 1}",
                    attachments = AttachmentItem(member.attachments!!),
                    inEditMode = false
                )
            })

            currentCrewItems.also {
                it.clear()
                it.addAll(newCrewMembers)
            }

            fillEmptyFields(currentCrewItems)
            _crewMembersData.value = currentCrewItems
        }
    }

    fun addNoteForMember(member: CrewMemberItem) {
        currentCrewItems.find { member.crewMember == it.crewMember }?.attachments?.addNote()
        _crewMembersData.value = currentCrewItems
    }

    fun removeNoteFromMember(member: CrewMemberItem) {
        currentCrewItems.find { member.crewMember == it.crewMember }?.attachments?.removeNote()
        _crewMembersData.value = currentCrewItems
    }

    fun addPhotoForMember(imageUri: Uri, member: CrewMemberItem) {
        val newPhotoItem = PhotoItem(createPhoto(), imageUri)
        currentReportPhotos.add(newPhotoItem)
        currentCrewItems.find { member.crewMember == it.crewMember }?.attachments?.addPhoto(
            newPhotoItem
        )
        _crewMembersData.value = currentCrewItems
    }

    fun removePhotoFromMember(photoItem: PhotoItem, member: CrewMemberItem) {
        currentReportPhotos.remove(photoItem)
        currentCrewItems.find { member.crewMember == it.crewMember }?.attachments?.removePhoto(
            photoItem
        )
        _crewMembersData.value = currentCrewItems
    }

    fun addCrewMember(crewMember: CrewMember? = null, isCaptain: Boolean = false) {
        fillEmptyFields(currentCrewItems)

        currentCrewItems.forEach {
            it.inEditMode = false
        }

        val newCrewMember = crewMember ?: CrewMember()
        currentReport.crew.add(newCrewMember)
        currentCrewItems.add(
            CrewMemberItem(
                newCrewMember,
                isCaptain = isCaptain,
                title = if (isCaptain) getString(R.string.captain) else "${getString(R.string.crew_member)} ${currentCrewItems.size}",
                attachments = AttachmentItem(newCrewMember.attachments!!),
                isRemovable = !isCaptain
            )
        )

        _crewMembersData.value = currentCrewItems
        updateAddNewMemberVisibility()
    }

    fun removeCrewMember(position: Int) {
        currentCrewItems.removeAt(position)
        // Captain is stored in another field.. So reduce collection by 1
        currentReport.crew.removeAt(position.dec())

        currentCrewItems.forEachIndexed { index, it ->
            if (index > 0) {
                it.title = "Crew Member $index"
            }
        }

        _crewMembersData.value = currentCrewItems
        updateAddNewMemberVisibility()
    }

    fun onCrewMemberChanged() {
        updateAddNewMemberVisibility()
    }

    fun editCrewMember(member: CrewMemberItem) {
        currentCrewItems.forEach { it.inEditMode = it.crewMember == member.crewMember }
        fillEmptyFields(currentCrewItems)
        _crewMembersData.value = currentCrewItems
        updateAddNewMemberVisibility()
    }

    fun onNextClicked() {
        _buttonIdData.value = Event(R.id.btn_next)
    }

    private fun isCrewChanged() =
        // report crew size is less by 1 because captain is stored separately in another field
        currentReport.captain != currentCrewItems[0].crewMember || currentReport.crew.size != currentCrewItems.size - 1

    private fun initiateCrewMembers(): List<CrewMemberItem> {
        val captain = CrewMember()
        val crewMember = CrewMember()
        currentReport.captain = captain
        currentReport.crew.add(crewMember)

        currentCrewItems = mutableListOf<CrewMemberItem>().apply {
            add(
                CrewMemberItem(
                    captain,
                    title = captainTitle,
                    attachments = AttachmentItem(captain.attachments!!),
                    isRemovable = false,
                    isCaptain = true
                )
            )

            add(
                CrewMemberItem(
                    crewMember,
                    title = "$memberTitle 1",
                    attachments = AttachmentItem(crewMember.attachments!!),
                    isRemovable = true,
                    isCaptain = false
                )
            )
        }

        return currentCrewItems
    }

    private fun updateAddNewMemberVisibility() {
        val crew = crewMembersData.value
        val notFinishedMembers = crew?.filter {
            // Captain can have empty fields
            (it.crewMember.name.isBlank() && it.crewMember.license.isBlank()) && !it.isCaptain
        }

        _canAddNewMemberData.value = notFinishedMembers.isNullOrEmpty()
    }

    private fun fillEmptyFields(crew: MutableList<CrewMemberItem>) {
        val iterator = crew.listIterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            val emptyName = it.crewMember.name.isBlank()
            val emptyLicense = it.crewMember.license.isBlank()
            val noNotes = it.attachments.getNote().isNullOrBlank()
            val noPhotos = !it.attachments.hasPhotos()

            if (!it.isCaptain && (emptyLicense && emptyName && noPhotos && noNotes)) {
                currentReport.crew.remove(it.crewMember)
                iterator.remove()
            } else {
                it.crewMember.name = it.crewMember.name.ifBlank { N_A }
                it.crewMember.license = it.crewMember.license.ifBlank { N_A }
            }
        }
    }

    private fun createPhoto(): Photo {
        return Photo().apply {
            referencingReportID = currentReport._id.toString()
        }
    }
}