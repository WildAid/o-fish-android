package org.wildaid.ofish.ui.crew

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.RealmList
import org.wildaid.ofish.Event
import org.wildaid.ofish.R
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.CrewMember
import org.wildaid.ofish.data.report.Photo
import org.wildaid.ofish.data.report.Report
import org.wildaid.ofish.ui.base.AttachmentItem
import org.wildaid.ofish.ui.base.BaseReportViewModel
import org.wildaid.ofish.ui.base.PhotoItem
import org.wildaid.ofish.util.getString

class CrewViewModel(
    repository: Repository,
    app: Application
) : BaseReportViewModel(repository, app) {

    private var _crewMembersData = MutableLiveData<List<CrewMemberItem>>()
    val crewMembersData: LiveData<List<CrewMemberItem>>
        get() = _crewMembersData

    private var _canAddNewMemberData = MutableLiveData(true)
    val canAddNewMemberData: LiveData<Boolean>
        get() = _canAddNewMemberData

    private var _crewUserEvent = MutableLiveData<Event<CrewUserEvent>>()
    val crewUserEvent: LiveData<Event<CrewUserEvent>>
        get() = _crewUserEvent

    private var currentCrewItems = mutableListOf<CrewMemberItem>()

    private val captainTitle = getString(R.string.captain)
    private val memberTitle = getString(R.string.crew_member)

    override fun initViewModel(report: Report, currentReportPhotos: MutableList<PhotoItem>) {
        super.initViewModel(report, currentReportPhotos)
        this._crewMembersData.postValue(initiateCrewMembers())
    }

    fun fillCrew(captain: CrewMember, crews: List<CrewMember>) {
        currentCrewItems.clear()
        currentReport.crew.clear()

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
        val newPhotoItem = createPhotoItem(imageUri)
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
        currentCrewItems.forEach {
            it.inEditMode = false
        }

        val newCrewMember = crewMember ?: CrewMember()
        if (isCaptain) {
            currentReport.captain = crewMember
        } else {
            currentReport.crew.add(newCrewMember)
        }

        currentCrewItems.add(
            CrewMemberItem(
                newCrewMember,
                isCaptain = isCaptain,
                title = if (isCaptain) getString(R.string.captain) else "${getString(R.string.crew_member)} ${currentCrewItems.size}",
                attachments = AttachmentItem(newCrewMember.attachments!!).apply {
                    photos.addAll(repository.getPhotosWithIds(newCrewMember.attachments!!.photoIDs).map { PhotoItem(it, null) })
                },
                isRemovable = !isCaptain
            )
        )

        _crewMembersData.value = currentCrewItems
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
    }

    fun onCrewMemberChanged() {
    }

    fun editCrewMember(member: CrewMemberItem) {
        currentCrewItems.forEach { it.inEditMode = it.crewMember == member.crewMember }
        _crewMembersData.value = currentCrewItems
    }

    fun onNextClicked() {
        _crewUserEvent.value = Event(CrewUserEvent.NextUserEvent)
    }

    private fun isCrewChanged() =
        // report crew size is less by 1 because captain is stored separately in another field
        currentReport.captain != currentCrewItems[0].crewMember || currentReport.crew.size != currentCrewItems.size - 1

    private fun initiateCrewMembers(): List<CrewMemberItem> {
        currentCrewItems = mutableListOf()

        // Init captain
        val captain = currentReport.captain ?: CrewMember()
        currentReport.captain = captain

        currentCrewItems.add(
            CrewMemberItem(
                captain,
                title = captainTitle,
                attachments = AttachmentItem(captain.attachments!!),
                isRemovable = false,
                isCaptain = true,
                inEditMode = false
            )
        )

        // Init crew
        val crew = currentReport.crew.ifEmpty { RealmList(CrewMember()) }
        currentReport.crew = crew

        crew.forEachIndexed { index, it ->
            currentCrewItems.add(
                CrewMemberItem(
                    it,
                    title = "$memberTitle ${index.inc()}",
                    attachments = AttachmentItem(it.attachments!!),
                    isRemovable = true,
                    isCaptain = false,
                    inEditMode = false
                )
            )
        }
        return currentCrewItems
    }

    sealed class CrewUserEvent {
        object NextUserEvent : CrewUserEvent()
    }
}